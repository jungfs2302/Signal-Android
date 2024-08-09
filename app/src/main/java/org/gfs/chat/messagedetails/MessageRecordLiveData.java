package org.gfs.chat.messagedetails;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import org.signal.core.util.concurrent.SignalExecutors;
import org.gfs.chat.database.DatabaseObserver;
import org.gfs.chat.database.NoSuchMessageException;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.database.model.MessageId;
import org.gfs.chat.database.model.MessageRecord;
import org.gfs.chat.dependencies.AppDependencies;

final class MessageRecordLiveData extends LiveData<MessageRecord> {

  private final DatabaseObserver.Observer observer;
  private final MessageId                 messageId;

  MessageRecordLiveData(MessageId messageId) {
    this.messageId = messageId;
    this.observer  = this::retrieveMessageRecordActual;
  }

  @Override
  protected void onActive() {
    SignalExecutors.BOUNDED_IO.execute(this::retrieveMessageRecordActual);
  }

  @Override
  protected void onInactive() {
    AppDependencies.getDatabaseObserver().unregisterObserver(observer);
  }

  @WorkerThread
  private synchronized void retrieveMessageRecordActual() {
    try {
      MessageRecord record = SignalDatabase.messages().getMessageRecord(messageId.getId());

      if (record.isPaymentNotification()) {
        record = SignalDatabase.payments().updateMessageWithPayment(record);
      }

      postValue(record);
      AppDependencies.getDatabaseObserver().registerVerboseConversationObserver(record.getThreadId(), observer);
    } catch (NoSuchMessageException ignored) {
      postValue(null);
    }
  }
}
