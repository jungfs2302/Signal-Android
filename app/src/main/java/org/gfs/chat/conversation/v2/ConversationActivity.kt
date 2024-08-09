package org.gfs.chat.conversation.v2

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import androidx.activity.viewModels
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.signal.core.util.logging.Log
import org.signal.core.util.logging.Log.tag
import org.gfs.chat.PassphraseRequiredActivity
import org.gfs.chat.R
import org.gfs.chat.components.settings.app.subscription.InAppPaymentComponent
import org.gfs.chat.components.settings.app.subscription.StripeRepository
import org.gfs.chat.components.voice.VoiceNoteMediaController
import org.gfs.chat.components.voice.VoiceNoteMediaControllerOwner
import org.gfs.chat.conversation.ConversationIntents
import org.gfs.chat.util.Debouncer
import org.gfs.chat.util.DynamicNoActionBarTheme
import java.util.concurrent.TimeUnit

/**
 * Wrapper activity for ConversationFragment.
 */
open class ConversationActivity : PassphraseRequiredActivity(), VoiceNoteMediaControllerOwner, InAppPaymentComponent {

  companion object {
    private val TAG = tag(ConversationActivity::class.java)

    private const val STATE_WATERMARK = "share_data_watermark"
  }

  private val theme = DynamicNoActionBarTheme()
  private val transitionDebouncer: Debouncer = Debouncer(150, TimeUnit.MILLISECONDS)

  override val voiceNoteMediaController = VoiceNoteMediaController(this, true)

  override val stripeRepository: StripeRepository by lazy { StripeRepository(this) }
  override val googlePayResultPublisher: Subject<InAppPaymentComponent.GooglePayResult> = PublishSubject.create()

  private val motionEventRelay: MotionEventRelay by viewModels()
  private val shareDataTimestampViewModel: ShareDataTimestampViewModel by viewModels()

  override fun onPreCreate() {
    theme.onCreate(this)
  }

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    supportPostponeEnterTransition()
    transitionDebouncer.publish { supportStartPostponedEnterTransition() }
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

    if (savedInstanceState != null) {
      shareDataTimestampViewModel.timestamp = savedInstanceState.getLong(STATE_WATERMARK, -1L)
    } else if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) {
      shareDataTimestampViewModel.timestamp = System.currentTimeMillis()
    }

    setContentView(R.layout.fragment_container)

    if (savedInstanceState == null) {
      replaceFragment()
    }
  }

  override fun onResume() {
    super.onResume()
    theme.onResume(this)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putLong(STATE_WATERMARK, shareDataTimestampViewModel.timestamp)
  }

  override fun onStop() {
    super.onStop()
    if (isChangingConfigurations) {
      Log.i(TAG, "Conversation recreating due to configuration change")
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    transitionDebouncer.clear()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)

    // Note: We utilize this instead of 'replaceFragment' because there seems to be a bug
    // in constraint-layout which mixes up insets when replacing the fragment via onNewIntent.
    finish()
    startActivity(intent)
  }

  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googlePayResultPublisher.onNext(InAppPaymentComponent.GooglePayResult(requestCode, resultCode, data))
  }

  private fun replaceFragment() {
    val fragment = ConversationFragment().apply {
      arguments = if (ConversationIntents.isBubbleIntentUri(intent.data)) {
        ConversationIntents.createParentFragmentArguments(intent)
      } else {
        intent.extras
      }
    }

    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .disallowAddToBackStack()
      .commitNowAllowingStateLoss()
  }

  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    return motionEventRelay.offer(ev) || super.dispatchTouchEvent(ev)
  }
}
