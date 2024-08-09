package org.gfs.chat.components.settings.app.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.util.TextSecurePreferences
import org.gfs.chat.util.livedata.Store

class AccountSettingsViewModel : ViewModel() {
  private val store: Store<AccountSettingsState> = Store(getCurrentState())

  val state: LiveData<AccountSettingsState> = store.stateLiveData

  fun refreshState() {
    store.update { getCurrentState() }
  }

  private fun getCurrentState(): AccountSettingsState {
    return AccountSettingsState(
      hasPin = SignalStore.svr.hasPin() && !SignalStore.svr.hasOptedOut(),
      pinRemindersEnabled = SignalStore.pin.arePinRemindersEnabled(),
      registrationLockEnabled = SignalStore.svr.isRegistrationLockEnabled,
      userUnregistered = TextSecurePreferences.isUnauthorizedReceived(AppDependencies.application),
      clientDeprecated = SignalStore.misc.isClientDeprecated
    )
  }
}
