package org.gfs.chat.stories.viewer.reply.direct

import android.content.Context
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.database.model.MessageRecord
import org.gfs.chat.database.model.MmsMessageRecord
import org.gfs.chat.database.model.ParentStoryId
import org.gfs.chat.database.model.databaseprotos.BodyRangeList
import org.gfs.chat.mms.OutgoingMessage
import org.gfs.chat.mms.QuoteModel
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.sms.MessageSender
import java.util.concurrent.TimeUnit

class StoryDirectReplyRepository(context: Context) {

  private val context = context.applicationContext

  fun getStoryPost(storyId: Long): Single<MessageRecord> {
    return Single.fromCallable {
      SignalDatabase.messages.getMessageRecord(storyId)
    }.subscribeOn(Schedulers.io())
  }

  fun send(storyId: Long, groupDirectReplyRecipientId: RecipientId?, body: CharSequence, bodyRangeList: BodyRangeList?, isReaction: Boolean): Completable {
    return Completable.create { emitter ->
      val message = SignalDatabase.messages.getMessageRecord(storyId) as MmsMessageRecord
      val (recipient, threadId) = if (groupDirectReplyRecipientId == null) {
        message.fromRecipient to message.threadId
      } else {
        val resolved = Recipient.resolved(groupDirectReplyRecipientId)
        resolved to SignalDatabase.threads.getOrCreateThreadIdFor(resolved)
      }

      val quoteAuthor: Recipient = message.fromRecipient

      MessageSender.send(
        context,
        OutgoingMessage(
          threadRecipient = recipient,
          body = body.toString(),
          sentTimeMillis = System.currentTimeMillis(),
          expiresIn = TimeUnit.SECONDS.toMillis(recipient.expiresInSeconds.toLong()),
          parentStoryId = ParentStoryId.DirectReply(storyId),
          isStoryReaction = isReaction,
          outgoingQuote = QuoteModel(message.dateSent, quoteAuthor.id, message.body, false, message.slideDeck.asAttachments(), null, QuoteModel.Type.NORMAL, message.messageRanges),
          bodyRanges = bodyRangeList,
          isSecure = true
        ),
        threadId,
        MessageSender.SendType.SIGNAL,
        null
      ) {
        emitter.onComplete()
      }
    }.subscribeOn(Schedulers.io())
  }
}
