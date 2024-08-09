package org.gfs.chat.dependencies;

import androidx.annotation.NonNull;

import org.signal.core.util.concurrent.DeadlockDetector;
import org.signal.libsignal.net.Network;
import org.signal.libsignal.zkgroup.profiles.ClientZkProfileOperations;
import org.signal.libsignal.zkgroup.receipts.ClientZkReceiptOperations;
import org.gfs.chat.components.TypingStatusRepository;
import org.gfs.chat.components.TypingStatusSender;
import org.gfs.chat.crypto.storage.SignalServiceDataStoreImpl;
import org.gfs.chat.database.DatabaseObserver;
import org.gfs.chat.database.PendingRetryReceiptCache;
import org.gfs.chat.jobmanager.JobManager;
import org.gfs.chat.megaphone.MegaphoneRepository;
import org.gfs.chat.messages.IncomingMessageObserver;
import org.gfs.chat.notifications.MessageNotifier;
import org.gfs.chat.payments.Payments;
import org.gfs.chat.push.SignalServiceNetworkAccess;
import org.gfs.chat.recipients.LiveRecipientCache;
import org.gfs.chat.revealable.ViewOnceMessageManager;
import org.gfs.chat.service.DeletedCallEventManager;
import org.gfs.chat.service.ExpiringMessageManager;
import org.gfs.chat.service.ExpiringStoriesManager;
import org.gfs.chat.service.PendingRetryReceiptManager;
import org.gfs.chat.service.ScheduledMessageManager;
import org.gfs.chat.service.TrimThreadsByDateManager;
import org.gfs.chat.service.webrtc.SignalCallManager;
import org.gfs.chat.shakereport.ShakeToReport;
import org.gfs.chat.util.AppForegroundObserver;
import org.gfs.chat.util.EarlyMessageCache;
import org.gfs.chat.util.FrameRateTracker;
import org.gfs.chat.video.exo.GiphyMp4Cache;
import org.gfs.chat.video.exo.SimpleExoPlayerPool;
import org.gfs.chat.webrtc.audio.AudioManagerCompat;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.SignalServiceDataStore;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.SignalWebSocket;
import org.whispersystems.signalservice.api.groupsv2.GroupsV2Operations;
import org.whispersystems.signalservice.api.services.CallLinksService;
import org.whispersystems.signalservice.api.services.DonationsService;
import org.whispersystems.signalservice.api.services.ProfileService;
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration;

import java.util.function.Supplier;

import static org.mockito.Mockito.mock;

@SuppressWarnings("ConstantConditions")
public class MockApplicationDependencyProvider implements AppDependencies.Provider {
  @Override
  public @NonNull GroupsV2Operations provideGroupsV2Operations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return null;
  }

  @Override
  public @NonNull SignalServiceAccountManager provideSignalServiceAccountManager(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return null;
  }

  @Override
  public @NonNull SignalServiceMessageSender provideSignalServiceMessageSender(@NonNull SignalWebSocket signalWebSocket, @NonNull SignalServiceDataStore protocolStore, @NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return null;
  }

  @Override
  public @NonNull SignalServiceMessageReceiver provideSignalServiceMessageReceiver(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return null;
  }

  @Override
  public @NonNull SignalServiceNetworkAccess provideSignalServiceNetworkAccess() {
    return null;
  }

  @Override
  public @NonNull LiveRecipientCache provideRecipientCache() {
    return null;
  }

  @Override
  public @NonNull JobManager provideJobManager() {
    return null;
  }

  @Override
  public @NonNull FrameRateTracker provideFrameRateTracker() {
    return null;
  }

  @Override
  public @NonNull MegaphoneRepository provideMegaphoneRepository() {
    return null;
  }

  @Override
  public @NonNull EarlyMessageCache provideEarlyMessageCache() {
    return null;
  }

  @Override
  public @NonNull MessageNotifier provideMessageNotifier() {
    return null;
  }

  @Override
  public @NonNull IncomingMessageObserver provideIncomingMessageObserver() {
    return null;
  }

  @Override
  public @NonNull TrimThreadsByDateManager provideTrimThreadsByDateManager() {
    return null;
  }

  @Override
  public @NonNull ViewOnceMessageManager provideViewOnceMessageManager() {
    return null;
  }

  @Override
  public @NonNull ExpiringStoriesManager provideExpiringStoriesManager() {
    return null;
  }

  @Override
  public @NonNull ExpiringMessageManager provideExpiringMessageManager() {
    return null;
  }

  @Override
  public @NonNull DeletedCallEventManager provideDeletedCallEventManager() {
    return null;
  }

  @Override
  public @NonNull TypingStatusRepository provideTypingStatusRepository() {
    return null;
  }

  @Override
  public @NonNull TypingStatusSender provideTypingStatusSender() {
    return null;
  }

  @Override
  public @NonNull DatabaseObserver provideDatabaseObserver() {
    return mock(DatabaseObserver.class);
  }

  @Override
  public @NonNull Payments providePayments(@NonNull SignalServiceAccountManager signalServiceAccountManager) {
    return null;
  }

  @Override
  public @NonNull ShakeToReport provideShakeToReport() {
    return null;
  }

  @Override
  public @NonNull AppForegroundObserver provideAppForegroundObserver() {
    return mock(AppForegroundObserver.class);
  }

  @Override
  public @NonNull SignalCallManager provideSignalCallManager() {
    return null;
  }

  @Override
  public @NonNull PendingRetryReceiptManager providePendingRetryReceiptManager() {
    return null;
  }

  @Override
  public @NonNull PendingRetryReceiptCache providePendingRetryReceiptCache() {
    return null;
  }

  @Override
  public @NonNull SignalWebSocket provideSignalWebSocket(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier, @NonNull Supplier<Network> libSignalNetworkSupplier) {
    return null;
  }

  @Override
  public @NonNull SignalServiceDataStoreImpl provideProtocolStore() {
    return null;
  }

  @Override
  public @NonNull GiphyMp4Cache provideGiphyMp4Cache() {
    return null;
  }

  @Override
  public @NonNull SimpleExoPlayerPool provideExoPlayerPool() {
    return null;
  }

  @Override
  public @NonNull AudioManagerCompat provideAndroidCallAudioManager() {
    return null;
  }

  @Override
  public @NonNull DonationsService provideDonationsService(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return null;
  }

  @NonNull @Override public CallLinksService provideCallLinksService(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return null;
  }

  @Override
  public @NonNull ProfileService provideProfileService(@NonNull ClientZkProfileOperations profileOperations, @NonNull SignalServiceMessageReceiver signalServiceMessageReceiver, @NonNull SignalWebSocket signalWebSocket) {
    return null;
  }

  @Override
  public @NonNull DeadlockDetector provideDeadlockDetector() {
    return null;
  }

  @Override
  public @NonNull ClientZkReceiptOperations provideClientZkReceiptOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return null;
  }

  @Override
  public @NonNull ScheduledMessageManager provideScheduledMessageManager() {
    return null;
  }

  @Override
  public @NonNull Network provideLibsignalNetwork(@NonNull SignalServiceConfiguration config) {
    return null;
  }
}
