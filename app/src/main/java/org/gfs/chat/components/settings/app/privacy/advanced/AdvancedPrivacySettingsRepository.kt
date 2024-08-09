package org.gfs.chat.components.settings.app.privacy.advanced

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.installations.FirebaseInstallations
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobs.MultiDeviceConfigurationUpdateJob
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.storage.StorageSyncHelper
import org.gfs.chat.util.TextSecurePreferences
import org.whispersystems.signalservice.api.push.exceptions.AuthorizationFailedException
import java.io.IOException
import java.util.Optional
import java.util.concurrent.ExecutionException

private val TAG = Log.tag(AdvancedPrivacySettingsRepository::class.java)

class AdvancedPrivacySettingsRepository(private val context: Context) {

  fun disablePushMessages(consumer: (DisablePushMessagesResult) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      val result = try {
        val accountManager = AppDependencies.signalServiceAccountManager
        try {
          accountManager.setGcmId(Optional.empty())
        } catch (e: AuthorizationFailedException) {
          Log.w(TAG, e)
        }
        if (SignalStore.account.fcmEnabled) {
          Tasks.await(FirebaseInstallations.getInstance().delete())
        }
        DisablePushMessagesResult.SUCCESS
      } catch (ioe: IOException) {
        Log.w(TAG, ioe)
        DisablePushMessagesResult.NETWORK_ERROR
      } catch (e: InterruptedException) {
        Log.w(TAG, "Interrupted while deleting", e)
        DisablePushMessagesResult.NETWORK_ERROR
      } catch (e: ExecutionException) {
        Log.w(TAG, "Error deleting", e.cause)
        DisablePushMessagesResult.NETWORK_ERROR
      }

      consumer(result)
    }
  }

  fun syncShowSealedSenderIconState() {
    SignalExecutors.BOUNDED.execute {
      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      StorageSyncHelper.scheduleSyncForDataChange()
      AppDependencies.jobManager.add(
        MultiDeviceConfigurationUpdateJob(
          TextSecurePreferences.isReadReceiptsEnabled(context),
          TextSecurePreferences.isTypingIndicatorsEnabled(context),
          TextSecurePreferences.isShowUnidentifiedDeliveryIndicatorsEnabled(context),
          SignalStore.settings.isLinkPreviewsEnabled
        )
      )
    }
  }

  enum class DisablePushMessagesResult {
    SUCCESS,
    NETWORK_ERROR
  }
}
