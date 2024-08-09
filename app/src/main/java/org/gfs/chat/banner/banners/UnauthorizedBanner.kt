/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.banner.banners

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.Flow
import org.gfs.chat.R
import org.gfs.chat.banner.Banner
import org.gfs.chat.banner.ui.compose.Action
import org.gfs.chat.banner.ui.compose.DefaultBanner
import org.gfs.chat.banner.ui.compose.Importance
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.registration.ui.RegistrationActivity
import org.gfs.chat.util.TextSecurePreferences

class UnauthorizedBanner(val context: Context) : Banner() {

  override val enabled = TextSecurePreferences.isUnauthorizedReceived(context) || !SignalStore.account.isRegistered

  @Composable
  override fun DisplayBanner() {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.UnauthorizedReminder_this_is_likely_because_you_registered_your_phone_number_with_Signal_on_a_different_device),
      importance = Importance.ERROR,
      actions = listOf(
        Action(R.string.UnauthorizedReminder_reregister_action) {
          val registrationIntent = RegistrationActivity.newIntentForReRegistration(context)
          context.startActivity(registrationIntent)
        }
      )
    )
  }

  companion object {

    @JvmStatic
    fun createFlow(context: Context): Flow<UnauthorizedBanner> = createAndEmit {
      UnauthorizedBanner(context)
    }
  }
}
