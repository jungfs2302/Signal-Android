package org.gfs.chat.conversation.ui.mentions;

import androidx.annotation.NonNull;

import org.gfs.chat.recipients.Recipient;
import org.gfs.chat.util.viewholders.RecipientMappingModel;

public final class MentionViewState extends RecipientMappingModel<MentionViewState> {

  private final Recipient recipient;

  public MentionViewState(@NonNull Recipient recipient) {
    this.recipient = recipient;
  }

  @Override
  public @NonNull Recipient getRecipient() {
    return recipient;
  }
}
