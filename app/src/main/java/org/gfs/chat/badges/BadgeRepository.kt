package org.gfs.chat.badges

import android.content.Context
import androidx.annotation.WorkerThread
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.logging.Log
import org.gfs.chat.badges.models.Badge
import org.gfs.chat.database.RecipientTable
import org.gfs.chat.database.SignalDatabase
import org.gfs.chat.dependencies.AppDependencies
import org.gfs.chat.jobs.MultiDeviceProfileContentUpdateJob
import org.gfs.chat.jobs.RefreshOwnProfileJob
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.storage.StorageSyncHelper
import org.gfs.chat.util.ProfileUtil
import java.io.IOException

class BadgeRepository(context: Context) {

  companion object {
    private val TAG = Log.tag(BadgeRepository::class.java)
  }

  private val context = context.applicationContext

  /**
   * Sets the visibility for each badge on a user's profile, and uploads them to the server.
   * Does not write to the local database. The caller must either do that themselves or schedule
   * a refresh own profile job.
   *
   * @return A list of the badges, properly modified to either visible or not visible, according to user preferences.
   */
  @Throws(IOException::class)
  @WorkerThread
  fun setVisibilityForAllBadgesSync(
    displayBadgesOnProfile: Boolean,
    selfBadges: List<Badge>
  ): List<Badge> {
    Log.d(TAG, "[setVisibilityForAllBadgesSync] Setting badge visibility...", true)

    val recipientTable: RecipientTable = SignalDatabase.recipients
    val badges = selfBadges.map { it.copy(visible = displayBadgesOnProfile) }

    Log.d(TAG, "[setVisibilityForAllBadgesSync] Uploading profile...", true)
    ProfileUtil.uploadProfileWithBadges(context, badges)
    SignalStore.inAppPayments.setDisplayBadgesOnProfile(displayBadgesOnProfile)
    recipientTable.markNeedsSync(Recipient.self().id)

    Log.d(TAG, "[setVisibilityForAllBadgesSync] Requesting data change sync...", true)
    StorageSyncHelper.scheduleSyncForDataChange()

    return badges
  }

  fun setVisibilityForAllBadges(
    displayBadgesOnProfile: Boolean,
    selfBadges: List<Badge> = Recipient.self().badges
  ): Completable = Completable.fromAction {
    setVisibilityForAllBadgesSync(displayBadgesOnProfile, selfBadges)

    Log.d(TAG, "[setVisibilityForAllBadges] Enqueueing profile refresh...", true)
    AppDependencies.jobManager
      .startChain(RefreshOwnProfileJob())
      .then(MultiDeviceProfileContentUpdateJob())
      .enqueue()
  }.subscribeOn(Schedulers.io())

  fun setFeaturedBadge(featuredBadge: Badge): Completable = Completable.fromAction {
    val badges = Recipient.self().badges
    val reOrderedBadges = listOf(featuredBadge.copy(visible = true)) + (badges.filterNot { it.id == featuredBadge.id })

    Log.d(TAG, "[setFeaturedBadge] Uploading profile with reordered badges...", true)
    ProfileUtil.uploadProfileWithBadges(context, reOrderedBadges)

    Log.d(TAG, "[setFeaturedBadge] Enqueueing profile refresh...", true)
    AppDependencies.jobManager
      .startChain(RefreshOwnProfileJob())
      .then(MultiDeviceProfileContentUpdateJob())
      .enqueue()
  }.subscribeOn(Schedulers.io())
}
