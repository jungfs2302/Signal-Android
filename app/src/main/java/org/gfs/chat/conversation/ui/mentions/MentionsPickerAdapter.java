package org.gfs.chat.conversation.ui.mentions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.gfs.chat.R;
import org.gfs.chat.util.adapter.mapping.MappingAdapter;
import org.gfs.chat.util.adapter.mapping.MappingModel;
import org.gfs.chat.util.viewholders.RecipientViewHolder;
import org.gfs.chat.util.viewholders.RecipientViewHolder.EventListener;

import java.util.List;

public class MentionsPickerAdapter extends MappingAdapter {
  private final Runnable currentListChangedListener;

  public MentionsPickerAdapter(@Nullable EventListener<MentionViewState> listener, @NonNull Runnable currentListChangedListener) {
    this.currentListChangedListener = currentListChangedListener;
    registerFactory(MentionViewState.class, RecipientViewHolder.createFactory(R.layout.mentions_picker_recipient_list_item, listener));
  }

  @Override
  public void onCurrentListChanged(@NonNull List<MappingModel<?>> previousList, @NonNull List<MappingModel<?>> currentList) {
    super.onCurrentListChanged(previousList, currentList);
    currentListChangedListener.run();
  }
}
