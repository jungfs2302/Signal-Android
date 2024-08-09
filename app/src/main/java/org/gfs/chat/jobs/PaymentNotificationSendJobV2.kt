package org.gfs.chat.jobs

import org.signal.core.util.logging.Log
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.jobmanager.Job
import org.gfs.chat.jobmanager.JsonJobData
import org.gfs.chat.mms.OutgoingMessage
import org.gfs.chat.net.NotPushRegisteredException
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.sms.MessageSender
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * Crafts a [OutgoingPaymentsNotificationMessage] and uses the regular media sending framework to send it
 * instead of attempting to send directly. The logic for actually creating over-the-wire representation is
 * now in [IndividualSendJob] which gets enqueued by [MessageSender.send].
 */
class PaymentNotificationSendJobV2 private constructor(
  parameters: Parameters,
  private val recipientId: RecipientId,
  private val uuid: UUID
) : BaseJob(parameters) {

  companion object {
    const val KEY = "PaymentNotificationSendJobV2"
    private const val TAG = "PaymentNotiSendJobV2"
    private const val KEY_UUID = "uuid"
    private const val KEY_RECIPIENT = "recipient"
  }

  constructor(recipientId: RecipientId, uuid: UUID) : this(Parameters.Builder().build(), recipientId, uuid)

  override fun serialize(): ByteArray? {
    return JsonJobData.Builder()
      .putString(KEY_RECIPIENT, recipientId.serialize())
      .putString(KEY_UUID, uuid.toString())
      .serialize()
  }

  override fun getFactoryKey(): String {
    return KEY
  }

  @Throws(Exception::class)
  override fun onRun() {
    if (!Recipient.self().isRegistered) {
      throw NotPushRegisteredException()
    }

    val recipient = Recipient.resolved(recipientId)
    if (recipient.isUnregistered) {
      Log.w(TAG, "$recipientId not registered!")
      return
    }

    val payment = SignalDatabase.payments.getPayment(uuid)
    if (payment == null) {
      Log.w(TAG, "Could not find payment, cannot send notification $uuid")
      return
    }

    MessageSender.send(
      context,
      OutgoingMessage.paymentNotificationMessage(
        recipient,
        uuid.toString(),
        System.currentTimeMillis(),
        recipient.expiresInSeconds.seconds.inWholeMilliseconds
      ),
      SignalDatabase.threads.getOrCreateThreadIdFor(recipient),
      MessageSender.SendType.SIGNAL,
      null,
      null
    )
  }

  override fun onShouldRetry(e: Exception): Boolean = false
  override fun onFailure() = Unit

  class Factory : Job.Factory<PaymentNotificationSendJobV2?> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): PaymentNotificationSendJobV2 {
      val data = JsonJobData.deserialize(serializedData)
      return PaymentNotificationSendJobV2(
        parameters,
        RecipientId.from(data.getString(KEY_RECIPIENT)),
        UUID.fromString(data.getString(KEY_UUID))
      )
    }
  }
}
