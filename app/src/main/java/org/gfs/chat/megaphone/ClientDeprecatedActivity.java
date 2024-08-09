package org.gfs.chat.megaphone;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.signal.core.util.ThreadUtil;
import org.gfs.chat.PassphraseRequiredActivity;
import org.gfs.chat.R;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.util.DynamicNoActionBarTheme;
import org.gfs.chat.util.DynamicTheme;
import org.gfs.chat.util.PlayStoreUtil;

/**
 * Shown when a users build fully expires. Controlled by {@link Megaphones.Event#CLIENT_DEPRECATED}.
 */
public class ClientDeprecatedActivity extends PassphraseRequiredActivity {

  private final DynamicTheme theme = new DynamicNoActionBarTheme();

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    setContentView(R.layout.client_deprecated_activity);

    findViewById(R.id.client_deprecated_update_button).setOnClickListener(v -> onUpdateClicked());
    findViewById(R.id.client_deprecated_dont_update_button).setOnClickListener(v -> onDontUpdateClicked());
  }

  @Override
  protected void onPreCreate() {
    theme.onCreate(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    theme.onResume(this);
  }

  @SuppressLint("MissingSuperCall")
  @Override
  public void onBackPressed() {
    // Disabled
  }

  private void onUpdateClicked() {
    PlayStoreUtil.openPlayStoreOrOurApkDownloadPage(this);
  }

  private void onDontUpdateClicked() {
    new MaterialAlertDialogBuilder(this)
                   .setTitle(R.string.ClientDeprecatedActivity_warning)
                   .setMessage(R.string.ClientDeprecatedActivity_your_version_of_signal_has_expired_you_can_view_your_message_history)
                   .setPositiveButton(R.string.ClientDeprecatedActivity_dont_update, (dialog, which) -> {
                     AppDependencies.getMegaphoneRepository().markFinished(Megaphones.Event.CLIENT_DEPRECATED, () -> {
                       ThreadUtil.runOnMain(this::finish);
                     });
                   })
                   .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                   .show();
  }
}
