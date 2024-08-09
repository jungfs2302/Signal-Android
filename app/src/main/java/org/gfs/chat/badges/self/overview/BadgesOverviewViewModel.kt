package org.gfs.chat.badges.self.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject
import org.signal.core.util.logging.Log
import org.gfs.chat.badges.BadgeRepository
import org.gfs.chat.components.settings.app.subscription.RecurringInAppPaymentRepository
import org.gfs.chat.database.model.InAppPaymentSubscriberRecord
import org.gfs.chat.keyvalue.SignalStore
import org.gfs.chat.recipients.Recipient
import org.gfs.chat.util.InternetConnectionObserver
import org.gfs.chat.util.livedata.Store
import java.util.Optional

private val TAG = Log.tag(BadgesOverviewViewModel::class.java)

class BadgesOverviewViewModel(
  private val badgeRepository: BadgeRepository
) : ViewModel() {
  private val store = Store(BadgesOverviewState())
  private val eventSubject = PublishSubject.create<BadgesOverviewEvent>()

  val state: LiveData<BadgesOverviewState> = store.stateLiveData
  val events: Observable<BadgesOverviewEvent> = eventSubject.observeOn(AndroidSchedulers.mainThread())

  val disposables = CompositeDisposable()

  init {
    store.update(Recipient.live(Recipient.self().id).liveDataResolved) { recipient, state ->
      state.copy(
        stage = if (state.stage == BadgesOverviewState.Stage.INIT) BadgesOverviewState.Stage.READY else state.stage,
        allUnlockedBadges = recipient.badges,
        displayBadgesOnProfile = SignalStore.inAppPayments.getDisplayBadgesOnProfile(),
        featuredBadge = recipient.featuredBadge
      )
    }

    disposables += InternetConnectionObserver.observe()
      .distinctUntilChanged()
      .subscribeBy { isConnected ->
        store.update { it.copy(hasInternet = isConnected) }
      }

    disposables += Single.zip(
      RecurringInAppPaymentRepository.getActiveSubscription(InAppPaymentSubscriberRecord.Type.DONATION),
      RecurringInAppPaymentRepository.getSubscriptions()
    ) { active, all ->
      if (!active.isActive && active.activeSubscription?.willCancelAtPeriodEnd() == true) {
        Optional.ofNullable<String>(all.firstOrNull { it.level == active.activeSubscription?.level }?.badge?.id)
      } else {
        Optional.empty()
      }
    }.subscribeBy(
      onSuccess = { badgeId ->
        store.update { it.copy(fadedBadgeId = badgeId.orElse(null)) }
      },
      onError = { throwable ->
        Log.w(TAG, "Could not retrieve data from server", throwable)
      }
    )
  }

  fun setDisplayBadgesOnProfile(displayBadgesOnProfile: Boolean) {
    store.update { it.copy(stage = BadgesOverviewState.Stage.UPDATING_BADGE_DISPLAY_STATE) }
    disposables += badgeRepository.setVisibilityForAllBadges(displayBadgesOnProfile)
      .subscribe(
        {
          store.update { it.copy(stage = BadgesOverviewState.Stage.READY) }
        },
        { error ->
          Log.e(TAG, "Failed to update visibility.", error)
          store.update { it.copy(stage = BadgesOverviewState.Stage.READY) }
          eventSubject.onNext(BadgesOverviewEvent.FAILED_TO_UPDATE_PROFILE)
        }
      )
  }

  override fun onCleared() {
    disposables.clear()
  }

  class Factory(
    private val badgeRepository: BadgeRepository
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return requireNotNull(modelClass.cast(BadgesOverviewViewModel(badgeRepository)))
    }
  }

  companion object {
    private val TAG = Log.tag(BadgesOverviewViewModel::class.java)
  }
}
