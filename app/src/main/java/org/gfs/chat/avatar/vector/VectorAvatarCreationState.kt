package org.gfs.chat.avatar.vector

import org.gfs.chat.avatar.Avatar
import org.gfs.chat.avatar.AvatarColorItem
import org.gfs.chat.avatar.Avatars

data class VectorAvatarCreationState(
  val currentAvatar: Avatar.Vector
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
