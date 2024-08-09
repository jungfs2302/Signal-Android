package org.gfs.chat.stories.settings.select

import org.gfs.chat.database.model.DistributionListId
import org.gfs.chat.database.model.DistributionListRecord
import org.gfs.chat.recipients.RecipientId

data class BaseStoryRecipientSelectionState(
  val distributionListId: DistributionListId?,
  val privateStory: DistributionListRecord? = null,
  val selection: Set<RecipientId> = emptySet()
)
