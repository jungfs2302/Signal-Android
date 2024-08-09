package org.gfs.chat.search

import org.gfs.chat.database.model.ThreadRecord

data class ThreadSearchResult(val results: List<ThreadRecord>, val query: String)
