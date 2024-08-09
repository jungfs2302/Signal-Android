package org.gfs.chat.stories.tabs

import io.reactivex.rxjava3.core.Flowable
import org.gfs.chat.database.RxDatabaseObserver
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.recipients.Recipient

class ConversationListTabRepository {

  fun getNumberOfUnreadMessages(): Flowable<Long> {
    return RxDatabaseObserver.conversationList.map { SignalDatabase.threads.getUnreadMessageCount() }
  }

  fun getNumberOfUnseenStories(): Flowable<Long> {
    return RxDatabaseObserver.conversationList.map {
      SignalDatabase
        .messages
        .getUnreadStoryThreadRecipientIds()
        .map { Recipient.resolved(it) }
        .filterNot { it.shouldHideStory }
        .size
        .toLong()
    }
  }

  fun getHasFailedOutgoingStories(): Flowable<Boolean> {
    return RxDatabaseObserver.conversationList.map { SignalDatabase.messages.hasFailedOutgoingStory() }
  }

  fun getNumberOfUnseenCalls(): Flowable<Long> {
    return RxDatabaseObserver.conversationList.map { SignalDatabase.calls.getUnreadMissedCallCount() }
  }
}
