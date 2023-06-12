package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    val viewModel: PostViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    private val interactionListener: OnInteractionListener = object : OnInteractionListener {
        override fun onLike(post: Post) {
            viewModel.likeById(post.id)
        }

        override fun onShare(post: Post) {
            viewModel.shareById(post.id)
        }

        override fun onRemove(post: Post) {
            viewModel.removeById(post.id)
        }

        override fun onEdit(post: Post) {
            viewModel.edit(post)
            binding.group.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.group.visibility = View.GONE

        val adapter = PostsAdapter(interactionListener)
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val newPost = adapter.itemCount < posts.size
            adapter.submitList(posts) {
                if (newPost) binding.list.smoothScrollToPosition(0)
            }
        }

        viewModel.edited.observe(this) { post ->
            if (post.id != 0L) {
                with(binding.content) {
                    requestFocus()
                    setText(post.content)
                }
                with(binding.textPost) {
                    text = post.content
                }
            }
        }

        binding.saveButton.setOnClickListener {
            with(binding.content) {
                if (text.isNullOrBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        context.getString(R.string.error_empty_content),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.changeContent(text.toString())
                    viewModel.save()
                    setText("")
                    clearFocus()
                    AndroidUtils.hideKeyboard(this)
                    binding.group.visibility = View.GONE
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            with(binding.content) {
                viewModel.cancelEditing()
                setText("")
                clearFocus()
                AndroidUtils.hideKeyboard(this)
                binding.group.visibility = View.GONE
            }
        }
    }
}

