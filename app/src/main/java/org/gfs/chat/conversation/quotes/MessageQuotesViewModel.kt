package org.gfs.chat.conversation.quotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.gfs.chat.conversation.ConversationMessage
import org.gfs.chat.conversation.colors.GroupAuthorNameColorHelper
import org.gfs.chat.conversation.colors.NameColor
import org.gfs.chat.database.model.MessageId
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId

class MessageQuotesViewModel(
  application: Application,
  private val messageId: MessageId,
  private val conversationRecipientId: RecipientId
) : AndroidViewModel(application) {

  private val groupAuthorNameColorHelper = GroupAuthorNameColorHelper()
  private val repository = MessageQuotesRepository()

  fun getMessages(): Observable<List<ConversationMessage>> {
    return repository
      .getMessagesInQuoteChain(getApplication(), messageId)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
  }

  fun getNameColorsMap(): Observable<Map<RecipientId, NameColor>> {
    return Observable.just(conversationRecipientId)
      .map { conversationRecipientId ->
        val conversationRecipient = Recipient.resolved(conversationRecipientId)

        if (conversationRecipient.groupId.isPresent) {
          groupAuthorNameColorHelper.getColorMap(conversationRecipient.groupId.get())
        } else {
          emptyMap()
        }
      }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
  }

  class Factory(private val application: Application, private val messageId: MessageId, private val conversationRecipientId: RecipientId) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return modelClass.cast(MessageQuotesViewModel(application, messageId, conversationRecipientId)) as T
    }
  }
}
