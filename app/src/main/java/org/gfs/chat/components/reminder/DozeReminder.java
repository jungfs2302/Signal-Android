package org.gfs.chat.components.reminder;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.gfs.chat.R;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.util.PowerManagerCompat;
import org.gfs.chat.util.TextSecurePreferences;

@SuppressLint("BatteryLife")
public class DozeReminder extends Reminder {

  @RequiresApi(api = 23)
  public DozeReminder(@NonNull final Context context) {
    super(R.string.DozeReminder_optimize_for_missing_play_services, R.string.DozeReminder_this_device_does_not_support_play_services_tap_to_disable_system_battery);

    setOkListener(v -> {
      TextSecurePreferences.setPromptedOptimizeDoze(context, true);
      PowerManagerCompat.requestIgnoreBatteryOptimizations(context);
    });

    setDismissListener(v -> TextSecurePreferences.setPromptedOptimizeDoze(context, true));
  }

  public static boolean isEligible(Context context) {
    return !SignalStore.account().isFcmEnabled()                   &&
           !TextSecurePreferences.hasPromptedOptimizeDoze(context) &&
           Build.VERSION.SDK_INT >= 23                             &&
           !((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(context.getPackageName());
  }
}
