package org.gfs.chat.stories.my

import androidx.fragment.app.Fragment
import org.gfs.chat.components.FragmentWrapperActivity

class MyStoriesActivity : FragmentWrapperActivity() {
  override fun getFragment(): Fragment {
    return MyStoriesFragment()
  }
}
