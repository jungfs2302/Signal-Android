package org.gfs.chat.messagedetails;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.gfs.chat.database.model.MessageRecord;
import org.gfs.chat.databinding.MessageDetailsViewEditHistoryBinding;

public class ViewEditHistoryViewHolder extends RecyclerView.ViewHolder {

  private final org.gfs.chat.databinding.MessageDetailsViewEditHistoryBinding binding;
  private final MessageDetailsAdapter.Callbacks                                             callbacks;

  public ViewEditHistoryViewHolder(@NonNull MessageDetailsViewEditHistoryBinding binding, @NonNull MessageDetailsAdapter.Callbacks callbacks) {
    super(binding.getRoot());
    this.binding   = binding;
    this.callbacks = callbacks;
  }

  public void bind(@NonNull MessageRecord record) {
    binding.viewEditHistory.setOnClickListener(v -> callbacks.onViewEditHistoryClicked(record));
  }
}
