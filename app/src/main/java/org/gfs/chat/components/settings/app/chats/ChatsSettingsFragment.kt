package org.gfs.chat.components.settings.app.chats

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import org.signal.donations.InAppPaymentType
import org.gfs.chat.R
import org.gfs.chat.components.settings.DSLConfiguration
import org.gfs.chat.components.settings.DSLSettingsFragment
import org.gfs.chat.components.settings.DSLSettingsText
import org.gfs.chat.components.settings.app.subscription.InAppPaymentCheckoutLauncher.createBackupsCheckoutLauncher
import org.gfs.chat.components.settings.configure
import org.gfs.chat.util.RemoteConfig
import org.gfs.chat.util.adapter.mapping.MappingAdapter
import org.gfs.chat.util.navigation.safeNavigate

class ChatsSettingsFragment : DSLSettingsFragment(R.string.preferences_chats__chats) {

  private lateinit var viewModel: ChatsSettingsViewModel
  private lateinit var checkoutLauncher: ActivityResultLauncher<InAppPaymentType>

  override fun onResume() {
    super.onResume()
    viewModel.refresh()
  }

  @Suppress("ReplaceGetOrSet")
  override fun bindAdapter(adapter: MappingAdapter) {
    checkoutLauncher = createBackupsCheckoutLauncher {
      findNavController().safeNavigate(ChatsSettingsFragmentDirections.actionChatsSettingsFragmentToRemoteBackupsSettingsFragment().setBackupLaterSelected(it))
    }

    viewModel = ViewModelProvider(this).get(ChatsSettingsViewModel::class.java)

    viewModel.state.observe(viewLifecycleOwner) {
      adapter.submitList(getConfiguration(it).toMappingModelList())
    }
  }

  private fun getConfiguration(state: ChatsSettingsState): DSLConfiguration {
    return configure {
      switchPref(
        title = DSLSettingsText.from(R.string.preferences__generate_link_previews),
        summary = DSLSettingsText.from(R.string.preferences__retrieve_link_previews_from_websites_for_messages),
        isChecked = state.generateLinkPreviews,
        onClick = {
          viewModel.setGenerateLinkPreviewsEnabled(!state.generateLinkPreviews)
        }
      )

      switchPref(
        title = DSLSettingsText.from(R.string.preferences__pref_use_address_book_photos),
        summary = DSLSettingsText.from(R.string.preferences__display_contact_photos_from_your_address_book_if_available),
        isChecked = state.useAddressBook,
        onClick = {
          viewModel.setUseAddressBook(!state.useAddressBook)
        }
      )

      switchPref(
        title = DSLSettingsText.from(R.string.preferences__pref_keep_muted_chats_archived),
        summary = DSLSettingsText.from(R.string.preferences__muted_chats_that_are_archived_will_remain_archived),
        isChecked = state.keepMutedChatsArchived,
        onClick = {
          viewModel.setKeepMutedChatsArchived(!state.keepMutedChatsArchived)
        }
      )

      dividerPref()

      sectionHeaderPref(R.string.ChatsSettingsFragment__keyboard)

      switchPref(
        title = DSLSettingsText.from(R.string.preferences_advanced__use_system_emoji),
        isChecked = state.useSystemEmoji,
        onClick = {
          viewModel.setUseSystemEmoji(!state.useSystemEmoji)
        }
      )

      switchPref(
        title = DSLSettingsText.from(R.string.ChatsSettingsFragment__send_with_enter),
        isChecked = state.enterKeySends,
        onClick = {
          viewModel.setEnterKeySends(!state.enterKeySends)
        }
      )

      dividerPref()

      sectionHeaderPref(R.string.preferences_chats__backups)

      if (RemoteConfig.messageBackups || state.canAccessRemoteBackupsSettings) {
        clickPref(
          title = DSLSettingsText.from(R.string.RemoteBackupsSettingsFragment__signal_backups),
          summary = DSLSettingsText.from(if (state.canAccessRemoteBackupsSettings) R.string.arrays__enabled else R.string.arrays__disabled),
          onClick = {
            if (state.canAccessRemoteBackupsSettings) {
              Navigation.findNavController(requireView()).safeNavigate(R.id.action_chatsSettingsFragment_to_remoteBackupsSettingsFragment)
            } else {
              checkoutLauncher.launch(InAppPaymentType.RECURRING_BACKUP)
            }
          }
        )
      }

      clickPref(
        title = DSLSettingsText.from(R.string.preferences_chats__chat_backups),
        summary = DSLSettingsText.from(if (state.localBackupsEnabled) R.string.arrays__enabled else R.string.arrays__disabled),
        onClick = {
          Navigation.findNavController(requireView()).safeNavigate(R.id.action_chatsSettingsFragment_to_backupsPreferenceFragment)
        }
      )
    }
  }
}
