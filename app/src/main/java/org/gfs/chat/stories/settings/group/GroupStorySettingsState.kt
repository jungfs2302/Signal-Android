package org.gfs.chat.stories.settings.group

import org.gfs.chat.recipients.Recipient

data class GroupStorySettingsState(
  val name: String = "",
  val members: List<Recipient> = emptyList(),
  val removed: Boolean = false
)
