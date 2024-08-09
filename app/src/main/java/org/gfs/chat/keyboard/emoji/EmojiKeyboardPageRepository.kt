package org.gfs.chat.keyboard.emoji

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import org.gfs.chat.components.emoji.EmojiPageModel
import org.gfs.chat.components.emoji.RecentEmojiPageModel
import org.gfs.chat.emoji.EmojiSource.Companion.latest
import org.gfs.chat.util.TextSecurePreferences
import java.util.function.Consumer

class EmojiKeyboardPageRepository(private val context: Context) {
  fun getEmoji(consumer: Consumer<List<EmojiPageModel>>) {
    SignalExecutors.BOUNDED.execute {
      val list = mutableListOf<EmojiPageModel>()
      list += RecentEmojiPageModel(context, TextSecurePreferences.RECENT_STORAGE_KEY)
      list += latest.displayPages
      consumer.accept(list)
    }
  }
}
