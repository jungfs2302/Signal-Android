package org.gfs.chat.database.model

import org.gfs.chat.recipients.RecipientId

/** A model for [org.gfs.chat.database.PendingRetryReceiptTable] */
data class PendingRetryReceiptModel(
  val id: Long,
  val author: RecipientId,
  val authorDevice: Int,
  val sentTimestamp: Long,
  val receivedTimestamp: Long,
  val threadId: Long
)
