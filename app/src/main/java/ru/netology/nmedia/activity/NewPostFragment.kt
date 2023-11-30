package ru.netology.nmedia.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringProperty
import ru.netology.nmedia.viewmodel.PostViewModel
import java.io.File
import androidx.core.net.toFile
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    private val photoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), "Image pick Error", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    val uri = requireNotNull(it.data?.data)
                    viewModel.setPhoto(uri, uri.toFile())
                }
            }
        }

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

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.save_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
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
                            true
                        }

                        else -> false
                    }
            },
            viewLifecycleOwner,
        )

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(photo.uri)
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(photoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(photoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.clearPhoto()
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
