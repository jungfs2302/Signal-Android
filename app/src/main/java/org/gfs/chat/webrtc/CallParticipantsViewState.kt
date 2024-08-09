package org.gfs.chat.webrtc

import org.gfs.chat.components.webrtc.CallParticipantsState
import org.gfs.chat.service.webrtc.state.WebRtcEphemeralState

class CallParticipantsViewState(
  callParticipantsState: CallParticipantsState,
  ephemeralState: WebRtcEphemeralState,
  val isPortrait: Boolean,
  val isLandscapeEnabled: Boolean,
  val isStartedFromCallLink: Boolean
) {

  val callParticipantsState = CallParticipantsState.update(callParticipantsState, ephemeralState)
}
