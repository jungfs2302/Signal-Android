package org.gfs.chat.conversation.colors.ui

import androidx.lifecycle.LiveData
import org.signal.core.util.concurrent.SignalExecutors
import org.gfs.chat.conversation.colors.ChatColors
import org.gfs.chat.conversation.colors.ChatColorsPalette
import org.gfs.chat.database.ChatColorsTable
import org.gfs.chat.database.DatabaseObserver
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.util.concurrent.SerialMonoLifoExecutor
import java.util.concurrent.Executor

class ChatColorsOptionsLiveData : LiveData<List<ChatColors>>() {
  private val chatColorsTable: ChatColorsTable = SignalDatabase.chatColors
  private val observer: DatabaseObserver.Observer = DatabaseObserver.Observer { refreshChatColors() }
  private val executor: Executor = SerialMonoLifoExecutor(SignalExecutors.BOUNDED)

  override fun onActive() {
    refreshChatColors()
    AppDependencies.databaseObserver.registerChatColorsObserver(observer)
  }

  override fun onInactive() {
    AppDependencies.databaseObserver.unregisterObserver(observer)
  }

  private fun refreshChatColors() {
    executor.execute {
      val options = mutableListOf<ChatColors>().apply {
        addAll(ChatColorsPalette.Bubbles.all)
        addAll(chatColorsTable.getSavedChatColors())
      }

      postValue(options)
    }
  }
}
