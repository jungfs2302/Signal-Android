package org.gfs.chat.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import org.gfs.chat.R
import org.gfs.chat.preferences.BackupsPreferenceFragment

class WrappedBackupsPreferenceFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.BackupsPreferenceFragment__chat_backups)
    return BackupsPreferenceFragment()
  }
}
