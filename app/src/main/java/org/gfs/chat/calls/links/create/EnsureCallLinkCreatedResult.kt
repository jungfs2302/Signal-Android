/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.calls.links.create

import org.gfs.chat.recipients.Recipient
import org.gfs.chat.service.webrtc.links.CreateCallLinkResult

sealed interface EnsureCallLinkCreatedResult {
  data class Success(val recipient: Recipient) : EnsureCallLinkCreatedResult
  data class Failure(val failure: CreateCallLinkResult.Failure) : EnsureCallLinkCreatedResult
}
