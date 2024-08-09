package org.gfs.chat.database.model

import org.signal.libsignal.protocol.IdentityKey
import org.gfs.chat.database.IdentityTable
import org.gfs.chat.recipients.RecipientId

data class IdentityStoreRecord(
  val addressName: String,
  val identityKey: IdentityKey,
  val verifiedStatus: IdentityTable.VerifiedStatus,
  val firstUse: Boolean,
  val timestamp: Long,
  val nonblockingApproval: Boolean
) {
  fun toIdentityRecord(recipientId: RecipientId): IdentityRecord {
    return IdentityRecord(
      recipientId = recipientId,
      identityKey = identityKey,
      verifiedStatus = verifiedStatus,
      firstUse = firstUse,
      timestamp = timestamp,
      nonblockingApproval = nonblockingApproval
    )
  }
}
