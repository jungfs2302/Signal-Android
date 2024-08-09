package org.gfs.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.signal.core.util.concurrent.LifecycleDisposable;
import org.signal.donations.StripeApi;
import org.gfs.chat.components.DebugLogsPromptDialogFragment;
import org.gfs.chat.components.DeviceSpecificNotificationBottomSheet;
import org.gfs.chat.components.PromptBatterySaverDialogFragment;
import org.gfs.chat.components.ConnectivityWarningBottomSheet;
import org.gfs.chat.components.settings.app.AppSettingsActivity;
import org.gfs.chat.components.voice.VoiceNoteMediaController;
import org.gfs.chat.components.voice.VoiceNoteMediaControllerOwner;
import org.gfs.chat.conversationlist.RelinkDevicesReminderBottomSheetFragment;
import org.gfs.chat.devicetransfer.olddevice.OldDeviceExitActivity;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.net.DeviceTransferBlockingInterceptor;
import org.gfs.chat.notifications.VitalsViewModel;
import org.gfs.chat.stories.tabs.ConversationListTabRepository;
import org.gfs.chat.stories.tabs.ConversationListTabsViewModel;
import org.gfs.chat.util.AppStartup;
import org.gfs.chat.util.CachedInflater;
import org.gfs.chat.util.CommunicationActions;
import org.gfs.chat.util.DynamicNoActionBarTheme;
import org.gfs.chat.util.DynamicTheme;
import org.gfs.chat.util.SplashScreenUtil;
import org.gfs.chat.util.WindowUtil;

public class MainActivity extends PassphraseRequiredActivity implements VoiceNoteMediaControllerOwner {

  public static final int RESULT_CONFIG_CHANGED = Activity.RESULT_FIRST_USER + 901;

  private final DynamicTheme  dynamicTheme = new DynamicNoActionBarTheme();
  private final MainNavigator navigator    = new MainNavigator(this);

  private VoiceNoteMediaController      mediaController;
  private ConversationListTabsViewModel conversationListTabsViewModel;
  private VitalsViewModel               vitalsViewModel;

  private final LifecycleDisposable lifecycleDisposable = new LifecycleDisposable();

  private boolean onFirstRender = false;

  public static @NonNull Intent clearTop(@NonNull Context context) {
    Intent intent = new Intent(context, MainActivity.class);

    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    AppStartup.getInstance().onCriticalRenderEventStart();
    super.onCreate(savedInstanceState, ready);

    setContentView(R.layout.main_activity);
    final View content = findViewById(android.R.id.content);
    content.getViewTreeObserver().addOnPreDrawListener(
        new ViewTreeObserver.OnPreDrawListener() {
          @Override
          public boolean onPreDraw() {
            // Use pre draw listener to delay drawing frames till conversation list is ready
            if (onFirstRender) {
              content.getViewTreeObserver().removeOnPreDrawListener(this);
              return true;
            } else {
              return false;
            }
          }
        });

    lifecycleDisposable.bindTo(this);

    mediaController = new VoiceNoteMediaController(this, true);

    ConversationListTabRepository         repository = new ConversationListTabRepository();
    ConversationListTabsViewModel.Factory factory    = new ConversationListTabsViewModel.Factory(repository);

    handleDeeplinkIntent(getIntent());

    CachedInflater.from(this).clear();

    conversationListTabsViewModel = new ViewModelProvider(this, factory).get(ConversationListTabsViewModel.class);
    updateTabVisibility();

    vitalsViewModel = new ViewModelProvider(this).get(VitalsViewModel.class);

    lifecycleDisposable.add(
        vitalsViewModel
            .getVitalsState()
            .subscribe(this::presentVitalsState)
    );
  }

  @SuppressLint("NewApi")
  private void presentVitalsState(VitalsViewModel.State state) {
    switch (state) {
      case NONE:
        break;
      case PROMPT_SPECIFIC_BATTERY_SAVER_DIALOG:
        DeviceSpecificNotificationBottomSheet.show(getSupportFragmentManager());
        break;
      case PROMPT_GENERAL_BATTERY_SAVER_DIALOG:
        PromptBatterySaverDialogFragment.show(getSupportFragmentManager());
        break;
      case PROMPT_CONNECTIVITY_WARNING:
        ConnectivityWarningBottomSheet.show(getSupportFragmentManager());
        break;
      case PROMPT_DEBUGLOGS_FOR_NOTIFICATIONS:
        DebugLogsPromptDialogFragment.show(this, DebugLogsPromptDialogFragment.Purpose.NOTIFICATIONS);
        break;
      case PROMPT_DEBUGLOGS_FOR_CRASH:
        DebugLogsPromptDialogFragment.show(this, DebugLogsPromptDialogFragment.Purpose.CRASH);
        break;
      case PROMPT_DEBUGLOGS_FOR_CONNECTIVITY_WARNING:
        DebugLogsPromptDialogFragment.show(this, DebugLogsPromptDialogFragment.Purpose.CONNECTIVITY_WARNING);
        break;
    }
  }

  @Override
  public Intent getIntent() {
    return super.getIntent().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                      Intent.FLAG_ACTIVITY_NEW_TASK |
                                      Intent.FLAG_ACTIVITY_SINGLE_TOP);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleDeeplinkIntent(intent);
  }

  @Override
  protected void onPreCreate() {
    super.onPreCreate();
    dynamicTheme.onCreate(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
    if (SignalStore.misc().isOldDeviceTransferLocked()) {
      new MaterialAlertDialogBuilder(this)
          .setTitle(R.string.OldDeviceTransferLockedDialog__complete_registration_on_your_new_device)
          .setMessage(R.string.OldDeviceTransferLockedDialog__your_signal_account_has_been_transferred_to_your_new_device)
          .setPositiveButton(R.string.OldDeviceTransferLockedDialog__done, (d, w) -> OldDeviceExitActivity.exit(this))
          .setNegativeButton(R.string.OldDeviceTransferLockedDialog__cancel_and_activate_this_device, (d, w) -> {
            SignalStore.misc().setOldDeviceTransferLocked(false);
            DeviceTransferBlockingInterceptor.getInstance().unblockNetwork();
          })
          .setCancelable(false)
          .show();
    }

    if (SignalStore.misc().getShouldShowLinkedDevicesReminder()) {
      SignalStore.misc().setShouldShowLinkedDevicesReminder(false);
      RelinkDevicesReminderBottomSheetFragment.show(getSupportFragmentManager());
    }

    updateTabVisibility();

    vitalsViewModel.checkSlowNotificationHeuristics();
  }

  @Override
  protected void onStop() {
    super.onStop();
    SplashScreenUtil.setSplashScreenThemeIfNecessary(this, SignalStore.settings().getTheme());
  }

  @Override
  public void onBackPressed() {
    if (!navigator.onBackPressed()) {
      super.onBackPressed();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == MainNavigator.REQUEST_CONFIG_CHANGES && resultCode == RESULT_CONFIG_CHANGED) {
      recreate();
    }
  }

  private void updateTabVisibility() {
    findViewById(R.id.conversation_list_tabs).setVisibility(View.VISIBLE);
    WindowUtil.setNavigationBarColor(this, ContextCompat.getColor(this, R.color.signal_colorSurface2));
  }

  public @NonNull MainNavigator getNavigator() {
    return navigator;
  }

  private void handleDeeplinkIntent(Intent intent) {
    handleGroupLinkInIntent(intent);
    handleProxyInIntent(intent);
    handleSignalMeIntent(intent);
    handleCallLinkInIntent(intent);
    handleDonateReturnIntent(intent);
  }

  private void handleGroupLinkInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialGroupLinkUrl(this, data.toString());
    }
  }

  private void handleProxyInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialProxyLinkUrl(this, data.toString());
    }
  }

  private void handleSignalMeIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialSignalMeUrl(this, data.toString());
    }
  }

  private void handleCallLinkInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialCallLinkUrl(this, data.toString());
    }
  }

  private void handleDonateReturnIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null && data.toString().startsWith(StripeApi.RETURN_URL_IDEAL)) {
      startActivity(AppSettingsActivity.manageSubscriptions(this));
    }
  }

  public void onFirstRender() {
    onFirstRender = true;
  }

  @Override
  public @NonNull VoiceNoteMediaController getVoiceNoteMediaController() {
    return mediaController;
  }
}
