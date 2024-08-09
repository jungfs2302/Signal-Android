/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.backup.v2

import org.signal.core.util.concurrent.SignalExecutors
import org.gfs.chat.attachments.AttachmentId
import org.gfs.chat.attachments.DatabaseAttachment
import org.gfs.chat.database.AttachmentTable
import org.gfs.chat.database.model.MessageRecord
import org.gfs.chat.database.model.MmsMessageRecord
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobs.RestoreAttachmentThumbnailJob

/**
 * Responsible for managing logic around restore prioritization
 */
object BackupRestoreManager {

  private val reprioritizedAttachments: HashSet<AttachmentId> = HashSet()

  /**
   * Raise priority of all attachments for the included message records.
   *
   * This is so we can make certain attachments get downloaded more quickly
   */
  fun prioritizeAttachmentsIfNeeded(messageRecords: List<MessageRecord>) {
    SignalExecutors.BOUNDED.execute {
      synchronized(this) {
        val restoringAttachments = messageRecords
          .mapNotNull { (it as? MmsMessageRecord?)?.slideDeck?.slides }
          .flatten()
          .mapNotNull { it.asAttachment() as? DatabaseAttachment }
          .filter {
            val needThumbnail = it.thumbnailRestoreState == AttachmentTable.ThumbnailRestoreState.NEEDS_RESTORE && it.transferState == AttachmentTable.TRANSFER_RESTORE_IN_PROGRESS
            (needThumbnail || it.thumbnailRestoreState == AttachmentTable.ThumbnailRestoreState.IN_PROGRESS) && !reprioritizedAttachments.contains(it.attachmentId)
          }
          .map { it.attachmentId to it.mmsId }
          .toSet()

        reprioritizedAttachments += restoringAttachments.map { it.first }
        val thumbnailJobs = restoringAttachments.map {
          val (attachmentId, mmsId) = it
          RestoreAttachmentThumbnailJob(attachmentId = attachmentId, messageId = mmsId, highPriority = true)
        }
        if (thumbnailJobs.isNotEmpty()) {
          AppDependencies.jobManager.addAll(thumbnailJobs)
        }
      }
    }
  }
}
