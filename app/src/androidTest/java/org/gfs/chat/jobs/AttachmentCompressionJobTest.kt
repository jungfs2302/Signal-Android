/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.gfs.chat.jobs

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.signal.core.util.StreamUtil
import org.gfs.chat.attachments.UriAttachment
import org.gfs.chat.database.AttachmentTable
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.database.UriAttachmentBuilder
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobmanager.Job
import org.gfs.chat.mms.SentMediaQuality
import org.gfs.chat.providers.BlobProvider
import org.gfs.chat.testing.SignalActivityRule
import org.gfs.chat.testing.assertIs
import org.gfs.chat.util.MediaUtil
import java.util.Optional
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AttachmentCompressionJobTest {

  @get:Rule
  val harness = SignalActivityRule()

  @Test
  fun testCompressionJobsWithDifferentTransformPropertiesCompleteSuccessfully() {
    val imageBytes: ByteArray = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("images/sample_image.png").use {
      StreamUtil.readFully(it)
    }

    val blob = BlobProvider.getInstance().forData(imageBytes).createForSingleSessionOnDisk(AppDependencies.application)

    val firstPreUpload = createAttachment(1, blob, AttachmentTable.TransformProperties.empty())
    val firstDatabaseAttachment = SignalDatabase.attachments.insertAttachmentForPreUpload(firstPreUpload)

    val firstCompressionJob: AttachmentCompressionJob = AttachmentCompressionJob.fromAttachment(firstDatabaseAttachment, false, -1)

    var secondCompressionJob: AttachmentCompressionJob? = null
    var firstJobResult: Job.Result? = null
    var secondJobResult: Job.Result? = null

    val secondJobLatch = CountDownLatch(1)
    val jobThread = Thread {
      firstCompressionJob.setContext(AppDependencies.application)
      firstJobResult = firstCompressionJob.run()

      secondJobLatch.await()

      secondCompressionJob!!.setContext(AppDependencies.application)
      secondJobResult = secondCompressionJob!!.run()
    }

    jobThread.start()
    val secondPreUpload = createAttachment(1, blob, AttachmentTable.TransformProperties.forSentMediaQuality(Optional.empty(), SentMediaQuality.HIGH))
    val secondDatabaseAttachment = SignalDatabase.attachments.insertAttachmentForPreUpload(secondPreUpload)
    secondCompressionJob = AttachmentCompressionJob.fromAttachment(secondDatabaseAttachment, false, -1)

    secondJobLatch.countDown()

    jobThread.join()

    firstJobResult!!.isSuccess assertIs true
    secondJobResult!!.isSuccess assertIs true
  }

  private fun createAttachment(id: Long, uri: Uri, transformProperties: AttachmentTable.TransformProperties): UriAttachment {
    return UriAttachmentBuilder.build(
      id,
      uri = uri,
      contentType = MediaUtil.IMAGE_JPEG,
      transformProperties = transformProperties
    )
  }
}
