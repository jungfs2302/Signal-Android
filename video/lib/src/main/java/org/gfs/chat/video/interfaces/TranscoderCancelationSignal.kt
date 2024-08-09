package org.gfs.chat.video.interfaces

fun interface TranscoderCancelationSignal {
  fun isCanceled(): Boolean
}
