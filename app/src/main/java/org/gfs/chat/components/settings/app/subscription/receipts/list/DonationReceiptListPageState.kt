package org.gfs.chat.components.settings.app.subscription.receipts.list

import org.gfs.chat.database.model.InAppPaymentReceiptRecord

data class DonationReceiptListPageState(
  val records: List<InAppPaymentReceiptRecord> = emptyList(),
  val isLoaded: Boolean = false
)
