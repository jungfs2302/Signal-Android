package org.gfs.chat.components.settings.app.subscription

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.logging.Log
import org.signal.core.util.money.FiatMoney
import org.signal.donations.PaymentSourceType
import org.gfs.chat.badges.models.Badge
import org.gfs.chat.components.settings.app.subscription.DonationSerializationHelper.toFiatMoney
import org.gfs.chat.components.settings.app.subscription.boost.Boost
import org.gfs.chat.components.settings.app.subscription.errors.DonationError
import org.gfs.chat.components.settings.app.subscription.errors.DonationError.BadgeRedemptionError
import org.gfs.chat.components.settings.app.subscription.errors.DonationErrorSource
import org.gfs.chat.database.InAppPaymentTable
import org.gfs.chat.database.RecipientTable
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.database.model.InAppPaymentReceiptRecord
import org.gfs.chat.database.model.databaseprotos.InAppPaymentData
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobs.InAppPaymentOneTimeContextJob
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId
import org.whispersystems.signalservice.api.services.DonationsService
import java.util.Currency
import java.util.Locale
import java.util.concurrent.TimeUnit

class OneTimeInAppPaymentRepository(private val donationsService: DonationsService) {

  companion object {
    private val TAG = Log.tag(OneTimeInAppPaymentRepository::class.java)

    fun <T : Any> handleCreatePaymentIntentError(throwable: Throwable, badgeRecipient: RecipientId, paymentSourceType: PaymentSourceType): Single<T> {
      return if (throwable is DonationError) {
        Single.error(throwable)
      } else {
        val recipient = Recipient.resolved(badgeRecipient)
        val errorSource = if (recipient.isSelf) DonationErrorSource.ONE_TIME else DonationErrorSource.GIFT
        Single.error(DonationError.getPaymentSetupError(errorSource, throwable, paymentSourceType))
      }
    }

    fun verifyRecipientIsAllowedToReceiveAGift(badgeRecipient: RecipientId): Completable {
      return Completable.fromAction {
        Log.d(TAG, "Verifying badge recipient $badgeRecipient", true)
        val recipient = Recipient.resolved(badgeRecipient)

        if (recipient.isSelf) {
          Log.d(TAG, "Cannot send a gift to self.", true)
          throw DonationError.GiftRecipientVerificationError.SelectedRecipientIsInvalid
        }

        if (recipient.isGroup || recipient.isDistributionList || recipient.registered != RecipientTable.RegisteredState.REGISTERED) {
          Log.w(TAG, "Invalid badge recipient $badgeRecipient. Verification failed.", true)
          throw DonationError.GiftRecipientVerificationError.SelectedRecipientIsInvalid
        }
      }.subscribeOn(Schedulers.io())
    }
  }

  fun getBoosts(): Single<Map<Currency, List<Boost>>> {
    return Single.fromCallable { donationsService.getDonationsConfiguration(Locale.getDefault()) }
      .subscribeOn(Schedulers.io())
      .flatMap { it.flattenResult() }
      .map { config ->
        config.getBoostAmounts().mapValues { (_, value) ->
          value.map {
            Boost(it)
          }
        }
      }
  }

  fun getBoostBadge(): Single<Badge> {
    return Single
      .fromCallable {
        AppDependencies.donationsService
          .getDonationsConfiguration(Locale.getDefault())
      }
      .subscribeOn(Schedulers.io())
      .flatMap { it.flattenResult() }
      .map { it.getBoostBadges().first() }
  }

  fun getMinimumDonationAmounts(): Single<Map<Currency, FiatMoney>> {
    return Single.fromCallable { donationsService.getDonationsConfiguration(Locale.getDefault()) }
      .flatMap { it.flattenResult() }
      .subscribeOn(Schedulers.io())
      .map { it.getMinimumDonationAmounts() }
  }

  fun waitForOneTimeRedemption(
    inAppPayment: InAppPaymentTable.InAppPayment,
    paymentIntentId: String,
    paymentSourceType: PaymentSourceType
  ): Completable {
    val isLongRunning = paymentSourceType == PaymentSourceType.Stripe.SEPADebit
    val isBoost = inAppPayment.data.recipientId?.let { RecipientId.from(it) } == Recipient.self().id
    val donationErrorSource: DonationErrorSource = if (isBoost) DonationErrorSource.ONE_TIME else DonationErrorSource.GIFT

    val timeoutError: DonationError = if (isLongRunning) {
      BadgeRedemptionError.DonationPending(donationErrorSource, inAppPayment)
    } else {
      BadgeRedemptionError.TimeoutWaitingForTokenError(donationErrorSource)
    }

    return Single.fromCallable {
      val inAppPaymentReceiptRecord = if (isBoost) {
        InAppPaymentReceiptRecord.createForBoost(inAppPayment.data.amount!!.toFiatMoney())
      } else {
        InAppPaymentReceiptRecord.createForGift(inAppPayment.data.amount!!.toFiatMoney())
      }

      val donationTypeLabel = inAppPaymentReceiptRecord.type.code.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.US) else c.toString() }

      Log.d(TAG, "Confirmed payment intent. Recording $donationTypeLabel receipt and submitting badge reimbursement job chain.", true)
      SignalDatabase.donationReceipts.addReceipt(inAppPaymentReceiptRecord)

      SignalDatabase.inAppPayments.update(
        inAppPayment = inAppPayment.copy(
          data = inAppPayment.data.copy(
            redemption = InAppPaymentData.RedemptionState(
              stage = InAppPaymentData.RedemptionState.Stage.INIT,
              paymentIntentId = paymentIntentId
            )
          )
        )
      )

      InAppPaymentOneTimeContextJob.createJobChain(inAppPayment).enqueue()
      inAppPayment.id
    }.flatMap { inAppPaymentId ->
      Log.d(TAG, "Awaiting completion of redemption chain for up to 10 seconds.", true)
      InAppPaymentsRepository.observeUpdates(inAppPaymentId).filter {
        it.state == InAppPaymentTable.State.END
      }.take(1).firstOrError().timeout(10, TimeUnit.SECONDS, Single.error(timeoutError))
    }.map {
      if (it.data.error != null) {
        Log.d(TAG, "Failure during redemption chain.", true)
        throw DonationError.genericBadgeRedemptionFailure(DonationErrorSource.MONTHLY)
      }
      it
    }.ignoreElement()
  }
}
