package ru.netology.nmedia.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringProperty
import ru.netology.nmedia.viewmodel.PostViewModel
import java.io.File

class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)

        val file = File(context?.filesDir, "Draft.txt")
        if (file.exists()) {
            binding.addPost.setText(readText())
            writeText("")
        } else {
            file.createNewFile()
        }

        arguments?.textArg?.let {
            binding.addPost.setText(it)
        }

        binding.addPost.requestFocus()

        binding.saveButton.setOnClickListener {
            val content = binding.addPost.text.toString()
            if (content.isNotBlank()) {
                viewModel.changeContent(content)
                viewModel.save()
                AndroidUtils.hideKeyboard(binding.addPost)
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

        //для решения проблемы при редактировании поста,
        //если нет обработки нажатия кнопки назад в EditPostFragment
        //viewModel.cancelEditing()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val content = binding.addPost.text.toString()
                    if (content.isNotBlank()) {
                        writeText(content)
                    }
                    AndroidUtils.hideKeyboard(binding.addPost)
                    findNavController().navigateUp()
                }
            }
        )

        return binding.root
    }

    companion object {
        var Bundle.textArg by StringProperty
    }

    private fun writeText(content: String) {
        context?.openFileOutput("Draft.txt", Context.MODE_PRIVATE).use {
            it?.write(content.toByteArray())
        }
    }

    private fun readText(): String {
        return context?.openFileInput("Draft.txt")?.bufferedReader().use {
            it?.readText().toString()
        }
    }
}
