package org.gfs.chat.stories.settings.custom.viewers

import org.gfs.chat.R
import org.gfs.chat.database.model.DistributionListId
import org.gfs.chat.stories.settings.select.BaseStoryRecipientSelectionFragment

/**
 * Allows user to manage users that can view a story for a given distribution list.
 */
class AddViewersFragment : BaseStoryRecipientSelectionFragment() {
  override val actionButtonLabel: Int = R.string.HideStoryFromFragment__done
  override val distributionListId: DistributionListId
    get() = AddViewersFragmentArgs.fromBundle(requireArguments()).distributionListId
}
