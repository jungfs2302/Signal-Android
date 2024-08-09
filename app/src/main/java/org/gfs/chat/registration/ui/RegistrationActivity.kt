/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.registration.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.ActivityNavigator
import org.signal.core.util.logging.Log
import org.gfs.chat.BaseActivity
import org.gfs.chat.MainActivity
import org.gfs.chat.R
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.lock.v2.CreateSvrPinActivity
import org.gfs.chat.pin.PinRestoreActivity
import org.gfs.chat.profiles.AvatarHelper
import org.gfs.chat.profiles.edit.CreateProfileActivity
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.registration.SmsRetrieverReceiver
import org.gfs.chat.registration.ui.restore.RemoteRestoreActivity
import org.gfs.chat.util.DynamicNoActionBarTheme
import org.gfs.chat.util.RemoteConfig

/**
 * Activity to hold the entire registration process.
 */
class RegistrationActivity : BaseActivity() {

  private val TAG = Log.tag(RegistrationActivity::class.java)

  private val dynamicTheme = DynamicNoActionBarTheme()
  val sharedViewModel: RegistrationViewModel by viewModels()

  private var smsRetrieverReceiver: SmsRetrieverReceiver? = null

  init {
    lifecycle.addObserver(SmsRetrieverObserver())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    dynamicTheme.onCreate(this)

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_registration_navigation_v2)

    sharedViewModel.isReregister = intent.getBooleanExtra(RE_REGISTRATION_EXTRA, false)

    sharedViewModel.checkpoint.observe(this) {
      if (it >= RegistrationCheckpoint.LOCAL_REGISTRATION_COMPLETE) {
        handleSuccessfulVerify()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    dynamicTheme.onResume(this)
  }

  private fun handleSuccessfulVerify() {
    if (SignalStore.misc.hasLinkedDevices) {
      SignalStore.misc.shouldShowLinkedDevicesReminder = sharedViewModel.isReregister
    }

    if (SignalStore.storageService.needsAccountRestore()) {
      Log.i(TAG, "Performing pin restore.")
      startActivity(Intent(this, PinRestoreActivity::class.java))
      finish()
    } else {
      val isProfileNameEmpty = Recipient.self().profileName.isEmpty
      val isAvatarEmpty = !AvatarHelper.hasAvatar(this, Recipient.self().id)
      val needsProfile = isProfileNameEmpty || isAvatarEmpty
      val needsPin = !sharedViewModel.hasPin()

      Log.i(TAG, "Pin restore flow not required. Profile name: $isProfileNameEmpty | Profile avatar: $isAvatarEmpty | Needs PIN: $needsPin")

      if (!needsProfile && !needsPin) {
        sharedViewModel.completeRegistration()
      }
      sharedViewModel.setInProgress(false)

      val startIntent = MainActivity.clearTop(this).apply {
        if (needsPin) {
          putExtra("next_intent", CreateSvrPinActivity.getIntentForPinCreate(this@RegistrationActivity))
        } else if (!SignalStore.registration.hasSkippedTransferOrRestore() && RemoteConfig.messageBackups) {
          putExtra("next_intent", RemoteRestoreActivity.getIntent(this@RegistrationActivity))
        } else if (needsProfile) {
          putExtra("next_intent", CreateProfileActivity.getIntentForUserProfile(this@RegistrationActivity))
        }
      }

      Log.d(TAG, "Launching ${startIntent.component}")
      startActivity(startIntent)
      finish()
      ActivityNavigator.applyPopAnimationsToPendingTransition(this)
    }
  }

  private inner class SmsRetrieverObserver : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
      smsRetrieverReceiver = SmsRetrieverReceiver(application)
      smsRetrieverReceiver?.registerReceiver()
    }

    override fun onDestroy(owner: LifecycleOwner) {
      smsRetrieverReceiver?.unregisterReceiver()
      smsRetrieverReceiver = null
    }
  }

  companion object {
    const val RE_REGISTRATION_EXTRA: String = "re_registration"

    @JvmStatic
    fun newIntentForNewRegistration(context: Context, originalIntent: Intent): Intent {
      return Intent(context, RegistrationActivity::class.java).apply {
        putExtra(RE_REGISTRATION_EXTRA, false)
        setData(originalIntent.data)
      }
    }

    @JvmStatic
    fun newIntentForReRegistration(context: Context): Intent {
      return Intent(context, RegistrationActivity::class.java).apply {
        putExtra(RE_REGISTRATION_EXTRA, true)
      }
    }
  }
}
