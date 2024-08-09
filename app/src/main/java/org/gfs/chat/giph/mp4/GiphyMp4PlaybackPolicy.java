package org.gfs.chat.giph.mp4;

import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.util.DeviceProperties;

import java.util.concurrent.TimeUnit;

/**
 * Central policy object for determining what kind of gifs to display, routing, etc.
 */
public final class GiphyMp4PlaybackPolicy {

  private GiphyMp4PlaybackPolicy() { }

  public static boolean autoplay() {
    return !DeviceProperties.isLowMemoryDevice(AppDependencies.getApplication());
  }

  public static int maxRepeatsOfSinglePlayback() {
    return 4;
  }

  public static long maxDurationOfSinglePlayback() {
    return TimeUnit.SECONDS.toMillis(8);
  }

  public static int maxSimultaneousPlaybackInSearchResults() {
    return AppDependencies.getExoPlayerPool().getPoolStats().getMaxUnreserved();
  }

  public static int maxSimultaneousPlaybackInConversation() {
    return AppDependencies.getExoPlayerPool().getPoolStats().getMaxUnreserved() / 3;
  }
}
