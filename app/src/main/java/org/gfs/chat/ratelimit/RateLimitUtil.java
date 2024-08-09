package org.gfs.chat.ratelimit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.signal.core.util.logging.Log;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobs.PushGroupSendJob;
import org.gfs.chat.jobs.IndividualSendJob;

import java.util.Set;

public final class RateLimitUtil {

  private static final String TAG = Log.tag(RateLimitUtil.class);

  private RateLimitUtil() {}

  /**
   * Forces a retry of all rate limited messages by editing jobs that are in the queue.
   */
  @WorkerThread
  public static void retryAllRateLimitedMessages(@NonNull Context context) {
    Set<Long> messageIds = SignalDatabase.messages().getAllRateLimitedMessageIds();

    if (messageIds.isEmpty()) {
      return;
    }

    Log.i(TAG, "Retrying " + messageIds.size() + " message records.");

    SignalDatabase.messages().clearRateLimitStatus(messageIds);

    AppDependencies.getJobManager().update((job) -> {
      if (job.getFactoryKey().equals(IndividualSendJob.KEY) && messageIds.contains(IndividualSendJob.getMessageId(job.getSerializedData()))) {
        return job.withNextBackoffInterval(0);
      } else if (job.getFactoryKey().equals(PushGroupSendJob.KEY) && messageIds.contains(PushGroupSendJob.getMessageId(job.getSerializedData()))) {
        return job.withNextBackoffInterval(0);
      } else {
        return job;
      }
    });
  }
}
