package org.gfs.chat.reactions.any;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.annimon.stream.Stream;

import org.gfs.chat.R;
import org.gfs.chat.components.emoji.Emoji;
import org.gfs.chat.components.emoji.EmojiPageModel;
import org.gfs.chat.components.emoji.RecentEmojiPageModel;

import java.util.List;

/**
 * Contains the Emojis that have been used in reactions for a given message.
 */
class ThisMessageEmojiPageModel implements EmojiPageModel {

  private final List<String> emoji;

  ThisMessageEmojiPageModel(@NonNull List<String> emoji) {
    this.emoji = emoji;
  }

  @Override
  public String getKey() {
    return RecentEmojiPageModel.KEY;
  }

  @Override
  public int getIconAttr() {
    return R.attr.emoji_category_recent;
  }

  @Override
  public @NonNull List<String> getEmoji() {
    return emoji;
  }

  @Override
  public @NonNull List<Emoji> getDisplayEmoji() {
    return Stream.of(getEmoji()).map(Emoji::new).toList();
  }

  @Override
  public @Nullable Uri getSpriteUri() {
    return null;
  }

  @Override
  public boolean isDynamic() {
    return true;
  }
}
