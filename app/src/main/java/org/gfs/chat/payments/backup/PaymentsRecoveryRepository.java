package org.gfs.chat.payments.backup;

import androidx.annotation.NonNull;

import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.payments.Mnemonic;

public final class PaymentsRecoveryRepository {
  public @NonNull Mnemonic getMnemonic() {
    return SignalStore.payments().getPaymentsMnemonic();
  }
}
