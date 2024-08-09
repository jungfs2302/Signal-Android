package org.gfs.chat.stories.viewer.post

import android.graphics.Typeface
import android.net.Uri
import org.gfs.chat.blurhash.BlurHash
import org.gfs.chat.database.model.databaseprotos.BodyRangeList
import org.gfs.chat.database.model.databaseprotos.StoryTextPost
import org.gfs.chat.linkpreview.LinkPreview
import kotlin.time.Duration

sealed class StoryPostState {
  data class TextPost(
    val storyTextPostId: Long = 0L,
    val storyTextPost: StoryTextPost? = null,
    val linkPreview: LinkPreview? = null,
    val typeface: Typeface? = null,
    val bodyRanges: BodyRangeList? = null,
    val loadState: LoadState = LoadState.INIT
  ) : StoryPostState()

  data class ImagePost(
    val imageUri: Uri,
    val blurHash: BlurHash?
  ) : StoryPostState()

  data class VideoPost(
    val videoUri: Uri,
    val size: Long,
    val clipStart: Duration,
    val clipEnd: Duration,
    val blurHash: BlurHash?
  ) : StoryPostState()

  data class None(private val ts: Long = System.currentTimeMillis()) : StoryPostState()

  enum class LoadState {
    INIT,
    LOADED,
    FAILED
  }
}
