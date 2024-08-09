package org.gfs.chat.animation

import android.animation.Animator

abstract class AnimationStartListener : Animator.AnimatorListener {
  override fun onAnimationEnd(animation: Animator) = Unit
  override fun onAnimationCancel(animation: Animator) = Unit
  override fun onAnimationRepeat(animation: Animator) = Unit
}
