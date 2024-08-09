package org.gfs.chat.migrations

import org.signal.core.util.logging.Log
import org.signal.core.util.withinTransaction
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobmanager.Job
import org.gfs.chat.jobs.MultiDeviceKeysUpdateJob
import org.gfs.chat.jobs.StorageSyncJob
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.util.TextSecurePreferences

/**
 * Remove local unknown storage ids not in local storage service manifest.
 */
internal class StorageFixLocalUnknownMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(parameters) {

  companion object {
    val TAG = Log.tag(StorageFixLocalUnknownMigrationJob::class.java)
    const val KEY = "StorageFixLocalUnknownMigrationJob"
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  @Suppress("UsePropertyAccessSyntax")
  override fun performMigration() {
    val localStorageIds = SignalStore.storageService.getManifest().storageIds.toSet()
    val unknownLocalIds = SignalDatabase.unknownStorageIds.getAllUnknownIds().toSet()
    val danglingLocalUnknownIds = unknownLocalIds - localStorageIds

    if (danglingLocalUnknownIds.isEmpty()) {
      return
    }

    Log.w(TAG, "Removing ${danglingLocalUnknownIds.size} dangling unknown ids")

    SignalDatabase.rawDatabase.withinTransaction {
      SignalDatabase.unknownStorageIds.delete(danglingLocalUnknownIds)
    }

    val jobManager = AppDependencies.jobManager

    if (TextSecurePreferences.isMultiDevice(context)) {
      Log.i(TAG, "Multi-device.")
      jobManager.startChain(StorageSyncJob())
        .then(MultiDeviceKeysUpdateJob())
        .enqueue()
    } else {
      Log.i(TAG, "Single-device.")
      jobManager.add(StorageSyncJob())
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<StorageFixLocalUnknownMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): StorageFixLocalUnknownMigrationJob {
      return StorageFixLocalUnknownMigrationJob(parameters)
    }
  }
}
