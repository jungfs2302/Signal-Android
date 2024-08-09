package org.gfs.chat.components.settings.conversation.sounds

import org.gfs.chat.database.RecipientTable
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.recipients.RecipientId

data class SoundsAndNotificationsSettingsState(
  val recipientId: RecipientId = Recipient.UNKNOWN.id,
  val muteUntil: Long = 0L,
  val mentionSetting: RecipientTable.MentionSetting = RecipientTable.MentionSetting.DO_NOT_NOTIFY,
  val hasCustomNotificationSettings: Boolean = false,
  val hasMentionsSupport: Boolean = false,
  val channelConsistencyCheckComplete: Boolean = false
)
