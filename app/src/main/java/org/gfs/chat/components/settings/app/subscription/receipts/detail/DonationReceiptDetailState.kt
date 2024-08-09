package org.gfs.chat.components.settings.app.subscription.receipts.detail

import org.gfs.chat.database.model.InAppPaymentReceiptRecord

data class DonationReceiptDetailState(
  val inAppPaymentReceiptRecord: InAppPaymentReceiptRecord? = null,
  val subscriptionName: String? = null
)
