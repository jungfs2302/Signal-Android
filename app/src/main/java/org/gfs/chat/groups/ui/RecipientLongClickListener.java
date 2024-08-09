package org.gfs.chat.groups.ui;

import androidx.annotation.NonNull;

import org.gfs.chat.recipients.Recipient;

public interface RecipientLongClickListener {
  boolean onLongClick(@NonNull Recipient recipient);
}
