package org.gfs.chat.payments.preferences;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.logging.Log;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobs.PaymentLedgerUpdateJob;
import org.gfs.chat.jobs.ProfileUploadJob;
import org.gfs.chat.jobs.SendPaymentsActivatedJob;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.util.AsynchronousCallback;
import org.gfs.chat.util.ProfileUtil;
import org.whispersystems.signalservice.api.push.exceptions.NonSuccessfulResponseCodeException;
import org.whispersystems.signalservice.internal.push.exceptions.PaymentsRegionException;

import java.io.IOException;

public class PaymentsHomeRepository {

  private static final String TAG = Log.tag(PaymentsHomeRepository.class);

  public void activatePayments(@NonNull AsynchronousCallback.WorkerThread<Void, Error> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      SignalStore.payments().setMobileCoinPaymentsEnabled(true);
      try {
        ProfileUtil.uploadProfile(AppDependencies.getApplication());
        AppDependencies.getJobManager()
                       .startChain(PaymentLedgerUpdateJob.updateLedger())
                       .then(new SendPaymentsActivatedJob())
                       .enqueue();
        callback.onComplete(null);
      } catch (PaymentsRegionException e) {
        SignalStore.payments().setMobileCoinPaymentsEnabled(false);
        Log.w(TAG, "Problem enabling payments in region", e);
        callback.onError(Error.RegionError);
      } catch (NonSuccessfulResponseCodeException e) {
        SignalStore.payments().setMobileCoinPaymentsEnabled(false);
        Log.w(TAG, "Problem enabling payments", e);
        callback.onError(Error.NetworkError);
      } catch (IOException e) {
        SignalStore.payments().setMobileCoinPaymentsEnabled(false);
        Log.w(TAG, "Problem enabling payments", e);
        tryToRestoreProfile();
        callback.onError(Error.NetworkError);
      }
    });
  }

  private void tryToRestoreProfile() {
    try {
      ProfileUtil.uploadProfile(AppDependencies.getApplication());
      Log.i(TAG, "Restored profile");
    } catch (IOException e) {
      Log.w(TAG, "Problem uploading profile", e);
    }
  }

  public void deactivatePayments(@NonNull Consumer<Boolean> consumer) {
    SignalExecutors.BOUNDED.execute(() -> {
      SignalStore.payments().setMobileCoinPaymentsEnabled(false);
      AppDependencies.getJobManager().add(new ProfileUploadJob());
      consumer.accept(!SignalStore.payments().mobileCoinPaymentsEnabled());
    });
  }

  public enum Error {
    NetworkError,
    RegionError
  }
}
