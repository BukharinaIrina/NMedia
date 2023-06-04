package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel by viewModels<PostViewModel>()
        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content

                if (post.likedByMe) {
                    likeButton.setImageResource(R.drawable.ic_liked_24)
                } else {
                    likeButton.setImageResource(R.drawable.ic_like_24)
                }
                likeCount.text = CountLikeShare.counter(post.likes)

                if (post.shareByMe) {
                    shareButton.setImageResource(R.drawable.ic_share_24)
                }
                shareCount.text = CountLikeShare.counter(post.shares)

                viewsCount.text = CountLikeShare.counter(post.views)
            }
        }

        binding.likeButton.setOnClickListener {
            viewModel.like()
        }

        binding.shareButton.setOnClickListener {
            viewModel.share()
        }
    }
}

