package org.gfs.chat.conversation

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.gfs.chat.contacts.paged.ContactSearchKey
import org.gfs.chat.conversation.v2.ConversationActivity
import org.gfs.chat.database.IdentityTable
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.database.model.DistributionListId
import org.gfs.chat.database.model.DistributionListPrivacyMode
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.profiles.ProfileName
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.safety.SafetyNumberBottomSheet
import org.gfs.chat.testing.SignalActivityRule

/**
 * Android test to help show SNC dialog quickly with custom data to make sure it displays properly.
 */
@Ignore("For testing/previewing manually, no assertions")
@RunWith(AndroidJUnit4::class)
class SafetyNumberChangeDialogPreviewer {

  @get:Rule val harness = SignalActivityRule(othersCount = 10)

  @Test
  fun testShowLongName() {
    val other: Recipient = Recipient.resolved(harness.others.first())

    SignalDatabase.recipients.setProfileName(other.id, ProfileName.fromParts("Super really long name like omg", "But seriously it's long like really really long"))

    harness.setVerified(other, IdentityTable.VerifiedStatus.VERIFIED)
    harness.changeIdentityKey(other)

    val scenario: ActivityScenario<ConversationActivity> = harness.launchActivity { putExtra("recipient_id", other.id.serialize()) }
    scenario.onActivity {
      SafetyNumberBottomSheet.forRecipientId(other.id).show(it.supportFragmentManager)
    }

    // Uncomment to make dialog stay on screen, otherwise will show/dismiss immediately
    // ThreadUtil.sleep(15000)
  }

  @Test
  fun testShowLargeSheet() {
    SignalDatabase.distributionLists.setPrivacyMode(DistributionListId.MY_STORY, DistributionListPrivacyMode.ONLY_WITH)

    val othersRecipients = harness.others.map { Recipient.resolved(it) }
    othersRecipients.forEach { other ->
      SignalDatabase.recipients.setProfileName(other.id, ProfileName.fromParts("My", "Name"))

      harness.setVerified(other, IdentityTable.VerifiedStatus.DEFAULT)
      harness.changeIdentityKey(other)

      SignalDatabase.distributionLists.addMemberToList(DistributionListId.MY_STORY, DistributionListPrivacyMode.ONLY_WITH, other.id)
    }

    val myStoryRecipientId = SignalDatabase.distributionLists.getRecipientId(DistributionListId.MY_STORY)!!
    val scenario: ActivityScenario<ConversationActivity> = harness.launchActivity { putExtra("recipient_id", harness.others.first().serialize()) }
    scenario.onActivity { conversationActivity ->
      SafetyNumberBottomSheet
        .forIdentityRecordsAndDestinations(
          identityRecords = AppDependencies.protocolStore.aci().identities().getIdentityRecords(othersRecipients).identityRecords,
          destinations = listOf(ContactSearchKey.RecipientSearchKey(myStoryRecipientId, true))
        )
        .show(conversationActivity.supportFragmentManager)
    }

    // Uncomment to make dialog stay on screen, otherwise will show/dismiss immediately
    // ThreadUtil.sleep( 30000)
  }
}
