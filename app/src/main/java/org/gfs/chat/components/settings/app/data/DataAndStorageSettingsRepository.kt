package org.gfs.chat.components.settings.app.data

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.dependencies.AppDependencies

class DataAndStorageSettingsRepository {

  private val context: Context = AppDependencies.application

  fun getTotalStorageUse(consumer: (Long) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      val breakdown = SignalDatabase.media.getStorageBreakdown()

      consumer(listOf(breakdown.audioSize, breakdown.documentSize, breakdown.photoSize, breakdown.videoSize).sum())
    }
  }
}
