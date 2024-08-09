package org.gfs.chat.groups.ui;

import androidx.annotation.NonNull;

import org.gfs.chat.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}
