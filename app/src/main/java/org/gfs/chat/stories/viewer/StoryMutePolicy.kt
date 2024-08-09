package org.gfs.chat.stories.viewer

import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.util.AppForegroundObserver

/**
 * Stories are to start muted, and once unmuted, remain as such until the
 * user backgrounds the application.
 */
object StoryMutePolicy : AppForegroundObserver.Listener {
  var isContentMuted: Boolean = true

  fun initialize() {
    AppDependencies.appForegroundObserver.addListener(this)
  }

  override fun onBackground() {
    isContentMuted = true
  }
}
