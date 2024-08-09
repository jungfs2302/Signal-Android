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
import org.gfs.chat.banner.ui.compose.DefaultBanner
import org.gfs.chat.banner.ui.compose.Importance
import org.gfs.chat.util.TextSecurePreferences

class ServiceOutageBanner(context: Context) : Banner() {

  override val enabled = TextSecurePreferences.getServiceOutage(context)

  @Composable
  override fun DisplayBanner() {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.reminder_header_service_outage_text),
      importance = Importance.ERROR
    )
  }

  companion object {

    @JvmStatic
    fun createFlow(context: Context): Flow<ServiceOutageBanner> = createAndEmit {
      ServiceOutageBanner(context)
    }
  }
}
