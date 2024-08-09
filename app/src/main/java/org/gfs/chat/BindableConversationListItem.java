package org.gfs.chat;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.RequestManager;

import org.gfs.chat.conversationlist.model.ConversationSet;
import org.gfs.chat.database.model.ThreadRecord;

import java.util.Locale;
import java.util.Set;

public interface BindableConversationListItem extends Unbindable {

  void bind(@NonNull LifecycleOwner lifecycleOwner,
            @NonNull ThreadRecord thread,
            @NonNull RequestManager requestManager, @NonNull Locale locale,
            @NonNull Set<Long> typingThreads,
            @NonNull ConversationSet selectedConversations);

  void setSelectedConversations(@NonNull ConversationSet conversations);
  void updateTypingIndicator(@NonNull Set<Long> typingThreads);
  void updateTimestamp();
}
