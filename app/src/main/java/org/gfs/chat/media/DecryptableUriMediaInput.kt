package org.gfs.chat.media

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import org.gfs.chat.database.SignalDatabase.Companion.attachments
import org.gfs.chat.mms.PartAuthority
import org.gfs.chat.mms.PartUriParser
import org.gfs.chat.providers.BlobProvider
import org.gfs.chat.video.interfaces.MediaInput
import org.gfs.chat.video.videoconverter.mediadatasource.MediaDataSourceMediaInput
import java.io.IOException

/**
 * A media input source that is decrypted on the fly.
 */
@RequiresApi(api = 23)
object DecryptableUriMediaInput {
  @JvmStatic
  @Throws(IOException::class)
  fun createForUri(context: Context, uri: Uri): MediaInput {
    if (BlobProvider.isAuthority(uri)) {
      return MediaDataSourceMediaInput(BlobProvider.getInstance().getMediaDataSource(context, uri))
    }
    return if (PartAuthority.isLocalUri(uri)) {
      createForAttachmentUri(uri)
    } else {
      UriMediaInput(context, uri)
    }
  }

  private fun createForAttachmentUri(uri: Uri): MediaInput {
    val partId = PartUriParser(uri).partId
    if (!partId.isValid) {
      throw AssertionError()
    }
    val mediaDataSource = attachments.mediaDataSourceFor(partId, true) ?: throw AssertionError()
    return MediaDataSourceMediaInput(mediaDataSource)
  }
}
