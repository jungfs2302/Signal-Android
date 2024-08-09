package org.gfs.chat.components.webrtc

/**
 * This is an interface for [WebRtcAudioPicker31] and [WebRtcAudioPickerLegacy] to reference methods in [WebRtcAudioOutputToggleButton] without actually depending on it.
 */
interface AudioStateUpdater {
  fun updateAudioOutputState(audioOutput: WebRtcAudioOutput)
  fun hidePicker()
}
