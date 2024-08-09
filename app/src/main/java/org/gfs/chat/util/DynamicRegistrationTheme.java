package org.gfs.chat.util;

import androidx.annotation.StyleRes;

import org.gfs.chat.R;

public class DynamicRegistrationTheme extends DynamicTheme {

  protected @StyleRes int getTheme() {
    return R.style.Signal_DayNight_Registration;
  }
}
