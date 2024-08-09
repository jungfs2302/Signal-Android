package org.gfs.chat.stories.viewer.views

import org.gfs.chat.recipients.Recipient

data class StoryViewItemData(
  val recipient: Recipient,
  val timeViewedInMillis: Long
)
