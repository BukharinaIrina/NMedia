package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.LongProperty
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        val id = arguments?.idArg

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest {
                    binding.postFragment.apply {
                        viewModel.data.map { pagingData ->
                            pagingData.map { post ->
                                if (post is Post)
                                    if (post.id == id) {
                                        PostViewHolder(
                                            this,
                                            object : OnInteractionListener {

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
                                                            putString("image", post.attachment?.url)
                                                        }
                                                    )
                                                }

                                                override fun onVideo(post: Post) {
                                                    val videoIntent =
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse(post.video)
                                                        )
                                                    startActivity(videoIntent)
                                                }
                                            }).bind(null, post)
                                    }
                            }
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

