package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.activity.PostFragment.Companion.idArg
import ru.netology.nmedia.adapter.DateTimeDecoration
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    private val viewModelAuth: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onLike(post: Post) {
                if (viewModelAuth.authenticated) {
                    if (post.likedByMe)
                        viewModel.unlikeById(post.id)
                    else
                        viewModel.likeById(post.id)
                } else {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(getString(R.string.confirmation))
                        .setMessage(getString(R.string.pass_authorization))
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                        }
                        .setNegativeButton(getString(R.string.no)) { _, _ -> }
                        .show()
                }
            }

            override fun onShare(post: Post) {
                if (viewModelAuth.authenticated) {
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
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_editPostFragment,
                    Bundle().apply {
                        textArg = post.content
                        putString("image", post.attachment?.url)
                    }
                )
            }

            override fun onVideo(post: Post) {
                val videoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(videoIntent)
            }

            override fun onRoot(post: Post) {
                if (viewModelAuth.authenticated) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_postFragment,
                        Bundle().apply {
                            idArg = post.id
                        }
                    )
                }
            }

            override fun onImage(image: String) {
                if (viewModelAuth.authenticated) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_imageFragment,
                        Bundle().apply {
                            putString("image", image)
                        }
                    )
                }
            }
        })
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() }
        )

        binding.list.addItemDecoration(
            DateTimeDecoration(
                resources.getDimensionPixelOffset(R.dimen.date_offset),
                resources.getDimension(R.dimen.date_text_size),
                ContextCompat.getColor(requireActivity(), R.color.green),
            )
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing

            if (state.error) {
                Snackbar.make(
                    binding.root,
                    R.string.error_loading,
                    BaseTransientBottomBar.LENGTH_LONG
                ).setAction(R.string.retry_loading)
                {
                    viewModel.loadPosts()
                }
                    .show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefresh.isRefreshing =
                        state.refresh is LoadState.Loading
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        viewModelAuth.data.observe(viewLifecycleOwner) {
            adapter.refresh()
        }

        /*viewLifecycleOwner.lifecycleScope.launch {
            adapter
                .loadStateFlow
                .distinctUntilChangedBy { it.source.refresh }
                .map { it.source.refresh is LoadState.NotLoading }
                .collectLatest { binding.list.scrollToPosition(0) }
        }*/

        binding.addButton.setOnClickListener {
            if (viewModelAuth.authenticated)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            else
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getString(R.string.confirmation))
                    .setMessage(getString(R.string.pass_authorization))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                    }
                    .setNegativeButton(getString(R.string.no)) { _, _ -> }
                    .show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(requireActivity()).apply {
                        setTitle(getString(R.string.confirmation))
                        setMessage(getString(R.string.exit_the_program))
                        setPositiveButton(getString(R.string.yes)) { _, _ ->
                            requireActivity().finish()
                        }
                        setNegativeButton(getString(R.string.no)) { _, _ -> }
                        setCancelable(true)
                    }.create().show()
                }
            }
        )

        return binding.root
    }
}


