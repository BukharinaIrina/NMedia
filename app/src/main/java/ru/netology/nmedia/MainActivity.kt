package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "27 мая в 11:30",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия - помочь встать на путь роста и начать цепочку перемен -> http://netolo.gy/fyb",
            likedByMe = false,
            shareByMe = false
        )

        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            if (post.likedByMe) {
                likeButton.setImageResource(R.drawable.ic_liked_24)
            } else {
                likeButton.setImageResource(R.drawable.ic_like_24)
            }
            likeCount.text = post.likes.toString()

            binding.likeButton.setOnClickListener {
                post.likedByMe = !post.likedByMe
                if (post.likedByMe) {
                    post.likes++
                    likeButton.setImageResource(R.drawable.ic_liked_24)
                } else {
                    likeButton.setImageResource(R.drawable.ic_like_24)
                    post.likes--
                }
                likeCount.text = CountLikeShare.counter(post.likes)
            }

            if (post.shareByMe) {
                shareButton.setImageResource(R.drawable.ic_share_24)
            }
            shareCount.text = post.shares.toString()

            binding.shareButton.setOnClickListener {
                post.shares++
                shareCount.text = CountLikeShare.counter(post.shares)
            }

            viewsCount.text = post.views.toString()
        }
    }
}
