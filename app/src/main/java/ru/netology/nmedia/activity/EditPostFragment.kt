package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.navigateUp
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentEditPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringProperty
import ru.netology.nmedia.viewmodel.PostViewModel

class EditPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditPostBinding.inflate(layoutInflater, container, false)

        arguments?.textArg?.let {
            binding.editPost.setText(it)
        }

        binding.editPost.requestFocus()

        binding.saveButton.setOnClickListener {
            val content = binding.editPost.text.toString()
            if (content.isNotBlank()) {
                viewModel.changeContent(content)
                viewModel.save()
                AndroidUtils.hideKeyboard(binding.editPost)
            } else {
                Toast.makeText(
                    activity,
                    R.string.error_empty_content,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.cancelEditing()
            AndroidUtils.hideKeyboard(binding.editPost)
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(requireActivity()).apply {
                        setTitle(getString(R.string.confirmation))
                        setMessage(getString(R.string.exit_post_editing_mode))
                        setPositiveButton(getString(R.string.yes)) { _, _ ->
                            viewModel.cancelEditing()
                            AndroidUtils.hideKeyboard(binding.editPost)
                            findNavController().navigateUp()
                        }
                        setNegativeButton(getString(R.string.no)) { _, _ -> }
                        setCancelable(true)
                    }.create().show()
                }
            }
        )

        return binding.root
    }

    companion object {
        var Bundle.textArg by StringProperty
    }
}

