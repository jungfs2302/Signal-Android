/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.gfs.chat.calls.log.CallLogFilter
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.service.webrtc.links.CallLinkCredentials
import org.gfs.chat.service.webrtc.links.CallLinkRoomId
import org.gfs.chat.service.webrtc.links.SignalCallLinkState
import org.gfs.chat.testing.SignalActivityRule

@RunWith(AndroidJUnit4::class)
class CallLinkTableTest {

  companion object {
    private val ROOM_ID_A = byteArrayOf(1, 2, 3, 4)
    private val ROOM_ID_B = byteArrayOf(2, 2, 3, 4)
    private const val TIMESTAMP_A = 1000L
    private const val TIMESTAMP_B = 2000L
  }

  @get:Rule
  val harness = SignalActivityRule(createGroup = true)

  @Test
  fun givenTwoNonAdminCallLinks_whenIDeleteBeforeFirst_thenIExpectNeitherDeleted() {
    insertTwoNonAdminCallLinksWithEvents()
    SignalDatabase.callLinks.deleteNonAdminCallLinksOnOrBefore(TIMESTAMP_A - 500)
    val callEvents = SignalDatabase.calls.getCalls(0, 2, "", CallLogFilter.ALL)
    assertEquals(2, callEvents.size)
  }

  @Test
  fun givenTwoNonAdminCallLinks_whenIDeleteOnFirst_thenIExpectFirstDeleted() {
    insertTwoNonAdminCallLinksWithEvents()
    SignalDatabase.callLinks.deleteNonAdminCallLinksOnOrBefore(TIMESTAMP_A)
    val callEvents = SignalDatabase.calls.getCalls(0, 2, "", CallLogFilter.ALL)
    assertEquals(1, callEvents.size)
    assertEquals(TIMESTAMP_B, callEvents.first().record.timestamp)
  }

  @Test
  fun givenTwoNonAdminCallLinks_whenIDeleteAfterFirstAndBeforeSecond_thenIExpectFirstDeleted() {
    insertTwoNonAdminCallLinksWithEvents()
    SignalDatabase.callLinks.deleteNonAdminCallLinksOnOrBefore(TIMESTAMP_B - 500)
    val callEvents = SignalDatabase.calls.getCalls(0, 2, "", CallLogFilter.ALL)
    assertEquals(1, callEvents.size)
    assertEquals(TIMESTAMP_B, callEvents.first().record.timestamp)
  }

  @Test
  fun givenTwoNonAdminCallLinks_whenIDeleteOnSecond_thenIExpectBothDeleted() {
    insertTwoNonAdminCallLinksWithEvents()
    SignalDatabase.callLinks.deleteNonAdminCallLinksOnOrBefore(TIMESTAMP_B)
    val callEvents = SignalDatabase.calls.getCalls(0, 2, "", CallLogFilter.ALL)
    assertEquals(0, callEvents.size)
  }

  @Test
  fun givenTwoNonAdminCallLinks_whenIDeleteAfterSecond_thenIExpectBothDeleted() {
    insertTwoNonAdminCallLinksWithEvents()
    SignalDatabase.callLinks.deleteNonAdminCallLinksOnOrBefore(TIMESTAMP_B + 500)
    val callEvents = SignalDatabase.calls.getCalls(0, 2, "", CallLogFilter.ALL)
    assertEquals(0, callEvents.size)
  }

  private fun insertTwoNonAdminCallLinksWithEvents() {
    insertCallLinkWithEvent(ROOM_ID_A, 1000)
    insertCallLinkWithEvent(ROOM_ID_B, 2000)
  }

  private fun insertCallLinkWithEvent(roomId: ByteArray, timestamp: Long) {
    SignalDatabase.callLinks.insertCallLink(
      CallLinkTable.CallLink(
        recipientId = RecipientId.UNKNOWN,
        roomId = CallLinkRoomId.fromBytes(roomId),
        credentials = CallLinkCredentials(
          linkKeyBytes = roomId,
          adminPassBytes = null
        ),
        state = SignalCallLinkState()
      )
    )

    val callLinkRecipient = SignalDatabase.recipients.getByCallLinkRoomId(CallLinkRoomId.fromBytes(roomId)).get()

    SignalDatabase.calls.insertAcceptedGroupCall(
      1,
      callLinkRecipient,
      CallTable.Direction.INCOMING,
      timestamp
    )
  }
}
