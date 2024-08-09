/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.badges.self.expired

import androidx.annotation.StringRes
import org.gfs.chat.badges.models.Badge

data class MonthlyDonationCanceledState(
  val loadState: LoadState = LoadState.LOADING,
  val badge: Badge? = null,
  @StringRes val errorMessage: Int = -1
) {
  enum class LoadState {
    LOADING,
    READY,
    FAILED
  }
}
