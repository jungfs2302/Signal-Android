/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.banner.banners

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.Flow
import org.gfs.chat.R
import org.gfs.chat.banner.Banner
import org.gfs.chat.banner.ui.compose.Action
import org.gfs.chat.banner.ui.compose.DefaultBanner
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.util.PowerManagerCompat
import org.gfs.chat.util.ServiceUtil
import org.gfs.chat.util.TextSecurePreferences

@RequiresApi(23)
class DozeBanner(private val context: Context) : Banner() {
  override val enabled: Boolean = !SignalStore.account.fcmEnabled && !TextSecurePreferences.hasPromptedOptimizeDoze(context) && Build.VERSION.SDK_INT >= 23 && !ServiceUtil.getPowerManager(context).isIgnoringBatteryOptimizations(context.packageName)

  @Composable
  override fun DisplayBanner() {
    DefaultBanner(
      title = stringResource(id = R.string.DozeReminder_optimize_for_missing_play_services),
      body = stringResource(id = R.string.DozeReminder_this_device_does_not_support_play_services_tap_to_disable_system_battery),
      actions = listOf(
        Action(android.R.string.ok) {
          TextSecurePreferences.setPromptedOptimizeDoze(context, true)
          PowerManagerCompat.requestIgnoreBatteryOptimizations(context)
        }
      ),
      onDismissListener = {
        TextSecurePreferences.setPromptedOptimizeDoze(context, true)
      }
    )
  }

  companion object {

    @JvmStatic
    fun createFlow(context: Context): Flow<DozeBanner> = createAndEmit {
      if (Build.VERSION.SDK_INT >= 23) {
        DozeBanner(context)
      } else {
        null
      }
    }
  }
}
