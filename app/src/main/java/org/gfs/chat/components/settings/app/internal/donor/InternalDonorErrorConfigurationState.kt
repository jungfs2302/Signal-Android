package org.gfs.chat.components.settings.app.internal.donor

import org.signal.donations.StripeDeclineCode
import org.gfs.chat.badges.models.Badge
import org.gfs.chat.components.settings.app.subscription.errors.UnexpectedSubscriptionCancellation

data class InternalDonorErrorConfigurationState(
  val badges: List<Badge> = emptyList(),
  val selectedBadge: Badge? = null,
  val selectedUnexpectedSubscriptionCancellation: UnexpectedSubscriptionCancellation? = null,
  val selectedStripeDeclineCode: StripeDeclineCode.Code? = null
)
