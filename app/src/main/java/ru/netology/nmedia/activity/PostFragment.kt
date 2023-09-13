package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
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

        viewModel.data.observe(viewLifecycleOwner) { state ->
            binding.postFragment.apply {
                state.posts.map { post ->
                    if (post.id == id) {
                        PostViewHolder(this, object : OnInteractionListener {

                            override fun onLike(post: Post) {
                                if (post.likedByMe) {
                                    viewModel.unlikeById(post.id)
                                } else {
                                    viewModel.likeById(post.id)
                                }
                                viewModel.loadPosts()
                            }

                            override fun onShare(post: Post) {
                                viewModel.shareById(post.id)
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, post.content)
                                    type = "text/plain"
                                }
                                val shareIntent =
                                    Intent.createChooser(
                                        intent,
                                        getString(R.string.chooser_share_post)
                                    )
                                startActivity(shareIntent)
                            }

                            override fun onRemove(post: Post) {
                                viewModel.removeById(post.id)
                                findNavController().navigate(
                                    R.id.action_postFragment_to_feedFragment
                                )
                            }

                            override fun onEdit(post: Post) {
                                viewModel.edit(post)
                                findNavController().navigate(
                                    R.id.action_postFragment_to_editPostFragment,
                                    Bundle().apply {
                                        textArg = post.content
                                    }
                                )
                            }

                            override fun onVideo(post: Post) {
                                val videoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                                startActivity(videoIntent)
                            }

                        }).bind(post)
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

