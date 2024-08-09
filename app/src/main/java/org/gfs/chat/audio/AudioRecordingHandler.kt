package org.gfs.chat.audio

interface AudioRecordingHandler {
  fun onRecordPressed()
  fun onRecordReleased()
  fun onRecordCanceled(byUser: Boolean)
  fun onRecordLocked()
  fun onRecordMoved(offsetX: Float, absoluteX: Float)
  fun onRecordPermissionRequired()
}
