/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.jobs

import org.signal.core.util.logging.Log
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobmanager.Job
import org.gfs.chat.jobmanager.JsonJobData
import org.gfs.chat.jobmanager.impl.NetworkConstraint
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.util.RemoteConfig
import java.util.concurrent.TimeUnit

/**
 * PeekJob for refreshing call link data. Small lifespan, because these are only expected to run
 * while the Calls Tab is in the foreground.
 *
 *
 * While we may not necessarily require the weight of the job for this use-case, there are some nice
 * properties around deduplication and lifetimes that jobs provide.
 */
internal class CallLinkPeekJob private constructor(
  parameters: Parameters,
  private val callLinkRecipientId: RecipientId
) : BaseJob(parameters) {

  companion object {
    private val TAG = Log.tag(CallLinkPeekJob::class.java)

    const val KEY = "CallLinkPeekJob"
    private const val KEY_CALL_LINK_RECIPIENT_ID = "call_link_recipient_id"
  }

  constructor(callLinkRecipientId: RecipientId) : this(
    Parameters.Builder()
      .setQueue(PushProcessMessageJob.getQueueName(callLinkRecipientId))
      .setMaxInstancesForQueue(1)
      .setLifespan(TimeUnit.MINUTES.toMillis(1))
      .addConstraint(NetworkConstraint.KEY)
      .build(),
    callLinkRecipientId
  )

  override fun onRun() {
    if (!RemoteConfig.adHocCalling) {
      Log.i(TAG, "Ad hoc calling is disabled. Dropping peek for call link.")
      return
    }

    val recipient = Recipient.resolved(callLinkRecipientId)
    if (!recipient.isCallLink) {
      Log.w(TAG, "Recipient was not a call link. Ignoring.")
      return
    }

    AppDependencies.signalCallManager.peekCallLinkCall(callLinkRecipientId)
  }

  override fun onShouldRetry(e: Exception): Boolean = false

  override fun serialize(): ByteArray? {
    return JsonJobData.Builder()
      .putString(KEY_CALL_LINK_RECIPIENT_ID, callLinkRecipientId.serialize())
      .serialize()
  }

  override fun getFactoryKey(): String {
    return KEY
  }

  override fun onFailure() = Unit

  class Factory : Job.Factory<CallLinkPeekJob?> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): CallLinkPeekJob {
      val data = JsonJobData.deserialize(serializedData)
      return CallLinkPeekJob(parameters, RecipientId.from(data.getString(KEY_CALL_LINK_RECIPIENT_ID)))
    }
  }
}
