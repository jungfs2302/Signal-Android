package org.gfs.chat.avatar.text

import org.gfs.chat.avatar.Avatar
import org.gfs.chat.avatar.AvatarColorItem
import org.gfs.chat.avatar.Avatars

data class TextAvatarCreationState(
  val currentAvatar: Avatar.Text
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
