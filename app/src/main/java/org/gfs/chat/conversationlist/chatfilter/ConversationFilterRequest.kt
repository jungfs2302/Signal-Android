package org.gfs.chat.conversationlist.chatfilter

import org.gfs.chat.conversationlist.model.ConversationFilter

data class ConversationFilterRequest(
  val filter: ConversationFilter,
  val source: ConversationFilterSource
)
