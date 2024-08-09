package org.gfs.chat.stories.landing

import android.view.View
import org.gfs.chat.R
import org.gfs.chat.avatar.view.AvatarView
import org.gfs.chat.components.settings.PreferenceModel
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.util.adapter.mapping.LayoutFactory
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.adapter.mapping.MappingViewHolder

/**
 * Item displayed on an empty Stories landing page allowing the user to add a new story.
 */
object MyStoriesItem {

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.stories_landing_item_my_stories))
  }

  class Model(
    val onClick: () -> Unit
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean = true
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val avatarView: AvatarView = itemView.findViewById(R.id.avatar)

    override fun bind(model: Model) {
      itemView.setOnClickListener { model.onClick() }
      avatarView.displayProfileAvatar(Recipient.self())
    }
  }
}
