/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.backup.v2.database

import org.signal.core.util.deleteAll
import org.gfs.chat.database.StickerTable

fun StickerTable.clearAllDataForBackupRestore() {
  writableDatabase.deleteAll(StickerTable.TABLE_NAME)
}
