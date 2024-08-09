package org.gfs.chat.mediasend.v2.capture

import org.gfs.chat.mediasend.Media
import org.gfs.chat.recipients.Recipient

sealed class MediaCaptureEvent {
  data class MediaCaptureRendered(val media: Media) : MediaCaptureEvent()
  data class UsernameScannedFromQrCode(val recipient: Recipient, val username: String) : MediaCaptureEvent()
  object DeviceLinkScannedFromQrCode : MediaCaptureEvent()
  object MediaCaptureRenderFailed : MediaCaptureEvent()
}
