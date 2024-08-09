package org.gfs.chat.components.settings.app.subscription.receipts.list

import org.gfs.chat.badges.models.Badge
import org.gfs.chat.database.model.InAppPaymentReceiptRecord

data class DonationReceiptBadge(
  val type: InAppPaymentReceiptRecord.Type,
  val level: Int,
  val badge: Badge
)
