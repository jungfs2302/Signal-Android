package org.gfs.chat.badges.self.featured

import org.gfs.chat.badges.models.Badge

data class SelectFeaturedBadgeState(
  val stage: Stage = Stage.INIT,
  val selectedBadge: Badge? = null,
  val allUnlockedBadges: List<Badge> = listOf()
) {
  enum class Stage {
    INIT,
    READY,
    SAVING
  }
}
