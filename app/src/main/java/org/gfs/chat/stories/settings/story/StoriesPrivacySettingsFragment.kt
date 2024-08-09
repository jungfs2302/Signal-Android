package org.gfs.chat.stories.settings.story

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import org.signal.core.util.concurrent.LifecycleDisposable
import org.signal.core.util.dp
import org.gfs.chat.R
import org.gfs.chat.components.DialogFragmentDisplayManager
import org.gfs.chat.components.ProgressCardDialogFragment
import org.gfs.chat.components.settings.DSLConfiguration
import org.gfs.chat.components.settings.DSLSettingsAdapter
import org.gfs.chat.components.settings.DSLSettingsFragment
import org.gfs.chat.components.settings.DSLSettingsText
import org.gfs.chat.components.settings.configure
import org.gfs.chat.contacts.paged.ContactSearchAdapter
import org.gfs.chat.contacts.paged.ContactSearchKey
import org.gfs.chat.contacts.paged.ContactSearchPagedDataSourceRepository
import org.gfs.chat.groups.ParcelableGroupId
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.mediasend.v2.stories.ChooseGroupStoryBottomSheet
import org.gfs.chat.mediasend.v2.stories.ChooseStoryTypeBottomSheet
import org.gfs.chat.stories.GroupStoryEducationSheet
import org.gfs.chat.stories.dialogs.StoryDialogs
import org.gfs.chat.stories.settings.create.CreateStoryFlowDialogFragment
import org.gfs.chat.stories.settings.create.CreateStoryWithViewersFragment
import org.gfs.chat.util.BottomSheetUtil
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.adapter.mapping.PagingMappingAdapter
import org.gfs.chat.util.navigation.safeNavigate

/**
 * Allows the user to view their stories they can send to and modify settings.
 */
class StoriesPrivacySettingsFragment :
  DSLSettingsFragment(
    titleId = R.string.preferences__stories
  ),
  ChooseStoryTypeBottomSheet.Callback,
  GroupStoryEducationSheet.Callback {

  private val viewModel: StoriesPrivacySettingsViewModel by viewModels(factoryProducer = {
    StoriesPrivacySettingsViewModel.Factory(ContactSearchPagedDataSourceRepository(requireContext()))
  })

  private val lifecycleDisposable = LifecycleDisposable()
  private val progressDisplayManager = DialogFragmentDisplayManager { ProgressCardDialogFragment.create() }

  override fun createAdapters(): Array<MappingAdapter> {
    return arrayOf(DSLSettingsAdapter(), PagingMappingAdapter<ContactSearchKey>(), DSLSettingsAdapter())
  }

  override fun bindAdapters(adapter: ConcatAdapter) {
    lifecycleDisposable.bindTo(viewLifecycleOwner)

    val titleId = StoriesPrivacySettingsFragmentArgs.fromBundle(requireArguments()).titleId
    setTitle(titleId)

    val (top, middle, bottom) = adapter.adapters

    findNavController().addOnDestinationChangedListener { _, destination, _ ->
      if (destination.id == R.id.storiesPrivacySettingsFragment) {
        viewModel.pagingController.onDataInvalidated()
      }
    }

    @Suppress("UNCHECKED_CAST")
    ContactSearchAdapter.registerStoryItems(
      mappingAdapter = middle as PagingMappingAdapter<ContactSearchKey>,
      storyListener = { _, story, _ ->
        when {
          story.recipient.isMyStory -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToMyStorySettings())
          story.recipient.isGroup -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToGroupStorySettings(ParcelableGroupId.from(story.recipient.requireGroupId())))
          else -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToPrivateStorySettings(story.recipient.requireDistributionListId()))
        }
      }
    )

    NewStoryItem.register(top as MappingAdapter)

    middle.setPagingController(viewModel.pagingController)

    parentFragmentManager.setFragmentResultListener(ChooseGroupStoryBottomSheet.GROUP_STORY, viewLifecycleOwner) { _, bundle ->
      val results = ChooseGroupStoryBottomSheet.ResultContract.getRecipientIds(bundle)
      viewModel.displayGroupsAsStories(results)
    }

    parentFragmentManager.setFragmentResultListener(CreateStoryWithViewersFragment.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
      viewModel.pagingController.onDataInvalidated()
    }

    lifecycleDisposable += viewModel.state.subscribe { state ->
      if (state.isUpdatingEnabledState) {
        progressDisplayManager.show(viewLifecycleOwner, childFragmentManager)
      } else {
        progressDisplayManager.hide()
      }

      top.submitList(getTopConfiguration(state).toMappingModelList())
      middle.submitList(getMiddleConfiguration(state).toMappingModelList())
      (bottom as MappingAdapter).submitList(getBottomConfiguration(state).toMappingModelList())
    }
  }

  private fun getTopConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return configure {
      if (state.areStoriesEnabled) {
        space(16.dp)

        noPadTextPref(
          title = DSLSettingsText.from(
            R.string.StoriesPrivacySettingsFragment__story_updates_automatically_disappear,
            DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_BodyMedium),
            DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), R.color.signal_colorOnSurfaceVariant))
          )
        )

        space(20.dp)

        sectionHeaderPref(R.string.StoriesPrivacySettingsFragment__stories)

        customPref(
          NewStoryItem.Model {
            ChooseStoryTypeBottomSheet().show(childFragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
          }
        )
      } else {
        clickPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__turn_on_stories),
          summary = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__share_and_view),
          onClick = {
            viewModel.setStoriesEnabled(true)
          }
        )
      }
    }
  }

  private fun getMiddleConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return if (state.areStoriesEnabled) {
      configure {
        ContactSearchAdapter.toMappingModelList(
          state.storyContactItems,
          emptySet(),
          null
        ).forEach {
          customPref(it)
        }
      }
    } else {
      configure { }
    }
  }

  private fun getBottomConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return if (state.areStoriesEnabled) {
      configure {
        dividerPref()

        switchPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__view_receipts),
          summary = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__see_and_share),
          isChecked = state.areViewReceiptsEnabled,
          onClick = {
            viewModel.toggleViewReceipts()
          }
        )

        dividerPref()

        clickPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__turn_off_stories),
          summary = DSLSettingsText.from(
            R.string.StoriesPrivacySettingsFragment__if_you_opt_out,
            DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_BodyMedium),
            DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), R.color.signal_colorOnSurfaceVariant))
          ),
          onClick = {
            StoryDialogs.disableStories(requireContext(), viewModel.userHasActiveStories) {
              viewModel.setStoriesEnabled(false)
            }
          }
        )
      }
    } else {
      configure { }
    }
  }

  override fun onGroupStoryClicked() {
    if (SignalStore.story.userHasSeenGroupStoryEducationSheet) {
      onGroupStoryEducationSheetNext()
    } else {
      GroupStoryEducationSheet().show(childFragmentManager, GroupStoryEducationSheet.KEY)
    }
  }

  override fun onNewStoryClicked() {
    CreateStoryFlowDialogFragment().show(parentFragmentManager, CreateStoryWithViewersFragment.REQUEST_KEY)
  }

  override fun onGroupStoryEducationSheetNext() {
    ChooseGroupStoryBottomSheet().show(parentFragmentManager, ChooseGroupStoryBottomSheet.GROUP_STORY)
  }
}
