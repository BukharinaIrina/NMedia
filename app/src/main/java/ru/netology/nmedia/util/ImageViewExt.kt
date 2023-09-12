package ru.netology.nmedia.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ru.netology.nmedia.R

fun ImageView.load(url: String, options: RequestOptions = RequestOptions()) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_loading_24)
        .error(R.drawable.ic_error_loading_24)
        .timeout(10_000)
        .apply(options)
        .into(this)
}