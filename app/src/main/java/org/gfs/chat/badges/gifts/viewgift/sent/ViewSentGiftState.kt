package org.gfs.chat.badges.gifts.viewgift.sent

import org.gfs.chat.badges.models.Badge
import org.gfs.chat.recipients.Recipient

data class ViewSentGiftState(
  val recipient: Recipient? = null,
  val badge: Badge? = null
)
