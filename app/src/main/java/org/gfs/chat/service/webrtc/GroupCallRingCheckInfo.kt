package org.gfs.chat.service.webrtc

import org.signal.ringrtc.CallManager
import org.gfs.chat.groups.GroupId
import org.gfs.chat.recipients.RecipientId
import org.whispersystems.signalservice.api.push.ServiceId.ACI

data class GroupCallRingCheckInfo(
  val recipientId: RecipientId,
  val groupId: GroupId.V2,
  val ringId: Long,
  val ringerAci: ACI,
  val ringUpdate: CallManager.RingUpdate
)
