package org.gfs.chat.migrations

import org.signal.core.util.logging.Log
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.jobmanager.Job
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.storage.StorageSyncHelper

/**
 * Migration to copy any existing username to [SignalStore.account]
 */
internal class CopyUsernameToSignalStoreMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(parameters) {

  companion object {
    const val KEY = "CopyUsernameToSignalStore"

    val TAG = Log.tag(CopyUsernameToSignalStoreMigrationJob::class.java)
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (SignalStore.account.aci == null || SignalStore.account.pni == null) {
      Log.i(TAG, "ACI/PNI are unset, skipping.")
      return
    }

    val self = Recipient.self()

    if (self.username.isEmpty || self.username.get().isBlank()) {
      Log.i(TAG, "No username set, skipping.")
      return
    }

    SignalStore.account.username = self.username.get()

    // New fields in storage service, so we trigger a sync
    SignalDatabase.recipients.markNeedsSync(self.id)
    StorageSyncHelper.scheduleSyncForDataChange()
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<CopyUsernameToSignalStoreMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): CopyUsernameToSignalStoreMigrationJob {
      return CopyUsernameToSignalStoreMigrationJob(parameters)
    }
  }
}
