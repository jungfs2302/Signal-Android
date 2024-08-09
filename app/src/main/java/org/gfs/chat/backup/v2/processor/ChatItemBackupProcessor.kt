/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.backup.v2.processor

import org.signal.core.util.logging.Log
import org.gfs.chat.backup.v2.ExportState
import org.gfs.chat.backup.v2.ImportState
import org.gfs.chat.backup.v2.database.ChatItemImportInserter
import org.gfs.chat.backup.v2.database.createChatItemInserter
import org.gfs.chat.backup.v2.database.getMessagesForBackup
import org.gfs.chat.backup.v2.proto.Frame
import org.gfs.chat.backup.v2.stream.BackupFrameEmitter
import org.gfs.chat.database.SignalDatabase

object ChatItemBackupProcessor {
  val TAG = Log.tag(ChatItemBackupProcessor::class.java)

  fun export(db: SignalDatabase, exportState: ExportState, emitter: BackupFrameEmitter) {
    db.messageTable.getMessagesForBackup(exportState.backupTime, exportState.allowMediaBackup).use { chatItems ->
      while (chatItems.hasNext()) {
        val chatItem = chatItems.next()
        if (chatItem != null) {
          if (exportState.threadIds.contains(chatItem.chatId)) {
            emitter.emit(Frame(chatItem = chatItem))
          }
        }
      }
    }
  }

  fun beginImport(importState: ImportState): ChatItemImportInserter {
    return SignalDatabase.messages.createChatItemInserter(importState)
  }
}
