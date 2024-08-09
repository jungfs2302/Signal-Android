package org.gfs.chat.stories.settings.create

import androidx.navigation.fragment.findNavController
import org.gfs.chat.R
import org.gfs.chat.database.model.DistributionListId
import org.gfs.chat.recipients.RecipientId
import org.gfs.chat.stories.settings.select.BaseStoryRecipientSelectionFragment
import org.gfs.chat.util.navigation.safeNavigate

/**
 * Allows user to select who will see the story they are creating
 */
class CreateStoryViewerSelectionFragment : BaseStoryRecipientSelectionFragment() {
  override val actionButtonLabel: Int = R.string.CreateStoryViewerSelectionFragment__next
  override val distributionListId: DistributionListId? = null

  override fun goToNextScreen(recipients: Set<RecipientId>) {
    findNavController().safeNavigate(CreateStoryViewerSelectionFragmentDirections.actionCreateStoryViewerSelectionToCreateStoryWithViewers(recipients.toTypedArray()))
  }
}
