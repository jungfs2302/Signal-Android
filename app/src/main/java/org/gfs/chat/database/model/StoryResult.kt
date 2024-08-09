package org.gfs.chat.database.model

import org.gfs.chat.recipients.RecipientId

class StoryResult(
  val recipientId: RecipientId,
  val messageId: Long,
  val messageSentTimestamp: Long,
  val isOutgoing: Boolean
)
