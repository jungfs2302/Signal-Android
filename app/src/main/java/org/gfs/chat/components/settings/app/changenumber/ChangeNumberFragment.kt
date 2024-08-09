/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.components.settings.app.changenumber

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import org.signal.core.util.logging.Log
import org.gfs.chat.LoggingFragment
import org.gfs.chat.R
import org.gfs.chat.components.ViewBinderDelegate
import org.gfs.chat.databinding.FragmentChangePhoneNumberBinding
import org.gfs.chat.util.navigation.safeNavigate

/**
 * Screen used to educate the user about what they're about to do (change their phone number)
 */
class ChangeNumberFragment : LoggingFragment(R.layout.fragment_change_phone_number) {

  companion object {
    private val TAG = Log.tag(ChangeNumberFragment::class.java)
  }

  private val binding: FragmentChangePhoneNumberBinding by ViewBinderDelegate(FragmentChangePhoneNumberBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

    binding.changePhoneNumberContinue.setOnClickListener {
      findNavController().safeNavigate(ChangeNumberFragmentDirections.actionChangePhoneNumberFragmentToEnterPhoneNumberChangeFragment())
    }
  }
}
