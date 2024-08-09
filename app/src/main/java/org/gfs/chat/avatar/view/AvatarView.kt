package org.gfs.chat.avatar.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.use
import com.bumptech.glide.RequestManager
import org.gfs.chat.R
import org.gfs.chat.components.AvatarImageView
import org.gfs.chat.database.model.StoryViewState
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.stories.Stories
import org.gfs.chat.util.visible

/**
 * AvatarView encapsulating the AvatarImageView and decorations.
 */
class AvatarView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

  private var storyRingScale = 0.8f
  init {
    inflate(context, R.layout.avatar_view, this)

    isClickable = false
    storyRingScale = context.theme.obtainStyledAttributes(attrs, R.styleable.AvatarView, 0, 0).use { it.getFloat(R.styleable.AvatarView_storyRingScale, storyRingScale) }
  }

  private val avatar: AvatarImageView = findViewById<AvatarImageView>(R.id.avatar_image_view).apply {
    initialize(context, attrs)
  }

  private val storyRing: View = findViewById(R.id.avatar_story_ring)

  private fun showStoryRing(hasUnreadStory: Boolean) {
    if (!Stories.isFeatureEnabled()) {
      return
    }

    storyRing.visible = true
    storyRing.setBackgroundResource(if (hasUnreadStory) R.drawable.avatar_story_ring_active else R.drawable.avatar_story_ring_inactive)

    avatar.scaleX = storyRingScale
    avatar.scaleY = storyRingScale
  }

  private fun hideStoryRing() {
    storyRing.visible = false

    avatar.scaleX = 1f
    avatar.scaleY = 1f
  }

  fun hasStory(): Boolean {
    return storyRing.visible
  }

  fun setStoryRingFromState(storyViewState: StoryViewState) {
    when (storyViewState) {
      StoryViewState.NONE -> hideStoryRing()
      StoryViewState.UNVIEWED -> showStoryRing(true)
      StoryViewState.VIEWED -> showStoryRing(false)
    }
  }

  /**
   * Displays Note-to-Self
   */
  fun displayChatAvatar(recipient: Recipient) {
    avatar.setAvatar(recipient)
  }

  /**
   * Displays Note-to-Self
   */
  fun displayChatAvatar(requestManager: RequestManager, recipient: Recipient, isQuickContactEnabled: Boolean) {
    avatar.setAvatar(requestManager, recipient, isQuickContactEnabled)
  }

  /**
   * Displays Profile image
   */
  fun displayProfileAvatar(recipient: Recipient) {
    avatar.setRecipient(recipient)
  }

  fun setFallbackAvatarProvider(fallbackAvatarProvider: AvatarImageView.FallbackAvatarProvider?) {
    avatar.setFallbackAvatarProvider(fallbackAvatarProvider)
  }

  fun disableQuickContact() {
    avatar.disableQuickContact()
  }
}
