package org.gfs.chat.migrations;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobmanager.Job;
import org.gfs.chat.keyvalue.SignalStore;

import static org.gfs.chat.keyvalue.SettingsValues.THREAD_TRIM_ENABLED;
import static org.gfs.chat.keyvalue.SettingsValues.THREAD_TRIM_LENGTH;

public class TrimByLengthSettingsMigrationJob extends MigrationJob {

  private static final String TAG = Log.tag(TrimByLengthSettingsMigrationJob.class);

  public static final String KEY = "TrimByLengthSettingsMigrationJob";

  TrimByLengthSettingsMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private TrimByLengthSettingsMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  boolean isUiBlocking() {
    return false;
  }

  @Override
  void performMigration() throws Exception {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppDependencies.getApplication());
    if (preferences.contains(THREAD_TRIM_ENABLED)) {
      SignalStore.settings().setThreadTrimByLengthEnabled(preferences.getBoolean(THREAD_TRIM_ENABLED, false));
      //noinspection ConstantConditions
      SignalStore.settings().setThreadTrimLength(Integer.parseInt(preferences.getString(THREAD_TRIM_LENGTH, "500")));

      preferences.edit()
                 .remove(THREAD_TRIM_ENABLED)
                 .remove(THREAD_TRIM_LENGTH)
                 .apply();
    }
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  public static class Factory implements Job.Factory<TrimByLengthSettingsMigrationJob> {
    @Override
    public @NonNull TrimByLengthSettingsMigrationJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new TrimByLengthSettingsMigrationJob(parameters);
    }
  }
}
