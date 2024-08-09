package org.gfs.chat.components.settings.conversation

import org.gfs.chat.util.DynamicNoActionBarTheme
import org.gfs.chat.util.DynamicTheme

class CallInfoActivity : ConversationSettingsActivity(), ConversationSettingsFragment.Callback {

  override val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()
}
