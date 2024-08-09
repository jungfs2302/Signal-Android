package org.gfs.chat.stories

import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.isA
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.gfs.chat.attachments.AttachmentId
import org.gfs.chat.database.AttachmentTable
import org.gfs.chat.database.FakeMessageRecords
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobmanager.JobManager
import org.gfs.chat.jobs.AttachmentDownloadJob

class StoriesTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var mockAttachmentTable: AttachmentTable

  @Mock
  private lateinit var mockJobManager: JobManager

  @Mock
  private lateinit var mockApplicationDependenciesStatic: MockedStatic<AppDependencies>

  @Mock
  private lateinit var mockSignalDatabaseStatic: MockedStatic<SignalDatabase>

  @Mock
  private lateinit var mockSignalDatabase: SignalDatabase

  private val testScheduler = TestScheduler()

  @Before
  fun setUp() {
    RxJavaPlugins.setInitIoSchedulerHandler { testScheduler }
    RxJavaPlugins.setIoSchedulerHandler { testScheduler }

    SignalDatabase.setSignalDatabaseInstanceForTesting(mockSignalDatabase)
    whenever(SignalDatabase.attachments).thenReturn(mockAttachmentTable)
    whenever(AppDependencies.jobManager).thenReturn(mockJobManager)
    whenever(mockAttachmentTable.getAttachmentsForMessage(any())).thenReturn(emptyList())
  }

  @After
  fun tearDown() {
    RxJavaPlugins.reset()
  }

  @Test
  fun `Given a MessageRecord with no attachments and a LinkPreview without a thumbnail, when I enqueueAttachmentsFromStoryForDownload, then I enqueue nothing`() {
    // GIVEN
    val messageRecord = FakeMessageRecords.buildMediaMmsMessageRecord(
      linkPreviews = listOf(FakeMessageRecords.buildLinkPreview())
    )

    // WHEN
    val testObserver = Stories.enqueueAttachmentsFromStoryForDownload(messageRecord, true).test()
    testScheduler.triggerActions()

    // THEN
    testObserver.assertComplete()
    verify(mockJobManager, never()).add(any())
  }

  @Test
  fun `Given a MessageRecord with no attachments and a LinkPreview with a thumbnail, when I enqueueAttachmentsFromStoryForDownload, then I enqueue once`() {
    // GIVEN
    val messageRecord = FakeMessageRecords.buildMediaMmsMessageRecord(
      linkPreviews = listOf(
        FakeMessageRecords.buildLinkPreview(
          attachmentId = AttachmentId(1)
        )
      )
    )

    // WHEN
    val testObserver = Stories.enqueueAttachmentsFromStoryForDownload(messageRecord, true).test()
    testScheduler.triggerActions()

    // THEN
    testObserver.assertComplete()
    verify(mockJobManager).add(isA<AttachmentDownloadJob>())
  }

  @Test
  fun `Given a MessageRecord with an attachment, when I enqueueAttachmentsFromStoryForDownload, then I enqueue once`() {
    // GIVEN
    val attachment = FakeMessageRecords.buildDatabaseAttachment()
    val messageRecord = FakeMessageRecords.buildMediaMmsMessageRecord()
    whenever(mockAttachmentTable.getAttachmentsForMessage(any())).thenReturn(listOf(attachment))

    // WHEN
    val testObserver = Stories.enqueueAttachmentsFromStoryForDownload(messageRecord, true).test()
    testScheduler.triggerActions()

    // THEN
    testObserver.assertComplete()
    verify(mockJobManager).add(isA<AttachmentDownloadJob>())
  }
}
