package org.gfs.chat.conversation.v2

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import org.gfs.chat.R
import org.gfs.chat.components.FixedRoundedCornerBottomSheetDialogFragment
import org.gfs.chat.conversation.ConversationMessage
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.util.fragments.requireListener

/**
 * Shows an education sheet to users explaining how double tapping a sent message within 24hrs will allow them to edit it
 */
class DoubleTapEditEducationSheet(private val conversationMessage: ConversationMessage) : FixedRoundedCornerBottomSheetDialogFragment() {

  companion object {
    const val KEY = "DOUBLE_TAP_EDIT_EDU"
  }

  override val peekHeightPercentage: Float = 1f

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.conversation_item_double_tap_edit_education_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    SignalStore.uiHints.hasSeenDoubleTapEditEducationSheet = true

    view.findViewById<MaterialButton>(R.id.got_it).setOnClickListener {
      requireListener<Callback>().onDoubleTapEditEducationSheetNext(conversationMessage)
      dismissAllowingStateLoss()
    }
  }

  override fun onCancel(dialog: DialogInterface) {
    super.onCancel(dialog)
    requireListener<Callback>().onDoubleTapEditEducationSheetNext(conversationMessage)
  }

  interface Callback {
    fun onDoubleTapEditEducationSheetNext(conversationMessage: ConversationMessage)
  }
}
