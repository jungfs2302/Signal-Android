package org.gfs.chat.database.model

/**
 * Details related to the current avatar profile image file that would be returned via [org.gfs.chat.profiles.AvatarHelper.getAvatarFile]
 * at the time this [org.gfs.chat.recipients.Recipient] was loaded/refreshed from the database.
 */
data class ProfileAvatarFileDetails(
  val hashId: Long,
  val lastModified: Long
) {
  fun getDiskCacheKeyBytes(): ByteArray {
    return toString().toByteArray()
  }

  fun hasFile(): Boolean {
    return this != NO_DETAILS
  }

  companion object {
    @JvmField
    val NO_DETAILS = ProfileAvatarFileDetails(0, 0)
  }
}
