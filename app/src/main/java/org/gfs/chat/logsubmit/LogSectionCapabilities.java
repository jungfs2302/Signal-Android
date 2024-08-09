package org.gfs.chat.logsubmit;

import android.content.Context;

import androidx.annotation.NonNull;

import org.gfs.chat.AppCapabilities;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.database.model.RecipientRecord;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.recipients.Recipient;
import org.whispersystems.signalservice.api.account.AccountAttributes;

public final class LogSectionCapabilities implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "CAPABILITIES";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    if (!SignalStore.account().isRegistered()) {
      return "Unregistered";
    }

    if (SignalStore.account().getE164() == null || SignalStore.account().getAci() == null) {
      return "Self not yet available!";
    }

    Recipient self = Recipient.self();

    AccountAttributes.Capabilities localCapabilities  = AppCapabilities.getCapabilities(false);
    RecipientRecord.Capabilities   globalCapabilities = SignalDatabase.recipients().getCapabilities(self.getId());

    StringBuilder builder = new StringBuilder().append("-- Local").append("\n")
                                               .append("DeleteSync: ").append(localCapabilities.getDeleteSync()).append("\n")
                                               .append("\n")
                                               .append("-- Global").append("\n");

    if (globalCapabilities != null) {
      builder.append("DeleteSync: ").append(globalCapabilities.getDeleteSync()).append("\n");
      builder.append("\n");
    } else {
      builder.append("Self not found!");
    }

    return builder;
  }
}
