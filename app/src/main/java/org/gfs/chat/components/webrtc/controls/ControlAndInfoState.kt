/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.components.webrtc.controls

import androidx.compose.runtime.Immutable
import org.gfs.chat.database.CallLinkTable

@Immutable
data class ControlAndInfoState(
  val callLink: CallLinkTable.CallLink? = null,
  val resetScrollState: Long = 0
) {
  fun isSelfAdmin(): Boolean {
    return callLink?.credentials?.adminPassBytes != null
  }
}
