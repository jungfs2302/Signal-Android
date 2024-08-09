package org.gfs.chat.stories.my

import org.gfs.chat.conversation.ConversationMessage
import org.gfs.chat.database.model.MessageRecord

data class MyStoriesState(
  val distributionSets: List<DistributionSet> = emptyList()
) {

  data class DistributionSet(
    val label: String?,
    val stories: List<DistributionStory>
  )

  data class DistributionStory(
    val message: ConversationMessage,
    val views: Int
  ) {
    val messageRecord: MessageRecord = message.messageRecord
  }
}
