package org.gfs.chat.components.settings.app.subscription

import org.gfs.chat.badges.models.Badge

/**
 * Events that can arise from use of the donations apis.
 */
sealed class DonationEvent {
  object RequestTokenSuccess : DonationEvent()
  class PaymentConfirmationSuccess(val badge: Badge) : DonationEvent()
  class SubscriptionCancellationFailed(val throwable: Throwable) : DonationEvent()
  object SubscriptionCancelled : DonationEvent()
}
