package org.gfs.chat.stories.viewer.reply.direct

import org.gfs.chat.database.model.MessageRecord
import org.gfs.chat.recipients.Recipient

data class StoryDirectReplyState(
  val groupDirectReplyRecipient: Recipient? = null,
  val storyRecord: MessageRecord? = null
)
