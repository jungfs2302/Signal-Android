package org.gfs.chat.conversation.ui.inlinequery

import org.gfs.chat.R
import org.gfs.chat.util.adapter.mapping.AnyMappingModel
import org.gfs.chat.util.adapter.mapping.MappingAdapter

class InlineQueryAdapter(listener: (AnyMappingModel) -> Unit) : MappingAdapter() {
  init {
    registerFactory(InlineQueryEmojiResult.Model::class.java, { InlineQueryEmojiResult.ViewHolder(it, listener) }, R.layout.inline_query_emoji_result)
  }
}
