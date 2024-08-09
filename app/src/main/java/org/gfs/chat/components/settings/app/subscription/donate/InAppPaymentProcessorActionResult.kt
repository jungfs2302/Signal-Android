package org.gfs.chat.components.settings.app.subscription.donate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.signal.donations.InAppPaymentType
import org.gfs.chat.database.InAppPaymentTable

@Parcelize
class InAppPaymentProcessorActionResult(
  val action: InAppPaymentProcessorAction,
  val inAppPayment: InAppPaymentTable.InAppPayment?,
  val inAppPaymentType: InAppPaymentType,
  val status: Status
) : Parcelable {
  enum class Status {
    SUCCESS,
    FAILURE
  }
}
