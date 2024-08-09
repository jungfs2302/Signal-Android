package org.gfs.chat.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import org.gfs.chat.database.RecipientTable;
import org.gfs.chat.database.RecipientTable.RecipientReader;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobmanager.Job;
import org.gfs.chat.jobmanager.impl.NetworkConstraint;
import org.gfs.chat.net.NotPushRegisteredException;
import org.gfs.chat.recipients.Recipient;
import org.gfs.chat.recipients.RecipientUtil;
import org.gfs.chat.util.TextSecurePreferences;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.multidevice.BlockedListMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;
import org.whispersystems.signalservice.api.push.exceptions.ServerRejectedException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MultiDeviceBlockedUpdateJob extends BaseJob {

  public static final String KEY = "MultiDeviceBlockedUpdateJob";

  @SuppressWarnings("unused")
  private static final String TAG = Log.tag(MultiDeviceBlockedUpdateJob.class);

  public MultiDeviceBlockedUpdateJob() {
    this(new Job.Parameters.Builder()
                           .addConstraint(NetworkConstraint.KEY)
                           .setQueue("MultiDeviceBlockedUpdateJob")
                           .setLifespan(TimeUnit.DAYS.toMillis(1))
                           .setMaxAttempts(Parameters.UNLIMITED)
                           .build());
  }

  private MultiDeviceBlockedUpdateJob(@NonNull Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public @Nullable byte[] serialize() {
    return null;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun()
      throws IOException, UntrustedIdentityException
  {
    if (!Recipient.self().isRegistered()) {
      throw new NotPushRegisteredException();
    }

    if (!TextSecurePreferences.isMultiDevice(context)) {
      Log.i(TAG, "Not multi device, aborting...");
      return;
    }

    RecipientTable database = SignalDatabase.recipients();

    try (RecipientReader reader = database.readerForBlocked(database.getBlocked())) {
      List<SignalServiceAddress> blockedIndividuals = new LinkedList<>();
      List<byte[]>               blockedGroups      = new LinkedList<>();

      Recipient recipient;

      while ((recipient = reader.getNext()) != null) {
        if (recipient.isPushGroup()) {
          blockedGroups.add(recipient.requireGroupId().getDecodedId());
        } else if (recipient.isMaybeRegistered() && (recipient.getHasServiceId() || recipient.getHasE164())) {
          blockedIndividuals.add(RecipientUtil.toSignalServiceAddress(context, recipient));
        }
      }

      SignalServiceMessageSender messageSender = AppDependencies.getSignalServiceMessageSender();
      messageSender.sendSyncMessage(SignalServiceSyncMessage.forBlocked(new BlockedListMessage(blockedIndividuals, blockedGroups))
      );
    }
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    if (exception instanceof ServerRejectedException) return false;
    if (exception instanceof PushNetworkException) return true;
    return false;
  }

  @Override
  public void onFailure() {
  }

  public static final class Factory implements Job.Factory<MultiDeviceBlockedUpdateJob> {
    @Override
    public @NonNull MultiDeviceBlockedUpdateJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new MultiDeviceBlockedUpdateJob(parameters);
    }
  }
}
