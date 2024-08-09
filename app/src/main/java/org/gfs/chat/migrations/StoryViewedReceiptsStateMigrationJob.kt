package org.gfs.chat.migrations

import org.gfs.chat.database.SignalDatabase.Companion.recipients
import org.gfs.chat.jobmanager.Job
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.storage.StorageSyncHelper
import org.gfs.chat.util.TextSecurePreferences

/**
 * Added as a way to initialize the story viewed receipts setting.
 */
internal class StoryViewedReceiptsStateMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(parameters) {
  companion object {
    const val KEY = "StoryViewedReceiptsStateMigrationJob"
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (!SignalStore.story.isViewedReceiptsStateSet()) {
      SignalStore.story.viewedReceiptsEnabled = TextSecurePreferences.isReadReceiptsEnabled(context)
      if (SignalStore.account.isRegistered) {
        recipients.markNeedsSync(Recipient.self().id)
        StorageSyncHelper.scheduleSyncForDataChange()
      }
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<StoryViewedReceiptsStateMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): StoryViewedReceiptsStateMigrationJob {
      return StoryViewedReceiptsStateMigrationJob(parameters)
    }
  }
}
