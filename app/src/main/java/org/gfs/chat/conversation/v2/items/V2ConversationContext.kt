/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.conversation.v2.items

import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.RequestManager
import org.gfs.chat.conversation.ConversationAdapter
import org.gfs.chat.conversation.ConversationItemDisplayMode
import org.gfs.chat.conversation.colors.Colorizer
import org.gfs.chat.conversation.mutiselect.MultiselectPart
import org.gfs.chat.database.model.MessageRecord

/**
 * Describes the Adapter "context" that would normally have been
 * visible to an inner class.
 */
interface V2ConversationContext {
  val lifecycleOwner: LifecycleOwner
  val requestManager: RequestManager
  val displayMode: ConversationItemDisplayMode
  val clickListener: ConversationAdapter.ItemClickListener
  val selectedItems: Set<MultiselectPart>
  val isMessageRequestAccepted: Boolean
  val searchQuery: String?
  val isParentInScroll: Boolean

  fun getChatColorsData(): ChatColorsDrawable.ChatColorsData

  fun onStartExpirationTimeout(messageRecord: MessageRecord)

  fun hasWallpaper(): Boolean
  fun getColorizer(): Colorizer
  fun getNextMessage(adapterPosition: Int): MessageRecord?
  fun getPreviousMessage(adapterPosition: Int): MessageRecord?
}
