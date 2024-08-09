package org.gfs.chat.conversation.ui.inlinequery

/**
 * Called when a query changes.
 */
interface InlineQueryChangedListener {
  fun onQueryChanged(inlineQuery: InlineQuery)
  fun clearQuery() = onQueryChanged(InlineQuery.NoQuery)
}
