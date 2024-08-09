package org.gfs.chat.components.settings.conversation.permissions

import org.gfs.chat.groups.ui.GroupChangeFailureReason

sealed class PermissionsSettingsEvents {
  class GroupChangeError(val reason: GroupChangeFailureReason) : PermissionsSettingsEvents()
}
