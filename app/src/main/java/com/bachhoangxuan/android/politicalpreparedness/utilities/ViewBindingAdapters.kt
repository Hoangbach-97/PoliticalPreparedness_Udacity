package com.bachhoangxuan.android.politicalpreparedness.utilities

import android.view.View
import androidx.databinding.BindingAdapter

object ViewBindingAdapters {
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setItemViewFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE)
                    view.animationFadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.animationFadeOut()
            }
        }
    }

}