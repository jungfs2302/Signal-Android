package org.gfs.chat.components.settings.app.privacy.pnp

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobs.ProfileUploadJob
import org.gfs.chat.jobs.RefreshAttributesJob
import org.gfs.chat.jobs.RefreshOwnProfileJob
import org.gfs.chat.keyvalue.PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode
import org.gfs.chat.keyvalue.PhoneNumberPrivacyValues.PhoneNumberSharingMode
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.storage.StorageSyncHelper

class PhoneNumberPrivacySettingsViewModel : ViewModel() {

  private val _state = mutableStateOf(
    PhoneNumberPrivacySettingsState(
      phoneNumberSharing = SignalStore.phoneNumberPrivacy.isPhoneNumberSharingEnabled,
      discoverableByPhoneNumber = SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode != PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
    )
  )

  val state: State<PhoneNumberPrivacySettingsState> = _state

  fun setNobodyCanSeeMyNumber() {
    setPhoneNumberSharingEnabled(false)
  }

  fun setEveryoneCanSeeMyNumber() {
    setPhoneNumberSharingEnabled(true)
    setDiscoverableByPhoneNumber(true)
  }

  fun setNobodyCanFindMeByMyNumber() {
    setDiscoverableByPhoneNumber(false)
  }

  fun setEveryoneCanFindMeByMyNumber() {
    setDiscoverableByPhoneNumber(true)
  }

  private fun setPhoneNumberSharingEnabled(phoneNumberSharingEnabled: Boolean) {
    SignalStore.phoneNumberPrivacy.phoneNumberSharingMode = if (phoneNumberSharingEnabled) PhoneNumberSharingMode.EVERYBODY else PhoneNumberSharingMode.NOBODY
    SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
    StorageSyncHelper.scheduleSyncForDataChange()
    AppDependencies.jobManager.add(ProfileUploadJob())
    refresh()
  }

  private fun setDiscoverableByPhoneNumber(discoverable: Boolean) {
    SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode = if (discoverable) PhoneNumberDiscoverabilityMode.DISCOVERABLE else PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
    StorageSyncHelper.scheduleSyncForDataChange()
    AppDependencies.jobManager.startChain(RefreshAttributesJob()).then(RefreshOwnProfileJob()).enqueue()
    refresh()
  }

  fun refresh() {
    _state.value = PhoneNumberPrivacySettingsState(
      phoneNumberSharing = SignalStore.phoneNumberPrivacy.isPhoneNumberSharingEnabled,
      discoverableByPhoneNumber = SignalStore.phoneNumberPrivacy.phoneNumberDiscoverabilityMode != PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
    )
  }
}
