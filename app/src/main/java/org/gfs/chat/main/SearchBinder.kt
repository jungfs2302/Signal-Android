package org.gfs.chat.main

import android.widget.ImageView
import org.gfs.chat.components.Material3SearchToolbar
import org.gfs.chat.util.views.Stub

interface SearchBinder {
  fun getSearchAction(): ImageView

  fun getSearchToolbar(): Stub<Material3SearchToolbar>

  fun onSearchOpened()

  fun onSearchClosed()
}
