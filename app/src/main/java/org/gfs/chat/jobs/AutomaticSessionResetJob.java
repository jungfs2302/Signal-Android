package org.gfs.chat.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import org.gfs.chat.crypto.SealedSenderAccessUtil;
import org.gfs.chat.database.MessageTable;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.database.model.databaseprotos.DeviceLastResetTime;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobmanager.Job;
import org.gfs.chat.jobmanager.JsonJobData;
import org.gfs.chat.jobmanager.impl.DecryptionsDrainedConstraint;
import org.gfs.chat.notifications.v2.ConversationId;
import org.gfs.chat.recipients.Recipient;
import org.gfs.chat.recipients.RecipientId;
import org.gfs.chat.recipients.RecipientUtil;
import org.gfs.chat.util.RemoteConfig;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * - Archives the session associated with the specified device
 * - Inserts an error message in the conversation
 * - Sends a new, empty message to trigger a fresh session with the specified device
 *
 * This will only be run when all decryptions have finished, and there can only be one enqueued
 * per websocket drain cycle.
 */
public class AutomaticSessionResetJob extends BaseJob {

  private static final String TAG = Log.tag(AutomaticSessionResetJob.class);

  public static final String KEY = "AutomaticSessionResetJob";

  private static final String KEY_RECIPIENT_ID   =  "recipient_id";
  private static final String KEY_DEVICE_ID      =  "device_id";
  private static final String KEY_SENT_TIMESTAMP =  "sent_timestamp";

  private final RecipientId recipientId;
  private final int         deviceId;
  private final long        sentTimestamp;

  public AutomaticSessionResetJob(@NonNull RecipientId recipientId, int deviceId, long sentTimestamp) {
    this(new Parameters.Builder()
                       .setQueue(PushProcessMessageJob.getQueueName(recipientId))
                       .addConstraint(DecryptionsDrainedConstraint.KEY)
                       .setMaxInstancesForQueue(1)
                       .build(),
         recipientId,
         deviceId,
         sentTimestamp);
  }

  private AutomaticSessionResetJob(@NonNull Parameters parameters,
                                   @NonNull RecipientId recipientId,
                                   int deviceId,
                                   long sentTimestamp)
  {
    super(parameters);
    this.recipientId   = recipientId;
    this.deviceId      = deviceId;
    this.sentTimestamp = sentTimestamp;
  }

  @Override
  public @Nullable byte[] serialize() {
    return new JsonJobData.Builder().putString(KEY_RECIPIENT_ID, recipientId.serialize())
                                    .putInt(KEY_DEVICE_ID, deviceId)
                                    .putLong(KEY_SENT_TIMESTAMP, sentTimestamp)
                                    .serialize();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  protected void onRun() throws Exception {
    AppDependencies.getProtocolStore().aci().sessions().archiveSessions(recipientId, deviceId);
    SignalDatabase.senderKeyShared().deleteAllFor(recipientId);
    insertLocalMessage();

    if (RemoteConfig.automaticSessionReset()) {
      long                resetInterval      = TimeUnit.SECONDS.toMillis(RemoteConfig.automaticSessionResetIntervalSeconds());
      DeviceLastResetTime resetTimes         = SignalDatabase.recipients().getLastSessionResetTimes(recipientId);
      long                timeSinceLastReset = System.currentTimeMillis() - getLastResetTime(resetTimes, deviceId);

      Log.i(TAG, "DeviceId: " + deviceId + ", Reset interval: " + resetInterval + ", Time since last reset: " + timeSinceLastReset, true);

      if (timeSinceLastReset > resetInterval) {
        Log.i(TAG, "We're good! Sending a null message.", true);

        SignalDatabase.recipients().setLastSessionResetTime(recipientId, setLastResetTime(resetTimes, deviceId, System.currentTimeMillis()));
        Log.i(TAG, "Marked last reset time: " + System.currentTimeMillis(), true);

        sendNullMessage();
        Log.i(TAG, "Successfully sent!", true);
      } else {
        Log.w(TAG, "Too soon! Time since last reset: " + timeSinceLastReset, true);
      }
    } else {
      Log.w(TAG, "Automatic session reset send disabled!", true);
    }
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    return false;
  }

  @Override
  public void onFailure() {
  }

  private void insertLocalMessage() {
    MessageTable.InsertResult result = SignalDatabase.messages().insertChatSessionRefreshedMessage(recipientId, deviceId, sentTimestamp - 1);
    AppDependencies.getMessageNotifier().updateNotification(context, ConversationId.forConversation(result.getThreadId()));
  }

  private void sendNullMessage() throws IOException {
    Recipient recipient = Recipient.resolved(recipientId);

    if (recipient.isUnregistered()) {
      Log.w(TAG, recipient.getId() + " not registered!");
      return;
    }

    SignalServiceMessageSender messageSender = AppDependencies.getSignalServiceMessageSender();
    SignalServiceAddress       address       = RecipientUtil.toSignalServiceAddress(context, recipient);

    try {
      messageSender.sendNullMessage(address, SealedSenderAccessUtil.getSealedSenderAccessFor(recipient));
    } catch (UntrustedIdentityException e) {
      Log.w(TAG, "Unable to send null message.");
    }
  }

  private long getLastResetTime(@NonNull DeviceLastResetTime resetTimes, int deviceId) {
    for (DeviceLastResetTime.Pair pair : resetTimes.resetTime) {
      if (pair.deviceId == deviceId) {
        return pair.lastResetTime;
      }
    }
    return 0;
  }

  private @NonNull DeviceLastResetTime setLastResetTime(@NonNull DeviceLastResetTime resetTimes, int deviceId, long time) {
    DeviceLastResetTime.Builder builder = new DeviceLastResetTime.Builder();

    List<DeviceLastResetTime.Pair> newResetTimes = new ArrayList<>(resetTimes.resetTime.size());
    for (DeviceLastResetTime.Pair pair : resetTimes.resetTime) {
      if (pair.deviceId != deviceId) {
        newResetTimes.add(pair);
      }
    }


    newResetTimes.add(new DeviceLastResetTime.Pair.Builder().deviceId(deviceId).lastResetTime(time).build());

    return builder.resetTime(newResetTimes).build();
  }

  public static final class Factory implements Job.Factory<AutomaticSessionResetJob> {
    @Override
    public @NonNull AutomaticSessionResetJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      JsonJobData data = JsonJobData.deserialize(serializedData);

      return new AutomaticSessionResetJob(parameters,
                                          RecipientId.from(data.getString(KEY_RECIPIENT_ID)),
                                          data.getInt(KEY_DEVICE_ID),
                                          data.getLong(KEY_SENT_TIMESTAMP));
    }
  }
}
