package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.DateSeparator
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.Constants.Companion.API_URL
import ru.netology.nmedia.util.CountLikeShare
import ru.netology.nmedia.util.load
import java.time.OffsetDateTime

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onRemove(post: Post) {}
    fun onEdit(post: Post) {}
    fun onVideo(post: Post) {}
    fun onRoot(post: Post) {}
    fun onImage(image: String) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            else -> error("unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (it) {
                is Post -> {
                    val previousPosition = position - 1
                    val previousItem = if (previousPosition >= 0) {
                        getItem(previousPosition)
                    } else {
                        null
                    }

                    val previousPost = if (previousItem is Ad) {
                        val previousPostPosition = position - 2
                        if (previousPostPosition >= 0) {
                            getItem(previousPostPosition) as? Post
                        } else {
                            null
                        }
                    } else {
                        previousItem as? Post
                    }

                    (holder as? PostViewHolder)?.bind(previousPost = previousPost, post = it)
                }

                is Ad -> (holder as? AdViewHolder)?.bind(it)
            }
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.image.load("${API_URL}/media/${ad.image}")
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(previousPost: Post?, post: Post) {
        val separator = if (previousPost.notToday() && post.today()) {
            DateSeparator.TODAY
        } else if (previousPost.notYesterday() && post.yesterday()) {
            DateSeparator.YESTERDAY
        } else if (previousPost.notLastWeek() && post.lastWeek()) {
            DateSeparator.LAST_WEEK
        } else {
            null
        }
        binding.root.tag = separator

        binding.apply {
            author.text = post.author
            content.text = post.content
            published.text = post.published.toString()

            val options = RequestOptions().circleCrop()
            val url = "${API_URL}/avatars/${post.authorAvatar}"
            avatar.load(url, options)

            attachment.visibility = View.GONE
            val urlAttachment = "${API_URL}/media/${post.attachment?.url}"
            if (post.attachment != null) {
                attachment.visibility = View.VISIBLE
                attachment.load(urlAttachment)
            } else {
                attachment.visibility = View.GONE
            }
            attachment.setOnClickListener {
                post.attachment?.let { attachment ->
                    onInteractionListener.onImage(attachment.url)
                }
            }

            likeButton.isChecked = post.likedByMe
            likeButton.text = CountLikeShare.counter(post.likes)
            likeButton.setOnClickListener {
                likeButton.isChecked = !likeButton.isChecked
                onInteractionListener.onLike(post)
            }

            shareButton.text = CountLikeShare.counter(post.shares)
            shareButton.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            viewsButton.text = CountLikeShare.counter(post.views)

            menuButton.isVisible = post.ownedByMe
            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            /*if (post.video == null) {
            binding.playVideoGroup.visibility = View.GONE
        } else {
            binding.playVideoGroup.visibility = View.VISIBLE
        }
        play.setOnClickListener { onInteractionListener.onVideo(post) }
        video.setOnClickListener { onInteractionListener.onVideo(post) }*/

            root.setOnClickListener { onInteractionListener.onRoot(post) }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}

private val today = OffsetDateTime.now()
private val yesterday = today.minusDays(1)
private val lastWeek = today.minusDays(2)

private fun Post.today(): Boolean =
    today.year == published.year &&
            today.dayOfYear == published.dayOfYear

private fun Post.yesterday(): Boolean =
    yesterday.year == published.year &&
            yesterday.dayOfYear == published.dayOfYear

private fun Post.lastWeek(): Boolean =
    lastWeek.year == published.year &&
            lastWeek.dayOfYear == published.dayOfYear

private fun Post?.notToday(): Boolean = this == null || !today()

private fun Post?.notYesterday(): Boolean = this == null || !yesterday()

private fun Post?.notLastWeek(): Boolean = this == null || !lastWeek()