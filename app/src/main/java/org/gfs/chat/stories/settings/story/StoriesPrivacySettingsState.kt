package org.gfs.chat.stories.settings.story

import org.gfs.chat.contacts.paged.ContactSearchData

data class StoriesPrivacySettingsState(
  val areStoriesEnabled: Boolean,
  val areViewReceiptsEnabled: Boolean,
  val isUpdatingEnabledState: Boolean = false,
  val storyContactItems: List<ContactSearchData> = emptyList(),
  val userHasStories: Boolean = false
)
