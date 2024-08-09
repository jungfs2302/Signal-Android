/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.banner.banners

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.Flow
import org.gfs.chat.R
import org.gfs.chat.banner.Banner
import org.gfs.chat.banner.ui.compose.Action
import org.gfs.chat.banner.ui.compose.DefaultBanner
import org.gfs.chat.keyvalue.SignalStore

class BubbleOptOutBanner(inBubble: Boolean, private val actionListener: (Boolean) -> Unit) : Banner() {

  override val enabled: Boolean = inBubble && !SignalStore.tooltips.hasSeenBubbleOptOutTooltip() && Build.VERSION.SDK_INT > 29

  @Composable
  override fun DisplayBanner() {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.BubbleOptOutTooltip__description),
      actions = listOf(
        Action(R.string.BubbleOptOutTooltip__turn_off) {
          actionListener(true)
        },
        Action(R.string.BubbleOptOutTooltip__not_now) {
          actionListener(false)
        }
      )
    )
  }

  companion object {
    @JvmStatic
    fun createFlow(inBubble: Boolean, actionListener: (Boolean) -> Unit): Flow<BubbleOptOutBanner> = createAndEmit {
      BubbleOptOutBanner(inBubble, actionListener)
    }
  }
}
