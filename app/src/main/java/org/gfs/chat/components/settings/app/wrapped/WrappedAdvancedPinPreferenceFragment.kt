package org.gfs.chat.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import org.gfs.chat.R
import org.gfs.chat.preferences.AdvancedPinPreferenceFragment

class WrappedAdvancedPinPreferenceFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences__advanced_pin_settings)
    return AdvancedPinPreferenceFragment()
  }
}
