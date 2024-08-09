package org.gfs.chat.stories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import org.signal.core.util.concurrent.SignalExecutors
import org.gfs.chat.R
import org.gfs.chat.components.FixedRoundedCornerBottomSheetDialogFragment
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.util.fragments.requireListener

/**
 * Displays an education sheet to the user which explains what Group Stories are.
 */
class GroupStoryEducationSheet : FixedRoundedCornerBottomSheetDialogFragment() {

  companion object {
    const val KEY = "GROUP_STORY_EDU"
  }

  override val peekHeightPercentage: Float = 1f

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.group_story_education_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    SignalStore.story.userHasSeenGroupStoryEducationSheet = true
    SignalExecutors.BOUNDED_IO.execute { Stories.onStorySettingsChanged(Recipient.self().id) }

    view.findViewById<MaterialButton>(R.id.next).setOnClickListener {
      requireListener<Callback>().onGroupStoryEducationSheetNext()
      dismissAllowingStateLoss()
    }
  }

  interface Callback {
    fun onGroupStoryEducationSheetNext()
  }
}
