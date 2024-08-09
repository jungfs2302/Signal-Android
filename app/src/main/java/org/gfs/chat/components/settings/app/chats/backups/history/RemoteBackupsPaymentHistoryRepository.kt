/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.components.settings.app.chats.backups.history

import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.database.model.InAppPaymentReceiptRecord

object RemoteBackupsPaymentHistoryRepository {

  fun getReceipts(): List<InAppPaymentReceiptRecord> {
    return SignalDatabase.donationReceipts.getReceipts(InAppPaymentReceiptRecord.Type.RECURRING_BACKUP)
  }
}
