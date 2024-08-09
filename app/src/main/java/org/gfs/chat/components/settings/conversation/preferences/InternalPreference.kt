package org.gfs.chat.components.settings.conversation.preferences

import android.view.View
import org.gfs.chat.R
import org.gfs.chat.components.settings.PreferenceModel
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.util.adapter.mapping.LayoutFactory
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.adapter.mapping.MappingViewHolder

object InternalPreference {

  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.conversation_settings_internal_preference))
  }

  class Model(
    private val recipient: Recipient,
    val onInternalDetailsClicked: () -> Unit
  ) : PreferenceModel<Model>() {

    override fun areItemsTheSame(newItem: Model): Boolean {
      return recipient == newItem.recipient
    }
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val internalDetails: View = itemView.findViewById(R.id.internal_details)

    override fun bind(model: Model) {
      internalDetails.setOnClickListener { model.onInternalDetailsClicked() }
    }
  }
}
