package org.gfs.chat.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobmanager.Job;
import org.gfs.chat.jobmanager.JsonJobData;
import org.gfs.chat.jobs.protos.GroupCallPeekJobData;
import org.gfs.chat.recipients.RecipientId;
import org.gfs.chat.service.webrtc.WebRtcUtil;

import java.io.IOException;

/**
 * Runs in the same queue as messages for the group.
 */
final class GroupCallPeekWorkerJob extends BaseJob {

  public static final String KEY = "GroupCallPeekWorkerJob";

  private static final String KEY_GROUP_CALL_JOB_DATA = "group_call_job_data";

  @NonNull private final GroupCallPeekJobData groupCallPeekJobData;

  public GroupCallPeekWorkerJob(@NonNull GroupCallPeekJobData groupCallPeekJobData) {
    this(new Parameters.Builder()
                       .setQueue(PushProcessMessageJob.getQueueName(RecipientId.from(groupCallPeekJobData.groupRecipientId)))
                       .setMaxInstancesForQueue(2)
                       .build(),
         groupCallPeekJobData);
  }

  private GroupCallPeekWorkerJob(@NonNull Parameters parameters, @NonNull GroupCallPeekJobData groupCallPeekJobData) {
    super(parameters);
    this.groupCallPeekJobData = groupCallPeekJobData;
  }

  @Override
  protected void onRun() {
    RecipientId groupRecipientId = RecipientId.from(groupCallPeekJobData.groupRecipientId);

    AppDependencies.getSignalCallManager().peekGroupCall(groupRecipientId, (peekInfo) -> {
      if (groupCallPeekJobData.senderRecipientId == RecipientId.UNKNOWN.toLong()) {
        return;
      }

      RecipientId senderRecipientId = RecipientId.from(groupCallPeekJobData.senderRecipientId);
      SignalDatabase.calls().insertOrUpdateGroupCallFromLocalEvent(
          groupRecipientId,
          senderRecipientId,
          groupCallPeekJobData.serverTimestamp,
          peekInfo.getEraId(),
          peekInfo.getJoinedMembers(),
          WebRtcUtil.isCallFull(peekInfo)
      );
    });
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    return false;
  }

  @Override
  public @NonNull byte[] serialize() {
    return groupCallPeekJobData.encode();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onFailure() {
  }

  public static final class Factory implements Job.Factory<GroupCallPeekWorkerJob> {

    @Override
    public @NonNull GroupCallPeekWorkerJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      try {
        GroupCallPeekJobData jobData = GroupCallPeekJobData.ADAPTER.decode(serializedData);
        return new GroupCallPeekWorkerJob(parameters, jobData);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
