package org.gfs.chat.registration;

import org.signal.core.util.logging.Log;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobs.DirectoryRefreshJob;
import org.gfs.chat.jobs.RefreshAttributesJob;
import org.gfs.chat.jobs.StorageSyncJob;
import org.gfs.chat.keyvalue.PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.recipients.Recipient;

public final class RegistrationUtil {

  private static final String TAG = Log.tag(RegistrationUtil.class);

  private RegistrationUtil() {}

  /**
   * There's several events where a registration may or may not be considered complete based on what
   * path a user has taken. This will only truly mark registration as complete if all of the
   * requirements are met.
   */
  public static void maybeMarkRegistrationComplete() {
    if (!SignalStore.registration().isRegistrationComplete() &&
        SignalStore.account().isRegistered() &&
        !Recipient.self().getProfileName().isEmpty() &&
        (SignalStore.svr().hasPin() || SignalStore.svr().hasOptedOut()))
    {
      Log.i(TAG, "Marking registration completed.", new Throwable());
      SignalStore.registration().setRegistrationComplete();
      SignalStore.registration().clearLocalRegistrationMetadata();

      if (SignalStore.phoneNumberPrivacy().getPhoneNumberDiscoverabilityMode() == PhoneNumberDiscoverabilityMode.UNDECIDED) {
        Log.w(TAG, "Phone number discoverability mode is still UNDECIDED. Setting to DISCOVERABLE.");
        SignalStore.phoneNumberPrivacy().setPhoneNumberDiscoverabilityMode(PhoneNumberDiscoverabilityMode.DISCOVERABLE);
      }

      AppDependencies.getJobManager().startChain(new RefreshAttributesJob())
                     .then(new StorageSyncJob())
                     .then(new DirectoryRefreshJob(false))
                     .enqueue();

    } else if (!SignalStore.registration().isRegistrationComplete()) {
      Log.i(TAG, "Registration is not yet complete.", new Throwable());
    }
  }
}
