/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.banner.banners

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.flow.Flow
import org.gfs.chat.R
import org.gfs.chat.banner.Banner
import org.gfs.chat.banner.ui.compose.Action
import org.gfs.chat.banner.ui.compose.DefaultBanner
import org.gfs.chat.banner.ui.compose.Importance
import org.gfs.chat.contacts.sync.CdsTemporaryErrorBottomSheet
import org.gfs.chat.keyvalue.SignalStore

class CdsTemporaryErrorBanner(private val fragmentManager: FragmentManager) : Banner() {
  private val timeUntilUnblock = SignalStore.misc.cdsBlockedUtil - System.currentTimeMillis()

  override val enabled: Boolean = SignalStore.misc.isCdsBlocked && timeUntilUnblock < CdsPermanentErrorBanner.PERMANENT_TIME_CUTOFF

  @Composable
  override fun DisplayBanner() {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.reminder_cds_warning_body),
      importance = Importance.ERROR,
      actions = listOf(
        Action(R.string.reminder_cds_warning_learn_more) {
          CdsTemporaryErrorBottomSheet.show(fragmentManager)
        }
      )
    )
  }

  companion object {

    @JvmStatic
    fun createFlow(fragmentManager: FragmentManager): Flow<CdsTemporaryErrorBanner> = createAndEmit {
      CdsTemporaryErrorBanner(fragmentManager)
    }
  }
}
