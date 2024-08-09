/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.calls.links.create

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.gfs.chat.database.CallLinkTable
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobs.CallLinkUpdateSendJob
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.service.webrtc.links.CallLinkCredentials
import org.gfs.chat.service.webrtc.links.CreateCallLinkResult
import org.gfs.chat.service.webrtc.links.SignalCallLinkManager
import org.whispersystems.signalservice.internal.push.SyncMessage

/**
 * Repository for creating new call links. This will delegate to the [SignalCallLinkManager]
 * but will also ensure the database is updated.
 */
class CreateCallLinkRepository(
  private val callLinkManager: SignalCallLinkManager = AppDependencies.signalCallManager.callLinkManager
) {
  fun ensureCallLinkCreated(credentials: CallLinkCredentials): Single<EnsureCallLinkCreatedResult> {
    val callLinkRecipientId = Single.fromCallable {
      SignalDatabase.recipients.getByCallLinkRoomId(credentials.roomId)
    }

    return callLinkRecipientId.flatMap { recipientId ->
      if (recipientId.isPresent) {
        Single.just(EnsureCallLinkCreatedResult.Success(Recipient.resolved(recipientId.get())))
      } else {
        callLinkManager.createCallLink(credentials).map {
          when (it) {
            is CreateCallLinkResult.Success -> {
              SignalDatabase.callLinks.insertCallLink(
                CallLinkTable.CallLink(
                  recipientId = RecipientId.UNKNOWN,
                  roomId = credentials.roomId,
                  credentials = credentials,
                  state = it.state
                )
              )

              AppDependencies.jobManager.add(
                CallLinkUpdateSendJob(
                  credentials.roomId,
                  SyncMessage.CallLinkUpdate.Type.UPDATE
                )
              )

              EnsureCallLinkCreatedResult.Success(
                Recipient.resolved(
                  SignalDatabase.recipients.getByCallLinkRoomId(credentials.roomId).get()
                )
              )
            }

            is CreateCallLinkResult.Failure -> EnsureCallLinkCreatedResult.Failure(it)
          }
        }
      }
    }.subscribeOn(Schedulers.io())
  }
}
