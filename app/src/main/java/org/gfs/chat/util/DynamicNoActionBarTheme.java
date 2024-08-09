package org.gfs.chat.util;

import androidx.annotation.StyleRes;

import org.gfs.chat.R;

public class DynamicNoActionBarTheme extends DynamicTheme {

  protected @StyleRes int getTheme() {
    return R.style.Signal_DayNight_NoActionBar;
  }
}
