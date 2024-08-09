/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.components.settings.app.internal.search

import org.gfs.chat.groups.GroupId
import org.gfs.chat.recipients.RecipientId

data class InternalSearchResult(
  val name: String,
  val id: RecipientId,
  val aci: String? = null,
  val pni: String? = null,
  val groupId: GroupId? = null
)
