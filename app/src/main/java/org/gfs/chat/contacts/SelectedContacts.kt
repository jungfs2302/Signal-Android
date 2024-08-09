package org.gfs.chat.contacts

import android.view.View
import com.bumptech.glide.Glide
import org.gfs.chat.R
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.util.adapter.mapping.LayoutFactory
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.adapter.mapping.MappingModel
import org.gfs.chat.util.adapter.mapping.MappingViewHolder

object SelectedContacts {
  @JvmStatic
  fun register(adapter: MappingAdapter, onCloseIconClicked: (Model) -> Unit) {
    adapter.registerFactory(Model::class.java, LayoutFactory({ ViewHolder(it, onCloseIconClicked) }, R.layout.contact_selection_list_chip))
  }

  class Model(val selectedContact: SelectedContact, val recipient: Recipient) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return newItem.selectedContact.matches(selectedContact) && recipient == newItem.recipient
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return areItemsTheSame(newItem) && recipient.hasSameContent(newItem.recipient)
    }
  }

  private class ViewHolder(itemView: View, private val onCloseIconClicked: (Model) -> Unit) : MappingViewHolder<Model>(itemView) {

    private val chip: ContactChip = itemView.findViewById(R.id.contact_chip)

    override fun bind(model: Model) {
      chip.text = model.recipient.getShortDisplayName(context)
      chip.setContact(model.selectedContact)
      chip.isCloseIconVisible = true
      chip.setOnCloseIconClickListener {
        onCloseIconClicked(model)
      }
      chip.setAvatar(Glide.with(itemView), model.recipient, null)
    }
  }
}
