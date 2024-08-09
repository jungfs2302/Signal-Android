package org.gfs.chat.conversation.colors.ui.custom

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import org.gfs.chat.conversation.colors.ChatColors
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.wallpaper.ChatWallpaper

class CustomChatColorCreatorRepository(private val context: Context) {
  fun loadColors(chatColorsId: ChatColors.Id, consumer: (ChatColors) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      val chatColors = SignalDatabase.chatColors.getById(chatColorsId)
      consumer(chatColors)
    }
  }

  fun getWallpaper(recipientId: RecipientId?, consumer: (ChatWallpaper?) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      if (recipientId != null) {
        val recipient = Recipient.resolved(recipientId)
        consumer(recipient.wallpaper)
      } else {
        consumer(SignalStore.wallpaper.wallpaper)
      }
    }
  }

  fun setChatColors(chatColors: ChatColors, consumer: (ChatColors) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      val savedColors = SignalDatabase.chatColors.saveChatColors(chatColors)
      consumer(savedColors)
    }
  }

  fun getUsageCount(chatColorsId: ChatColors.Id, consumer: (Int) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      val recipientsDatabase = SignalDatabase.recipients

      consumer(recipientsDatabase.getColorUsageCount(chatColorsId))
    }
  }
}
