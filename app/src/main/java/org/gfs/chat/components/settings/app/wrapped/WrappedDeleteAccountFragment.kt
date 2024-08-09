package org.gfs.chat.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import org.gfs.chat.R
import org.gfs.chat.delete.DeleteAccountFragment

class WrappedDeleteAccountFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences__delete_account)
    return DeleteAccountFragment()
  }
}
