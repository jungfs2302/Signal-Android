/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.banner.banners

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.gfs.chat.R
import org.gfs.chat.banner.Banner
import org.gfs.chat.banner.ui.compose.Action
import org.gfs.chat.banner.ui.compose.DefaultBanner
import org.gfs.chat.banner.ui.compose.Importance
import org.gfs.chat.util.PlayStoreUtil

class EnclaveFailureBanner(enclaveFailed: Boolean, private val context: Context) : Banner() {
  override val enabled: Boolean = enclaveFailed

  @Composable
  override fun DisplayBanner() {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.EnclaveFailureReminder_update_signal),
      importance = Importance.ERROR,
      actions = listOf(
        Action(R.string.ExpiredBuildReminder_update_now) {
          PlayStoreUtil.openPlayStoreOrOurApkDownloadPage(context)
        }
      )
    )
  }

  companion object {
    @JvmStatic
    fun Flow<Boolean>.mapBooleanFlowToBannerFlow(context: Context): Flow<EnclaveFailureBanner> {
      return map { EnclaveFailureBanner(it, context) }
    }
  }
}
