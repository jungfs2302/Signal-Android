package org.gfs.chat.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import org.gfs.chat.R
import org.gfs.chat.preferences.EditProxyFragment

class WrappedEditProxyFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences_use_proxy)
    return EditProxyFragment()
  }
}
