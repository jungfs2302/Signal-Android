package org.gfs.chat.pin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.gfs.chat.MainActivity;
import org.gfs.chat.PassphraseRequiredActivity;
import org.gfs.chat.R;
import org.gfs.chat.lock.v2.CreateSvrPinActivity;
import org.gfs.chat.util.DynamicNoActionBarTheme;
import org.gfs.chat.util.DynamicTheme;

public final class PinRestoreActivity extends AppCompatActivity {

  private final DynamicTheme dynamicTheme = new DynamicNoActionBarTheme();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    dynamicTheme.onCreate(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pin_restore_activity);
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
  }

  void navigateToPinCreation() {
    final Intent main      = MainActivity.clearTop(this);
    final Intent createPin = CreateSvrPinActivity.getIntentForPinCreate(this);
    final Intent chained   = PassphraseRequiredActivity.chainIntent(createPin, main);

    startActivity(chained);
    finish();
  }
}
