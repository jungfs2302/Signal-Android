package org.gfs.chat.mediasend.v2.gallery

import android.content.Context
import org.gfs.chat.mediasend.Media
import org.gfs.chat.mediasend.MediaFolder
import org.gfs.chat.mediasend.MediaRepository

class MediaGalleryRepository(context: Context, private val mediaRepository: MediaRepository) {
  private val context: Context = context.applicationContext

  fun getFolders(onFoldersRetrieved: (List<MediaFolder>) -> Unit) {
    mediaRepository.getFolders(context) { onFoldersRetrieved(it) }
  }

  fun getMedia(bucketId: String, onMediaRetrieved: (List<Media>) -> Unit) {
    mediaRepository.getMediaInBucket(context, bucketId) { onMediaRetrieved(it) }
  }
}
