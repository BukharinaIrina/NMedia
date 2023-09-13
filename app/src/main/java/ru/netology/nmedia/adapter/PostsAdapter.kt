package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.CountLikeShare
import ru.netology.nmedia.util.load


interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onRemove(post: Post) {}
    fun onEdit(post: Post) {}
    fun onVideo(post: Post) {}
    fun onRoot(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            content.text = post.content
            published.text = post.published

            val options = RequestOptions().circleCrop()
            val url = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
            avatar.load(url, options)

            attachment.visibility = View.GONE
            val urlAttachment = "http://10.0.2.2:9999/images/${post.attachment?.url}"
            if (post.attachment != null) {
                attachment.visibility = View.VISIBLE
                attachment.load(urlAttachment)
            } else {
                attachment.visibility = View.GONE
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

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}