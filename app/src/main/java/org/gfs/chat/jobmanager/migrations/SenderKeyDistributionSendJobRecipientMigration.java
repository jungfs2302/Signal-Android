package org.gfs.chat.jobmanager.migrations;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.signal.core.util.logging.Log;
import org.gfs.chat.database.GroupTable;
import org.gfs.chat.database.model.GroupRecord;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.groups.GroupId;
import org.gfs.chat.jobmanager.JsonJobData;
import org.gfs.chat.jobmanager.JobMigration;
import org.gfs.chat.jobs.FailingJob;

import java.util.Optional;

/**
 * We removed the messageId property from the job data and replaced it with a serialized envelope,
 * so we need to take jobs that referenced an ID and replace it with the envelope instead.
 */
public class SenderKeyDistributionSendJobRecipientMigration extends JobMigration {

  private static final String TAG = Log.tag(SenderKeyDistributionSendJobRecipientMigration.class);

  private final GroupTable groupDatabase;

  public SenderKeyDistributionSendJobRecipientMigration() {
    this(SignalDatabase.groups());
  }

  @VisibleForTesting
  SenderKeyDistributionSendJobRecipientMigration(GroupTable groupDatabase) {
    super(9);
    this.groupDatabase = groupDatabase;
  }

  @Override
  public @NonNull JobData migrate(@NonNull JobData jobData) {
    if ("SenderKeyDistributionSendJob".equals(jobData.getFactoryKey())) {
      return migrateJob(jobData, groupDatabase);
    } else {
      return jobData;
    }
  }

  private static @NonNull JobData migrateJob(@NonNull JobData jobData, @NonNull GroupTable groupDatabase) {
    JsonJobData data = JsonJobData.deserialize(jobData.getData());

    if (data.hasString("group_id")) {
      GroupId               groupId = GroupId.pushOrThrow(data.getStringAsBlob("group_id"));
      Optional<GroupRecord> group   = groupDatabase.getGroup(groupId);

      if (group.isPresent()) {
        return jobData.withData(data.buildUpon()
                                    .putString("thread_recipient_id", group.get().getRecipientId().serialize())
                                    .serialize());

      } else {
        return jobData.withFactoryKey(FailingJob.KEY);
      }
    } else if (!data.hasString("thread_recipient_id")) {
      return jobData.withFactoryKey(FailingJob.KEY);
    } else {
      return jobData;
    }
  }
}
