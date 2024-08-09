package org.gfs.chat.migrations

import org.signal.core.util.logging.Log
import org.gfs.chat.database.RecipientTable
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.jobmanager.Job
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient

/**
 * There was a bug where some users had their own recipient entry marked unregistered. This fixes that.
 */
internal class SelfRegisteredStateMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(parameters) {

  companion object {
    const val KEY = "SelfRegisteredStateMigrationJob"

    val TAG = Log.tag(SelfRegisteredStateMigrationJob::class.java)
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (SignalStore.account.isRegistered && SignalStore.account.aci != null) {
      val record = SignalDatabase.recipients.getRecord(Recipient.self().id)

      if (record.registered != RecipientTable.RegisteredState.REGISTERED) {
        Log.w(TAG, "Inconsistent registered state! Fixing...")
        SignalDatabase.recipients.markRegistered(Recipient.self().id, SignalStore.account.aci!!)
      } else {
        Log.d(TAG, "Local user is already registered.")
      }
    } else {
      Log.d(TAG, "Not registered. Skipping.")
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<SelfRegisteredStateMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): SelfRegisteredStateMigrationJob {
      return SelfRegisteredStateMigrationJob(parameters)
    }
  }
}
