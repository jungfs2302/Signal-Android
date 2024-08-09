package org.gfs.chat.mediasend.camerax

import android.os.Build
import org.signal.core.util.asListContains
import org.gfs.chat.util.RemoteConfig

/**
 * Some phones don't work well with CameraX. This class uses a remote config to decide
 * which phones should fall back to the legacy camera.
 */
object CameraXModelBlocklist {

  @JvmStatic
  fun isBlocklisted(): Boolean {
    return RemoteConfig.cameraXModelBlocklist.asListContains(Build.MODEL)
  }
}
