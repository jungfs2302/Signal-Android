/*
 * Copyright (C) 2013 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gfs.chat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.multidex.MultiDexApplication;

import com.bumptech.glide.Glide;
import com.google.android.gms.security.ProviderInstaller;

import org.conscrypt.ConscryptSignal;
import org.greenrobot.eventbus.EventBus;
import org.signal.aesgcmprovider.AesGcmProvider;
import org.signal.core.util.MemoryTracker;
import org.signal.core.util.concurrent.AnrDetector;
import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.logging.AndroidLogger;
import org.signal.core.util.logging.Log;
import org.signal.core.util.logging.Scrubber;
import org.signal.core.util.tracing.Tracer;
import org.signal.glide.SignalGlideCodecs;
import org.signal.libsignal.protocol.logging.SignalProtocolLoggerProvider;
import org.signal.ringrtc.CallManager;
import org.gfs.chat.apkupdate.ApkUpdateRefreshListener;
import org.gfs.chat.avatar.AvatarPickerStorage;
import org.gfs.chat.crypto.AttachmentSecretProvider;
import org.gfs.chat.crypto.DatabaseSecretProvider;
import org.gfs.chat.database.LogDatabase;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.database.SqlCipherLibraryLoader;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.dependencies.ApplicationDependencyProvider;
import org.gfs.chat.emoji.EmojiSource;
import org.gfs.chat.emoji.JumboEmoji;
import org.gfs.chat.gcm.FcmFetchManager;
import org.gfs.chat.jobs.AccountConsistencyWorkerJob;
import org.gfs.chat.jobs.BuildExpirationConfirmationJob;
import org.gfs.chat.jobs.CheckServiceReachabilityJob;
import org.gfs.chat.jobs.DownloadLatestEmojiDataJob;
import org.gfs.chat.jobs.EmojiSearchIndexDownloadJob;
import org.gfs.chat.jobs.FcmRefreshJob;
import org.gfs.chat.jobs.FontDownloaderJob;
import org.gfs.chat.jobs.GroupRingCleanupJob;
import org.gfs.chat.jobs.GroupV2UpdateSelfProfileKeyJob;
import org.gfs.chat.jobs.InAppPaymentAuthCheckJob;
import org.gfs.chat.jobs.InAppPaymentKeepAliveJob;
import org.gfs.chat.jobs.LinkedDeviceInactiveCheckJob;
import org.gfs.chat.jobs.MultiDeviceContactUpdateJob;
import org.gfs.chat.jobs.PnpInitializeDevicesJob;
import org.gfs.chat.jobs.PreKeysSyncJob;
import org.gfs.chat.jobs.ProfileUploadJob;
import org.gfs.chat.jobs.RefreshSvrCredentialsJob;
import org.gfs.chat.jobs.RetrieveProfileJob;
import org.gfs.chat.jobs.RetrieveRemoteAnnouncementsJob;
import org.gfs.chat.jobs.StoryOnboardingDownloadJob;
import org.gfs.chat.keyvalue.KeepMessagesDuration;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.logging.CustomSignalProtocolLogger;
import org.gfs.chat.logging.PersistentLogger;
import org.gfs.chat.messageprocessingalarm.RoutineMessageFetchReceiver;
import org.gfs.chat.messages.GroupSendEndorsementInternalNotifier;
import org.gfs.chat.migrations.ApplicationMigrations;
import org.gfs.chat.mms.SignalGlideComponents;
import org.gfs.chat.mms.SignalGlideModule;
import org.gfs.chat.providers.BlobProvider;
import org.gfs.chat.ratelimit.RateLimitUtil;
import org.gfs.chat.recipients.Recipient;
import org.gfs.chat.registration.RegistrationUtil;
import org.gfs.chat.ringrtc.RingRtcLogger;
import org.gfs.chat.service.AnalyzeDatabaseAlarmListener;
import org.gfs.chat.service.DirectoryRefreshListener;
import org.gfs.chat.service.KeyCachingService;
import org.gfs.chat.service.LocalBackupListener;
import org.gfs.chat.service.MessageBackupListener;
import org.gfs.chat.service.RotateSenderCertificateListener;
import org.gfs.chat.service.RotateSignedPreKeyListener;
import org.gfs.chat.service.webrtc.ActiveCallManager;
import org.gfs.chat.service.webrtc.AndroidTelecomUtil;
import org.gfs.chat.storage.StorageSyncHelper;
import org.gfs.chat.util.AppForegroundObserver;
import org.gfs.chat.util.AppStartup;
import org.gfs.chat.util.DynamicTheme;
import org.gfs.chat.util.RemoteConfig;
import org.gfs.chat.util.SignalLocalMetrics;
import org.gfs.chat.util.SignalUncaughtExceptionHandler;
import org.gfs.chat.util.TextSecurePreferences;
import org.gfs.chat.util.Util;
import org.gfs.chat.util.VersionTracker;
import org.gfs.chat.util.dynamiclanguage.DynamicLanguageContextWrapper;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;
import rxdogtag2.RxDogTag;

/**
 * Will be called once when the TextSecure process is created.
 *
 * We're using this as an insertion point to patch up the Android PRNG disaster,
 * to initialize the job manager, and to check for GCM registration freshness.
 *
 * @author Moxie Marlinspike
 */
public class ApplicationContext extends MultiDexApplication implements AppForegroundObserver.Listener {

  private static final String TAG = Log.tag(ApplicationContext.class);

  public static ApplicationContext getInstance(Context context) {
    return (ApplicationContext)context.getApplicationContext();
  }

  @Override
  public void onCreate() {
    Tracer.getInstance().start("Application#onCreate()");
    AppStartup.getInstance().onApplicationCreate();
    SignalLocalMetrics.ColdStart.start();

    long startTime = System.currentTimeMillis();

    super.onCreate();

    AppStartup.getInstance().addBlocking("sqlcipher-init", () -> {
                              SqlCipherLibraryLoader.load();
                              SignalDatabase.init(this,
                                                  DatabaseSecretProvider.getOrCreateDatabaseSecret(this),
                                                  AttachmentSecretProvider.getInstance(this).getOrCreateAttachmentSecret());
                            })
                            .addBlocking("logging", () -> {
                              initializeLogging();
                              Log.i(TAG, "onCreate()");
                            })
                            .addBlocking("app-dependencies", this::initializeAppDependencies)
                            .addBlocking("anr-detector", this::startAnrDetector)
                            .addBlocking("security-provider", this::initializeSecurityProvider)
                            .addBlocking("crash-handling", this::initializeCrashHandling)
                            .addBlocking("rx-init", this::initializeRx)
                            .addBlocking("event-bus", () -> EventBus.builder().logNoSubscriberMessages(false).installDefaultEventBus())
                            .addBlocking("scrubber", () -> Scrubber.setIdentifierHmacKeyProvider(() -> SignalStore.svr().getOrCreateMasterKey().deriveLoggingKey()))
                            .addBlocking("first-launch", this::initializeFirstEverAppLaunch)
                            .addBlocking("app-migrations", this::initializeApplicationMigrations)
                            .addBlocking("lifecycle-observer", () -> AppDependencies.getAppForegroundObserver().addListener(this))
                            .addBlocking("message-retriever", this::initializeMessageRetrieval)
                            .addBlocking("dynamic-theme", () -> DynamicTheme.setDefaultDayNightMode(this))
                            .addBlocking("proxy-init", () -> {
                              if (SignalStore.proxy().isProxyEnabled()) {
                                Log.w(TAG, "Proxy detected. Enabling Conscrypt.setUseEngineSocketByDefault()");
                                ConscryptSignal.setUseEngineSocketByDefault(true);
                              }
                            })
                            .addBlocking("blob-provider", this::initializeBlobProvider)
                            .addBlocking("remote-config", RemoteConfig::init)
                            .addBlocking("ring-rtc", this::initializeRingRtc)
                            .addBlocking("glide", () -> SignalGlideModule.setRegisterGlideComponents(new SignalGlideComponents()))
                            .addBlocking("tracer", this::initializeTracer)
                            .addNonBlocking(() -> RegistrationUtil.maybeMarkRegistrationComplete())
                            .addNonBlocking(() -> Glide.get(this))
                            .addNonBlocking(this::cleanAvatarStorage)
                            .addNonBlocking(this::initializeRevealableMessageManager)
                            .addNonBlocking(this::initializePendingRetryReceiptManager)
                            .addNonBlocking(this::initializeScheduledMessageManager)
                            .addNonBlocking(this::initializeFcmCheck)
                            .addNonBlocking(PreKeysSyncJob::enqueueIfNeeded)
                            .addNonBlocking(this::initializePeriodicTasks)
                            .addNonBlocking(this::initializeCircumvention)
                            .addNonBlocking(this::initializeCleanup)
                            .addNonBlocking(this::initializeGlideCodecs)
                            .addNonBlocking(StorageSyncHelper::scheduleRoutineSync)
                            .addNonBlocking(this::beginJobLoop)
                            .addNonBlocking(EmojiSource::refresh)
                            .addNonBlocking(() -> AppDependencies.getGiphyMp4Cache().onAppStart(this))
                            .addNonBlocking(this::ensureProfileUploaded)
                            .addNonBlocking(() -> AppDependencies.getExpireStoriesManager().scheduleIfNecessary())
                            .addPostRender(() -> AppDependencies.getDeletedCallEventManager().scheduleIfNecessary())
                            .addPostRender(() -> RateLimitUtil.retryAllRateLimitedMessages(this))
                            .addPostRender(this::initializeExpiringMessageManager)
                            .addPostRender(this::initializeTrimThreadsByDateManager)
                            .addPostRender(RefreshSvrCredentialsJob::enqueueIfNecessary)
                            .addPostRender(() -> DownloadLatestEmojiDataJob.scheduleIfNecessary(this))
                            .addPostRender(EmojiSearchIndexDownloadJob::scheduleIfNecessary)
                            .addPostRender(() -> SignalDatabase.messageLog().trimOldMessages(System.currentTimeMillis(), RemoteConfig.retryRespondMaxAge()))
                            .addPostRender(() -> JumboEmoji.updateCurrentVersion(this))
                            .addPostRender(RetrieveRemoteAnnouncementsJob::enqueue)
                            .addPostRender(() -> AndroidTelecomUtil.registerPhoneAccount())
                            .addPostRender(() -> AppDependencies.getJobManager().add(new FontDownloaderJob()))
                            .addPostRender(CheckServiceReachabilityJob::enqueueIfNecessary)
                            .addPostRender(GroupV2UpdateSelfProfileKeyJob::enqueueForGroupsIfNecessary)
                            .addPostRender(StoryOnboardingDownloadJob.Companion::enqueueIfNeeded)
                            .addPostRender(PnpInitializeDevicesJob::enqueueIfNecessary)
                            .addPostRender(() -> AppDependencies.getExoPlayerPool().getPoolStats().getMaxUnreserved())
                            .addPostRender(() -> AppDependencies.getRecipientCache().warmUp())
                            .addPostRender(AccountConsistencyWorkerJob::enqueueIfNecessary)
                            .addPostRender(GroupRingCleanupJob::enqueue)
                            .addPostRender(LinkedDeviceInactiveCheckJob::enqueueIfNecessary)
                            .addPostRender(() -> ActiveCallManager.clearNotifications(this))
                            .addPostRender(() -> GroupSendEndorsementInternalNotifier.init())
                            .execute();

    Log.d(TAG, "onCreate() took " + (System.currentTimeMillis() - startTime) + " ms");
    SignalLocalMetrics.ColdStart.onApplicationCreateFinished();
    Tracer.getInstance().end("Application#onCreate()");
  }

  @Override
  public void onForeground() {
    long startTime = System.currentTimeMillis();
    Log.i(TAG, "App is now visible.");

    AppDependencies.getFrameRateTracker().start();
    AppDependencies.getMegaphoneRepository().onAppForegrounded();
    AppDependencies.getDeadlockDetector().start();
    InAppPaymentKeepAliveJob.enqueueAndTrackTimeIfNecessary();
    FcmFetchManager.onForeground(this);
    startAnrDetector();

    SignalExecutors.BOUNDED.execute(() -> {
      InAppPaymentAuthCheckJob.enqueueIfNeeded();
      RemoteConfig.refreshIfNecessary();
      RetrieveProfileJob.enqueueRoutineFetchIfNecessary();
      executePendingContactSync();
      KeyCachingService.onAppForegrounded(this);
      AppDependencies.getShakeToReport().enable();
      checkBuildExpiration();
      MemoryTracker.start();

      long lastForegroundTime = SignalStore.misc().getLastForegroundTime();
      long currentTime        = System.currentTimeMillis();
      long timeDiff           = currentTime - lastForegroundTime;

      if (timeDiff < 0) {
        Log.w(TAG, "Time travel! The system clock has moved backwards. (currentTime: " + currentTime + " ms, lastForegroundTime: " + lastForegroundTime + " ms, diff: " + timeDiff + " ms)", true);
      }

      SignalStore.misc().setLastForegroundTime(currentTime);
    });

    Log.d(TAG, "onStart() took " + (System.currentTimeMillis() - startTime) + " ms");
  }

  @Override
  public void onBackground() {
    Log.i(TAG, "App is no longer visible.");
    KeyCachingService.onAppBackgrounded(this);
    AppDependencies.getMessageNotifier().clearVisibleThread();
    AppDependencies.getFrameRateTracker().stop();
    AppDependencies.getShakeToReport().disable();
    AppDependencies.getDeadlockDetector().stop();
    MemoryTracker.stop();
    AnrDetector.stop();
  }

  public void checkBuildExpiration() {
    if (Util.getTimeUntilBuildExpiry(SignalStore.misc().getEstimatedServerTime()) <= 0 && !SignalStore.misc().isClientDeprecated()) {
      Log.w(TAG, "Build potentially expired! Enqueing job to check.", true);
      AppDependencies.getJobManager().add(new BuildExpirationConfirmationJob());
    }
  }

  /**
   * Note: this is purposefully "started" twice -- once during application create, and once during foreground.
   * This is so we can capture ANR's that happen on boot before the foreground event.
   */
  private void startAnrDetector() {
    AnrDetector.start(TimeUnit.SECONDS.toMillis(5), RemoteConfig::internalUser, (dumps) -> {
      LogDatabase.getInstance(this).anrs().save(System.currentTimeMillis(), dumps);
      return Unit.INSTANCE;
    });
  }

  private void initializeSecurityProvider() {
    int aesPosition = Security.insertProviderAt(new AesGcmProvider(), 1);
    Log.i(TAG, "Installed AesGcmProvider: " + aesPosition);

    if (aesPosition < 0) {
      Log.e(TAG, "Failed to install AesGcmProvider()");
      throw new ProviderInitializationException();
    }

    int conscryptPosition = Security.insertProviderAt(ConscryptSignal.newProvider(), 2);
    Log.i(TAG, "Installed Conscrypt provider: " + conscryptPosition);

    if (conscryptPosition < 0) {
      Log.w(TAG, "Did not install Conscrypt provider. May already be present.");
    }
  }

  @VisibleForTesting
  protected void initializeLogging() {
    Log.initialize(RemoteConfig::internalUser, new AndroidLogger(), new PersistentLogger(this));

    SignalProtocolLoggerProvider.setProvider(new CustomSignalProtocolLogger());
    SignalProtocolLoggerProvider.initializeLogging(BuildConfig.LIBSIGNAL_LOG_LEVEL);

    SignalExecutors.UNBOUNDED.execute(() -> {
      Log.blockUntilAllWritesFinished();
      LogDatabase.getInstance(this).logs().trimToSize();
      LogDatabase.getInstance(this).crashes().trimToSize();
    });
  }

  private void initializeCrashHandling() {
    final Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(new SignalUncaughtExceptionHandler(originalHandler));
  }

  private void initializeRx() {
    RxDogTag.install();
    RxJavaPlugins.setInitIoSchedulerHandler(schedulerSupplier -> Schedulers.from(SignalExecutors.BOUNDED_IO, true, false));
    RxJavaPlugins.setInitComputationSchedulerHandler(schedulerSupplier -> Schedulers.from(SignalExecutors.BOUNDED, true, false));
    RxJavaPlugins.setErrorHandler(e -> {
      boolean wasWrapped = false;
      while ((e instanceof UndeliverableException || e instanceof AssertionError || e instanceof OnErrorNotImplementedException) && e.getCause() != null) {
        wasWrapped = true;
        e = e.getCause();
      }

      if (wasWrapped && (e instanceof SocketException || e instanceof SocketTimeoutException || e instanceof InterruptedException)) {
        return;
      }

      Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
      if (uncaughtExceptionHandler == null) {
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
      }

      uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e);
    });
  }

  private void initializeApplicationMigrations() {
    ApplicationMigrations.onApplicationCreate(this, AppDependencies.getJobManager());
  }

  public void initializeMessageRetrieval() {
    AppDependencies.getIncomingMessageObserver();
  }

  @VisibleForTesting
  void initializeAppDependencies() {
    AppDependencies.init(this, new ApplicationDependencyProvider(this));
  }

  private void initializeFirstEverAppLaunch() {
    if (TextSecurePreferences.getFirstInstallVersion(this) == -1) {
      if (!SignalDatabase.databaseFileExists(this) || VersionTracker.getDaysSinceFirstInstalled(this) < 365) {
        Log.i(TAG, "First ever app launch!");
        AppInitialization.onFirstEverAppLaunch(this);
      }

      Log.i(TAG, "Setting first install version to " + BuildConfig.CANONICAL_VERSION_CODE);
      TextSecurePreferences.setFirstInstallVersion(this, BuildConfig.CANONICAL_VERSION_CODE);
    } else if (!SignalStore.settings().getPassphraseDisabled() && VersionTracker.getDaysSinceFirstInstalled(this) < 90) {
      Log.i(TAG, "Detected a new install that doesn't have passphrases disabled -- assuming bad initialization.");
      AppInitialization.onRepairFirstEverAppLaunch(this);
    } else if (!SignalStore.settings().getPassphraseDisabled() && VersionTracker.getDaysSinceFirstInstalled(this) < 912) {
      Log.i(TAG, "Detected a not-recent install that doesn't have passphrases disabled -- disabling now.");
      SignalStore.settings().setPassphraseDisabled(true);
    }
  }

  private void initializeFcmCheck() {
    if (SignalStore.account().isRegistered()) {
      long lastSetTime = SignalStore.account().getFcmTokenLastSetTime();
      long nextSetTime = lastSetTime + TimeUnit.HOURS.toMillis(6);
      long now         = System.currentTimeMillis();

      if (SignalStore.account().getFcmToken() == null || nextSetTime <= now || lastSetTime > now) {
        AppDependencies.getJobManager().add(new FcmRefreshJob());
      }
    }
  }

  private void initializeExpiringMessageManager() {
    AppDependencies.getExpiringMessageManager().checkSchedule();
  }

  private void initializeRevealableMessageManager() {
    AppDependencies.getViewOnceMessageManager().scheduleIfNecessary();
  }

  private void initializePendingRetryReceiptManager() {
    AppDependencies.getPendingRetryReceiptManager().scheduleIfNecessary();
  }

  private void initializeScheduledMessageManager() {
    AppDependencies.getScheduledMessageManager().scheduleIfNecessary();
  }

  private void initializeTrimThreadsByDateManager() {
    KeepMessagesDuration keepMessagesDuration = SignalStore.settings().getKeepMessagesDuration();
    if (keepMessagesDuration != KeepMessagesDuration.FOREVER) {
      AppDependencies.getTrimThreadsByDateManager().scheduleIfNecessary();
    }
  }

  private void initializeTracer() {
    if (RemoteConfig.internalUser()) {
      Tracer.getInstance().setMaxBufferSize(35_000);
    }
  }

  private void initializePeriodicTasks() {
    RotateSignedPreKeyListener.schedule(this);
    DirectoryRefreshListener.schedule(this);
    LocalBackupListener.schedule(this);
    MessageBackupListener.schedule(this);
    RotateSenderCertificateListener.schedule(this);
    RoutineMessageFetchReceiver.startOrUpdateAlarm(this);
    AnalyzeDatabaseAlarmListener.schedule(this);

    if (BuildConfig.MANAGES_APP_UPDATES) {
      ApkUpdateRefreshListener.schedule(this);
    }
  }

  private void initializeRingRtc() {
    try {
      Map<String, String> fieldTrials = new HashMap<>();
      if (RemoteConfig.callingFieldTrialAnyAddressPortsKillSwitch()) {
        fieldTrials.put("RingRTC-AnyAddressPortsKillSwitch", "Enabled");
      }
      CallManager.initialize(this, new RingRtcLogger(), fieldTrials);
    } catch (UnsatisfiedLinkError e) {
      throw new AssertionError("Unable to load ringrtc library", e);
    }
  }

  @WorkerThread
  private void initializeCircumvention() {
    if (AppDependencies.getSignalServiceNetworkAccess().isCensored()) {
      try {
        ProviderInstaller.installIfNeeded(ApplicationContext.this);
      } catch (Throwable t) {
        Log.w(TAG, t);
      }
    }
  }

  private void ensureProfileUploaded() {
    if (SignalStore.account().isRegistered() && !SignalStore.registration().hasUploadedProfile() && !Recipient.self().getProfileName().isEmpty()) {
      Log.w(TAG, "User has a profile, but has not uploaded one. Uploading now.");
      AppDependencies.getJobManager().add(new ProfileUploadJob());
    }
  }

  private void executePendingContactSync() {
    if (TextSecurePreferences.needsFullContactSync(this)) {
      AppDependencies.getJobManager().add(new MultiDeviceContactUpdateJob(true));
    }
  }

  @VisibleForTesting
  protected void beginJobLoop() {
    AppDependencies.getJobManager().beginJobLoop();
  }

  @WorkerThread
  private void initializeBlobProvider() {
    BlobProvider.getInstance().initialize(this);
  }

  @WorkerThread
  private void cleanAvatarStorage() {
    AvatarPickerStorage.cleanOrphans(this);
  }

  @WorkerThread
  private void initializeCleanup() {
    int deleted = SignalDatabase.attachments().deleteAbandonedPreuploadedAttachments();
    Log.i(TAG, "Deleted " + deleted + " abandoned attachments.");
  }

  private void initializeGlideCodecs() {
    SignalGlideCodecs.setLogProvider(new org.signal.glide.Log.Provider() {
      @Override
      public void v(@NonNull String tag, @NonNull String message) {
        Log.v(tag, message);
      }

      @Override
      public void d(@NonNull String tag, @NonNull String message) {
        Log.d(tag, message);
      }

      @Override
      public void i(@NonNull String tag, @NonNull String message) {
        Log.i(tag, message);
      }

      @Override
      public void w(@NonNull String tag, @NonNull String message) {
        Log.w(tag, message);
      }

      @Override
      public void e(@NonNull String tag, @NonNull String message, @Nullable Throwable throwable) {
        Log.e(tag, message, throwable);
      }
    });
  }

  @Override
  protected void attachBaseContext(Context base) {
    DynamicLanguageContextWrapper.updateContext(base);
    super.attachBaseContext(base);
  }

  private static class ProviderInitializationException extends RuntimeException {
  }
}
