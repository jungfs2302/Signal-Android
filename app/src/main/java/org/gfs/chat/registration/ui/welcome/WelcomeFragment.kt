/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.registration.ui.welcome

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.signal.core.util.logging.Log
import org.gfs.chat.LoggingFragment
import org.gfs.chat.R
import org.gfs.chat.components.ViewBinderDelegate
import org.gfs.chat.databinding.FragmentRegistrationWelcomeBinding
import org.gfs.chat.permissions.Permissions
import org.gfs.chat.registration.fragments.RegistrationViewDelegate.setDebugLogSubmitMultiTapView
import org.gfs.chat.registration.fragments.WelcomePermissions
import org.gfs.chat.registration.ui.RegistrationCheckpoint
import org.gfs.chat.registration.ui.RegistrationViewModel
import org.gfs.chat.registration.ui.grantpermissions.GrantPermissionsFragment
import org.gfs.chat.restore.RestoreActivity
import org.gfs.chat.util.BackupUtil
import org.gfs.chat.util.CommunicationActions
import org.gfs.chat.util.RemoteConfig
import org.gfs.chat.util.TextSecurePreferences
import org.gfs.chat.util.navigation.safeNavigate
import org.gfs.chat.util.visible

/**
 * First screen that is displayed on the very first app launch.
 */
class WelcomeFragment : LoggingFragment(R.layout.fragment_registration_welcome) {
  private val sharedViewModel by activityViewModels<RegistrationViewModel>()
  private val binding: FragmentRegistrationWelcomeBinding by ViewBinderDelegate(FragmentRegistrationWelcomeBinding::bind)

  private val launchRestoreActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
    when (val resultCode = result.resultCode) {
      Activity.RESULT_OK -> {
        sharedViewModel.onBackupSuccessfullyRestored()
        findNavController().safeNavigate(WelcomeFragmentDirections.actionGoToRegistration())
      }
      Activity.RESULT_CANCELED -> {
        Log.w(TAG, "Backup restoration canceled.")
      }
      else -> Log.w(TAG, "Backup restoration activity ended with unknown result code: $resultCode")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setDebugLogSubmitMultiTapView(binding.image)
    setDebugLogSubmitMultiTapView(binding.title)
    binding.welcomeContinueButton.setOnClickListener { onContinueClicked() }
    binding.welcomeTermsButton.setOnClickListener { onTermsClicked() }
    binding.welcomeTransferOrRestore.setOnClickListener { onTransferOrRestoreClicked() }
    binding.welcomeTransferOrRestore.visible = !RemoteConfig.restoreAfterRegistration
  }

  private fun onContinueClicked() {
    TextSecurePreferences.setHasSeenWelcomeScreen(requireContext(), true)
    if (Permissions.isRuntimePermissionsRequired() && !hasAllPermissions()) {
      findNavController().safeNavigate(WelcomeFragmentDirections.actionWelcomeFragmentToGrantPermissionsFragment(GrantPermissionsFragment.WelcomeAction.CONTINUE))
    } else {
      sharedViewModel.maybePrefillE164(requireContext())
      findNavController().safeNavigate(WelcomeFragmentDirections.actionSkipRestore())
    }
  }

  private fun hasAllPermissions(): Boolean {
    val isUserSelectionRequired = BackupUtil.isUserSelectionRequired(requireContext())
    return WelcomePermissions.getWelcomePermissions(isUserSelectionRequired).all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }
  }

  private fun onTermsClicked() {
    CommunicationActions.openBrowserLink(requireContext(), TERMS_AND_CONDITIONS_URL)
  }

  private fun onTransferOrRestoreClicked() {
    if (Permissions.isRuntimePermissionsRequired() && !hasAllPermissions()) {
      findNavController().safeNavigate(WelcomeFragmentDirections.actionWelcomeFragmentToGrantPermissionsFragment(GrantPermissionsFragment.WelcomeAction.RESTORE_BACKUP))
    } else {
      sharedViewModel.setRegistrationCheckpoint(RegistrationCheckpoint.PERMISSIONS_GRANTED)

      val restoreIntent = RestoreActivity.getIntentForTransferOrRestore(requireActivity())
      launchRestoreActivity.launch(restoreIntent)
    }
  }

  companion object {
    private val TAG = Log.tag(WelcomeFragment::class.java)
    private const val TERMS_AND_CONDITIONS_URL = "https://signal.org/legal"
  }
}
