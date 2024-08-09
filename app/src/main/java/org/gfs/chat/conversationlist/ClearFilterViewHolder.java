package org.gfs.chat.conversationlist;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.gfs.chat.R;
import org.gfs.chat.conversationlist.model.Conversation;
import org.gfs.chat.conversationlist.model.ConversationReader;

class ClearFilterViewHolder extends RecyclerView.ViewHolder {

  private final View tip;

  ClearFilterViewHolder(@NonNull View itemView, OnClearFilterClickListener listener) {
    super(itemView);
    tip = itemView.findViewById(R.id.clear_filter_tip);
    itemView.findViewById(R.id.clear_filter).setOnClickListener(v -> {
      listener.onClearFilterClick();
    });
  }

  void bind(@NonNull Conversation conversation) {
    if (conversation.getThreadRecord().getType() == ConversationReader.TYPE_SHOW_TIP) {
      tip.setVisibility(View.VISIBLE);
    } else {
      tip.setVisibility(View.GONE);
    }
  }

  interface OnClearFilterClickListener {
    void onClearFilterClick();
  }
}
