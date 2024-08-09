/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.restore

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.signal.core.util.logging.Log
import org.gfs.chat.AppInitialization
import org.gfs.chat.backup.BackupPassphrase
import org.gfs.chat.backup.FullBackupImporter
import org.gfs.chat.crypto.AttachmentSecretProvider
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.jobmanager.impl.DataRestoreConstraint
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.notifications.NotificationChannels
import org.gfs.chat.service.LocalBackupListener
import org.gfs.chat.util.BackupUtil
import org.gfs.chat.util.BackupUtil.BackupInfo
import java.io.IOException

/**
 * Repository to handle restoring a backup of a user's message history.
 */
object RestoreRepository {
  private val TAG = Log.tag(RestoreRepository.javaClass)

  suspend fun getLocalBackupFromUri(context: Context, uri: Uri): BackupInfoResult = withContext(Dispatchers.IO) {
    try {
      return@withContext BackupInfoResult(backupInfo = BackupUtil.getBackupInfoFromSingleUri(context, uri), failureCause = null, failure = false)
    } catch (ex: BackupUtil.BackupFileException) {
      Log.w(TAG, "Encountered error while trying to read backup!", ex)
      return@withContext BackupInfoResult(backupInfo = null, failureCause = ex, failure = true)
    }
  }

  suspend fun restoreBackupAsynchronously(context: Context, backupFileUri: Uri, passphrase: String): BackupImportResult = withContext(Dispatchers.IO) {
    // TODO [regv2]: migrate this to a service
    try {
      Log.i(TAG, "Starting backup restore.")
      DataRestoreConstraint.isRestoringData = true

      val database = SignalDatabase.backupDatabase

      BackupPassphrase.set(context, passphrase)

      if (!FullBackupImporter.validatePassphrase(context, backupFileUri, passphrase)) {
        // TODO [regv2]: implement a specific, user-visible error for wrong passphrase.
        return@withContext BackupImportResult.FAILURE_UNKNOWN
      }

      FullBackupImporter.importFile(
        context,
        AttachmentSecretProvider.getInstance(context).getOrCreateAttachmentSecret(),
        database,
        backupFileUri,
        passphrase
      )

      SignalDatabase.runPostBackupRestoreTasks(database)
      NotificationChannels.getInstance().restoreContactNotificationChannels()

      if (BackupUtil.canUserAccessBackupDirectory(context)) {
        LocalBackupListener.setNextBackupTimeToIntervalFromNow(context)
        SignalStore.settings.isBackupEnabled = true
        LocalBackupListener.schedule(context)
      }

      AppInitialization.onPostBackupRestore(context)

      Log.i(TAG, "Backup restore complete.")
      return@withContext BackupImportResult.SUCCESS
    } catch (e: FullBackupImporter.DatabaseDowngradeException) {
      Log.w(TAG, "Failed due to the backup being from a newer version of Signal.", e)
      return@withContext BackupImportResult.FAILURE_VERSION_DOWNGRADE
    } catch (e: FullBackupImporter.ForeignKeyViolationException) {
      Log.w(TAG, "Failed due to foreign key constraint violations.", e)
      return@withContext BackupImportResult.FAILURE_FOREIGN_KEY
    } catch (e: IOException) {
      Log.w(TAG, e)
      return@withContext BackupImportResult.FAILURE_UNKNOWN
    } finally {
      DataRestoreConstraint.isRestoringData = false
    }
  }

  enum class BackupImportResult {
    SUCCESS,
    FAILURE_VERSION_DOWNGRADE,
    FAILURE_FOREIGN_KEY,
    FAILURE_UNKNOWN
  }

  data class BackupInfoResult(val backupInfo: BackupInfo?, val failureCause: BackupUtil.BackupFileException?, val failure: Boolean)
}
