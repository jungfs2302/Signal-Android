package org.gfs.chat.components.settings.app.subscription.manage

import org.gfs.chat.badges.models.Badge
import org.gfs.chat.database.model.databaseprotos.PendingOneTimeDonation
import org.gfs.chat.subscription.Subscription
import org.whispersystems.signalservice.api.subscriptions.ActiveSubscription

data class ManageDonationsState(
  val hasOneTimeBadge: Boolean = false,
  val hasReceipts: Boolean = false,
  val featuredBadge: Badge? = null,
  val subscriptionTransactionState: TransactionState = TransactionState.Init,
  val availableSubscriptions: List<Subscription> = emptyList(),
  val pendingOneTimeDonation: PendingOneTimeDonation? = null,
  val nonVerifiedMonthlyDonation: NonVerifiedMonthlyDonation? = null,
  val subscriberRequiresCancel: Boolean = false,
  private val subscriptionRedemptionState: RedemptionState = RedemptionState.NONE
) {

  fun getMonthlyDonorRedemptionState(): RedemptionState {
    return when (subscriptionTransactionState) {
      TransactionState.Init -> subscriptionRedemptionState
      TransactionState.NetworkFailure -> subscriptionRedemptionState
      TransactionState.InTransaction -> RedemptionState.IN_PROGRESS
      is TransactionState.NotInTransaction -> getStateFromActiveSubscription(subscriptionTransactionState.activeSubscription) ?: subscriptionRedemptionState
    }
  }

  private fun getStateFromActiveSubscription(activeSubscription: ActiveSubscription): RedemptionState? {
    return when {
      activeSubscription.isFailedPayment && !activeSubscription.isPastDue -> RedemptionState.FAILED
      activeSubscription.isPendingBankTransfer -> RedemptionState.IS_PENDING_BANK_TRANSFER
      activeSubscription.isInProgress -> RedemptionState.IN_PROGRESS
      else -> null
    }
  }

  sealed class TransactionState {
    object Init : TransactionState()
    object NetworkFailure : TransactionState()
    object InTransaction : TransactionState()
    class NotInTransaction(val activeSubscription: ActiveSubscription) : TransactionState()
  }

  enum class RedemptionState {
    NONE,
    IN_PROGRESS,
    IS_PENDING_BANK_TRANSFER,
    FAILED
  }
}
