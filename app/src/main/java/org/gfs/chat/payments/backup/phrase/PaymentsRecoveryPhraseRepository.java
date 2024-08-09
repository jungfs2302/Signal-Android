package org.gfs.chat.payments.backup.phrase;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.core.util.logging.Log;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobs.PaymentLedgerUpdateJob;
import org.gfs.chat.jobs.ProfileUploadJob;
import org.gfs.chat.keyvalue.PaymentsValues;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.util.Util;

import java.util.List;

class PaymentsRecoveryPhraseRepository {

  private static final String TAG = Log.tag(PaymentsRecoveryPhraseRepository.class);

  void restoreMnemonic(@NonNull List<String> words,
                       @NonNull Consumer<PaymentsValues.WalletRestoreResult> resultConsumer)
  {
    SignalExecutors.BOUNDED.execute(() -> {
      String                             mnemonic = Util.join(words, " ");
      PaymentsValues.WalletRestoreResult result   = SignalStore.payments().restoreWallet(mnemonic);

      switch (result) {
        case ENTROPY_CHANGED:
          Log.i(TAG, "restoreMnemonic: mnemonic resulted in entropy mismatch, flushing cached values");
          SignalDatabase.payments().deleteAll();
          AppDependencies.getPayments().closeWallet();
          updateProfileAndFetchLedger();
          break;
        case ENTROPY_UNCHANGED:
          Log.i(TAG, "restoreMnemonic: mnemonic resulted in entropy match, no flush needed.");
          updateProfileAndFetchLedger();
          break;
        case MNEMONIC_ERROR:
          Log.w(TAG, "restoreMnemonic: failed to restore wallet from given mnemonic.");
          break;
      }

      resultConsumer.accept(result);
    });
  }

  private void updateProfileAndFetchLedger() {
    AppDependencies.getJobManager()
                   .startChain(new ProfileUploadJob())
                   .then(PaymentLedgerUpdateJob.updateLedger())
                   .enqueue();
  }
}
