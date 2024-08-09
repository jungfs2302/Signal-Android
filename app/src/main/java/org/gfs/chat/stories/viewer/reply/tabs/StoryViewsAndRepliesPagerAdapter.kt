package org.gfs.chat.stories.viewer.reply.tabs

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.stories.viewer.reply.group.StoryGroupReplyFragment
import org.gfs.chat.stories.viewer.views.StoryViewsFragment

class StoryViewsAndRepliesPagerAdapter(
  fragment: Fragment,
  private val storyId: Long,
  private val groupRecipientId: RecipientId,
  private val isFromNotification: Boolean,
  private val groupReplyStartPosition: Int
) : FragmentStateAdapter(fragment) {
  override fun getItemCount(): Int = 2

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    recyclerView.isNestedScrollingEnabled = false
  }

  override fun createFragment(position: Int): Fragment {
    return when (position) {
      0 -> StoryViewsFragment.create(storyId)
      1 -> StoryGroupReplyFragment.create(storyId, groupRecipientId, isFromNotification, groupReplyStartPosition)
      else -> throw IndexOutOfBoundsException()
    }
  }
}
