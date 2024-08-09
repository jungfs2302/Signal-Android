package org.gfs.chat.migrations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.SqlUtil;
import org.gfs.chat.database.EmojiSearchTable;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.jobmanager.Job;
import org.gfs.chat.jobs.EmojiSearchIndexDownloadJob;
import org.gfs.chat.keyvalue.SignalStore;

/**
 * Schedules job to get the latest emoji search index if it's empty.
 */
public final class EmojiSearchIndexCheckMigrationJob extends MigrationJob {

  public static final String KEY = "EmojiSearchIndexCheckMigrationJob";

  EmojiSearchIndexCheckMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private EmojiSearchIndexCheckMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public boolean isUiBlocking() {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void performMigration() {
    if (SqlUtil.isEmpty(SignalDatabase.getRawDatabase(), EmojiSearchTable.TABLE_NAME)) {
      SignalStore.emoji().clearSearchIndexMetadata();
      EmojiSearchIndexDownloadJob.scheduleImmediately();
    }
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  public static class Factory implements Job.Factory<EmojiSearchIndexCheckMigrationJob> {
    @Override
    public @NonNull EmojiSearchIndexCheckMigrationJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new EmojiSearchIndexCheckMigrationJob(parameters);
    }
  }
}
