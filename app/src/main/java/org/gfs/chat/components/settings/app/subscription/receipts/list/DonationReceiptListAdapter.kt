package org.gfs.chat.components.settings.app.subscription.receipts.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import org.gfs.chat.R
import org.gfs.chat.components.settings.DSLSettingsText
import org.gfs.chat.components.settings.SectionHeaderPreference
import org.gfs.chat.components.settings.SectionHeaderPreferenceViewHolder
import org.gfs.chat.components.settings.TextPreference
import org.gfs.chat.components.settings.TextPreferenceViewHolder
import org.gfs.chat.util.StickyHeaderDecoration
import org.gfs.chat.util.adapter.mapping.LayoutFactory
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.toLocalDateTime

class DonationReceiptListAdapter(onModelClick: (DonationReceiptListItem.Model) -> Unit) : MappingAdapter(), StickyHeaderDecoration.StickyHeaderAdapter<SectionHeaderPreferenceViewHolder> {

  init {
    registerFactory(TextPreference::class.java, LayoutFactory({ TextPreferenceViewHolder(it) }, R.layout.dsl_preference_item))
    DonationReceiptListItem.register(this, onModelClick)
  }

  override fun getHeaderId(position: Int): Long {
    return when (val item = getItem(position)) {
      is DonationReceiptListItem.Model -> item.record.timestamp.toLocalDateTime().year.toLong()
      else -> StickyHeaderDecoration.StickyHeaderAdapter.NO_HEADER_ID
    }
  }

  override fun onCreateHeaderViewHolder(parent: ViewGroup?, position: Int, type: Int): SectionHeaderPreferenceViewHolder {
    return SectionHeaderPreferenceViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.dsl_section_header, parent, false))
  }

  override fun onBindHeaderViewHolder(viewHolder: SectionHeaderPreferenceViewHolder?, position: Int, type: Int) {
    viewHolder?.itemView?.run {
      val color = ContextCompat.getColor(context, R.color.signal_colorBackground)
      setBackgroundColor(color)
    }

    viewHolder?.bind(SectionHeaderPreference(DSLSettingsText.from(getHeaderId(position).toString())))
  }
}
