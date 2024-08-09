package org.gfs.chat.components.settings.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.gfs.chat.components.settings.app.subscription.InAppDonations
import org.gfs.chat.components.settings.app.subscription.RecurringInAppPaymentRepository
import org.gfs.chat.conversationlist.model.UnreadPaymentsLiveData
import org.gfs.chat.database.model.InAppPaymentSubscriberRecord
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.util.TextSecurePreferences
import org.gfs.chat.util.livedata.Store

class AppSettingsViewModel : ViewModel() {

  private val store = Store(
    AppSettingsState(
      Recipient.self(),
      0,
      SignalStore.inAppPayments.getExpiredGiftBadge() != null,
      SignalStore.inAppPayments.isLikelyASustainer() || InAppDonations.hasAtLeastOnePaymentMethodAvailable(),
      TextSecurePreferences.isUnauthorizedReceived(AppDependencies.application) || !SignalStore.account.isRegistered,
      SignalStore.misc.isClientDeprecated
    )
  )

  private val unreadPaymentsLiveData = UnreadPaymentsLiveData()
  private val selfLiveData: LiveData<Recipient> = Recipient.self().live().liveData
  private val disposables = CompositeDisposable()

  val state: LiveData<AppSettingsState> = store.stateLiveData

  init {
    store.update(unreadPaymentsLiveData) { payments, state -> state.copy(unreadPaymentsCount = payments.map { it.unreadCount }.orElse(0)) }
    store.update(selfLiveData) { self, state -> state.copy(self = self) }

    disposables += RecurringInAppPaymentRepository.getActiveSubscription(InAppPaymentSubscriberRecord.Type.DONATION).subscribeBy(
      onSuccess = { activeSubscription ->
        store.update { state ->
          state.copy(allowUserToGoToDonationManagementScreen = activeSubscription.isActive || InAppDonations.hasAtLeastOnePaymentMethodAvailable())
        }
      },
      onError = {}
    )
  }

  override fun onCleared() {
    disposables.clear()
  }

  fun refreshDeprecatedOrUnregistered() {
    store.update {
      it.copy(
        clientDeprecated = SignalStore.misc.isClientDeprecated,
        userUnregistered = TextSecurePreferences.isUnauthorizedReceived(AppDependencies.application) || !SignalStore.account.isRegistered
      )
    }
  }

  fun refreshExpiredGiftBadge() {
    store.update { it.copy(hasExpiredGiftBadge = SignalStore.inAppPayments.getExpiredGiftBadge() != null) }
  }
}
