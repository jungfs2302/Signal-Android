/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.restore.restorelocalbackup

import android.net.Uri
import org.gfs.chat.restore.RestoreRepository
import org.gfs.chat.util.BackupUtil
import org.gfs.chat.util.BackupUtil.BackupInfo

/**
 * State holder for a backup restore.
 */
data class RestoreLocalBackupState(
  val uri: Uri,
  val backupInfo: BackupInfo? = null,
  val backupFileStateError: BackupUtil.BackupFileState? = null,
  val backupPassphrase: String = "",
  val restoreInProgress: Boolean = false,
  val backupVerifyingInProgress: Boolean = false,
  val backupProgressCount: Long = -1,
  val backupEstimatedTotalCount: Long = -1,
  val backupRestoreComplete: Boolean = false,
  val backupImportResult: RestoreRepository.BackupImportResult? = null,
  val abort: Boolean = false
)
