package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.util.CountLikeShare
import ru.netology.nmedia.util.LongProperty
import ru.netology.nmedia.viewmodel.PostViewModel

class PostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        val id = arguments?.idArg

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            binding.postFragment.apply {
                posts.map { post ->
                    if (post.id == id) {
                        author.text = post.author
                        published.text = post.published
                        content.text = post.content

                        likeButton.isChecked = post.likedByMe
                        likeButton.text = CountLikeShare.counter(post.likes)
                        likeButton.setOnClickListener {
                            viewModel.likeById(post.id)
                        }

                        shareButton.text = CountLikeShare.counter(post.shares)
                        shareButton.setOnClickListener {
                            viewModel.shareById(post.id)
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, post.content)
                                type = "text/plain"
                            }
                            val shareIntent =
                                Intent.createChooser(intent, getString(R.string.chooser_share_post))
                            startActivity(shareIntent)
                        }

                        viewsButton.text = CountLikeShare.counter(post.views)

                        menuButton.setOnClickListener {
                            PopupMenu(it.context, it).apply {
                                inflate(R.menu.options_post)
                                setOnMenuItemClickListener { item ->
                                    when (item.itemId) {
                                        R.id.edit -> {
                                            viewModel.edit(post)
                                            findNavController()
                                                .navigate(R.id.action_postFragment_to_editPostFragment,
                                                    Bundle().apply {
                                                        textArg = post.content
                                                    }
                                                )
                                            true
                                        }

                                        R.id.remove -> {
                                            viewModel.removeById(post.id)
                                            findNavController()
                                                .navigate(R.id.action_postFragment_to_feedFragment)
                                            true
                                        }

                                        else -> false
                                    }
                                }
                            }.show()
                        }

                        if (post.video == null) {
                            binding.postFragment.playVideoGroup.visibility = View.GONE
                        } else {
                            binding.postFragment.playVideoGroup.visibility = View.VISIBLE
                        }
                        play.setOnClickListener {
                            val videoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                            startActivity(videoIntent)
                        }
                        video.setOnClickListener {
                            val videoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                            startActivity(videoIntent)
                        }
                    }
                }
            }
        }
        return binding.root
    }

    companion object {
        var Bundle.idArg by LongProperty
    }
}

