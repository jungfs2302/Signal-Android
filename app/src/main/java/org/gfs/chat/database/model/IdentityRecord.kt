package org.gfs.chat.database.model

import org.signal.libsignal.protocol.IdentityKey
import org.gfs.chat.database.IdentityTable
import org.gfs.chat.recipients.RecipientId

data class IdentityRecord(
  val recipientId: RecipientId,
  val identityKey: IdentityKey,
  val verifiedStatus: IdentityTable.VerifiedStatus,
  @get:JvmName("isFirstUse")
  val firstUse: Boolean,
  val timestamp: Long,
  @get:JvmName("isApprovedNonBlocking")
  val nonblockingApproval: Boolean
)
