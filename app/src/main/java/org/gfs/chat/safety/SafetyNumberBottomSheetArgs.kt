package org.gfs.chat.safety

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.gfs.chat.contacts.paged.ContactSearchKey
import org.gfs.chat.database.model.MessageId
import org.gfs.chat.recipients.RecipientId

/**
 * Fragment argument for `SafetyNumberBottomSheetFragment`
 */
@Parcelize
data class SafetyNumberBottomSheetArgs(
  val untrustedRecipients: List<RecipientId>,
  val destinations: List<ContactSearchKey.RecipientSearchKey>,
  val messageId: MessageId? = null
) : Parcelable
