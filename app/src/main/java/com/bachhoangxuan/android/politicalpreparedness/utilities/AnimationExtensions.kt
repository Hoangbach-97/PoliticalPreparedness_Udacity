package com.bachhoangxuan.android.politicalpreparedness.utilities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

// Animate changing when the view visibility.
fun View.animationFadeIn() {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@animationFadeIn.alpha = 1f
        }
    })
}

fun View.animationFadeOut() {
    this.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@animationFadeOut.alpha = 1f
            this@animationFadeOut.visibility = View.GONE
        }
    })
}