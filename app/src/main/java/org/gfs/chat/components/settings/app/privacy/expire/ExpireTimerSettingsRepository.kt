package org.gfs.chat.components.settings.app.privacy.expire

import android.content.Context
import androidx.annotation.WorkerThread
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.database.ThreadTable
import org.gfs.chat.groups.GroupChangeException
import org.gfs.chat.groups.GroupManager
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.mms.OutgoingMessage
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.sms.MessageSender
import org.gfs.chat.storage.StorageSyncHelper
import java.io.IOException

private val TAG: String = Log.tag(ExpireTimerSettingsRepository::class.java)

/**
 * Provide operations to set expire timer for individuals and groups.
 */
class ExpireTimerSettingsRepository(val context: Context) {

  fun setExpiration(recipientId: RecipientId, newExpirationTime: Int, consumer: (Result<Int>) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      val recipient = Recipient.resolved(recipientId)
      if (recipient.groupId.isPresent && recipient.groupId.get().isPush) {
        try {
          GroupManager.updateGroupTimer(context, recipient.groupId.get().requirePush(), newExpirationTime)
          consumer.invoke(Result.success(newExpirationTime))
        } catch (e: GroupChangeException) {
          Log.w(TAG, e)
          consumer.invoke(Result.failure(e))
        } catch (e: IOException) {
          Log.w(TAG, e)
          consumer.invoke(Result.failure(e))
        }
      } else {
        SignalDatabase.recipients.setExpireMessages(recipientId, newExpirationTime)
        val outgoingMessage = OutgoingMessage.expirationUpdateMessage(Recipient.resolved(recipientId), System.currentTimeMillis(), newExpirationTime * 1000L)
        MessageSender.send(context, outgoingMessage, getThreadId(recipientId), MessageSender.SendType.SIGNAL, null, null)
        consumer.invoke(Result.success(newExpirationTime))
      }
    }
  }

  fun setUniversalExpireTimerSeconds(newExpirationTime: Int, onDone: () -> Unit) {
    SignalExecutors.BOUNDED.execute {
      SignalStore.settings.universalExpireTimer = newExpirationTime
      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      StorageSyncHelper.scheduleSyncForDataChange()
      onDone.invoke()
    }
  }

  @WorkerThread
  private fun getThreadId(recipientId: RecipientId): Long {
    val threadTable: ThreadTable = SignalDatabase.threads
    val recipient: Recipient = Recipient.resolved(recipientId)
    return threadTable.getOrCreateThreadIdFor(recipient)
  }
}
