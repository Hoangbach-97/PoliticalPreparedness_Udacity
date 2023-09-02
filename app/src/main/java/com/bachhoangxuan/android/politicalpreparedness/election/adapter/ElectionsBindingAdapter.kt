package com.bachhoangxuan.android.politicalpreparedness.election.adapter


import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.bachhoangxuan.android.politicalpreparedness.R
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("buttonText")
fun buttonText(button: Button, status: Boolean) {
    val text =
        button.context.getString(if (status) R.string.unfollow else R.string.follow)
    button.text = text
}

@BindingAdapter("displayDate")
fun displayDate(textView: TextView, date: String?) {
    var dateString = "yyyy-MM-dd"
    date?.let {
        if (date.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                dateFormat.parse(date)?.let {
                    dateString = it.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    textView.text = dateString
}

@BindingAdapter("onTapOpenLink")
fun onTapOpenLink(textView: TextView, url: String?) {
    textView.setOnClickListener {
        if (!url.isNullOrEmpty()) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            textView.context.startActivity(browserIntent)
        } else {
            Toast.makeText(
                textView.context,
                textView.context.getString(R.string.link_invalid),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
