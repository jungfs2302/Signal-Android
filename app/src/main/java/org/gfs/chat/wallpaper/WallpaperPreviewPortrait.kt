package org.gfs.chat.wallpaper

import org.gfs.chat.R
import org.gfs.chat.components.AvatarImageView
import org.gfs.chat.conversation.colors.AvatarColor
import org.gfs.chat.recipients.Recipient

sealed class WallpaperPreviewPortrait {
  class ContactPhoto(private val recipient: Recipient) : WallpaperPreviewPortrait() {
    override fun applyToAvatarImageView(avatarImageView: AvatarImageView) {
      avatarImageView.setAvatar(recipient)
      avatarImageView.colorFilter = null
    }
  }

  class SolidColor(private val avatarColor: AvatarColor) : WallpaperPreviewPortrait() {
    override fun applyToAvatarImageView(avatarImageView: AvatarImageView) {
      avatarImageView.setImageResource(R.drawable.circle_tintable)
      avatarImageView.setColorFilter(avatarColor.colorInt())
    }
  }

  abstract fun applyToAvatarImageView(avatarImageView: AvatarImageView)
}
