package org.gfs.chat.mediasend.v2.review

import android.view.View
import org.gfs.chat.R
import org.gfs.chat.util.adapter.mapping.LayoutFactory
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.adapter.mapping.MappingModel
import org.gfs.chat.util.adapter.mapping.MappingViewHolder

typealias OnAddMediaItemClicked = () -> Unit

object MediaReviewAddItem {

  fun register(mappingAdapter: MappingAdapter, onAddMediaItemClicked: OnAddMediaItemClicked) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory({ ViewHolder(it, onAddMediaItemClicked) }, R.layout.v2_media_review_add_media_item))
  }

  object Model : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return true
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return true
    }
  }

  class ViewHolder(itemView: View, onAddMediaItemClicked: OnAddMediaItemClicked) : MappingViewHolder<Model>(itemView) {

    init {
      itemView.setOnClickListener { onAddMediaItemClicked() }
    }

    override fun bind(model: Model) = Unit
  }
}
