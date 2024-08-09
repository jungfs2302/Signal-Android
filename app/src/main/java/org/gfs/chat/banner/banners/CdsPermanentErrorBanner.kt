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
import org.gfs.chat.contacts.sync.CdsPermanentErrorBottomSheet
import org.gfs.chat.keyvalue.SignalStore
import kotlin.time.Duration.Companion.days

class CdsPermanentErrorBanner(private val fragmentManager: FragmentManager) : Banner() {
  private val timeUntilUnblock = SignalStore.misc.cdsBlockedUtil - System.currentTimeMillis()

  override val enabled: Boolean = SignalStore.misc.isCdsBlocked && timeUntilUnblock >= PERMANENT_TIME_CUTOFF

  @Composable
  override fun DisplayBanner() {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.reminder_cds_permanent_error_body),
      importance = Importance.ERROR,
      actions = listOf(
        Action(R.string.reminder_cds_permanent_error_learn_more) {
          CdsPermanentErrorBottomSheet.show(fragmentManager)
        }
      )
    )
  }

  companion object {

    /**
     * Even if we're not truly "permanently blocked", if the time until we're unblocked is long enough, we'd rather show the permanent error message than
     * telling the user to wait for 3 months or something.
     */
    val PERMANENT_TIME_CUTOFF = 30.days.inWholeMilliseconds

    @JvmStatic
    fun createFlow(fragmentManager: FragmentManager): Flow<CdsPermanentErrorBanner> = createAndEmit {
      CdsPermanentErrorBanner(fragmentManager)
    }
  }
}
