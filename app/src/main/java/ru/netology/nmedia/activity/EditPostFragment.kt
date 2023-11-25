package ru.netology.nmedia.activity

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
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentEditPostBinding
import ru.netology.nmedia.util.Constants.Companion.API_URL
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringProperty
import ru.netology.nmedia.util.load
import ru.netology.nmedia.viewmodel.PostViewModel

class EditPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

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
        val binding = FragmentEditPostBinding.inflate(layoutInflater, container, false)

        arguments?.textArg?.let {
            binding.editPost.setText(it)
        }

        binding.editPost.requestFocus()

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.edit_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
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
                            true
                        }

                        R.id.cancel -> {
                            viewModel.cancelEditing()
                            AndroidUtils.hideKeyboard(binding.editPost)
                            findNavController().navigateUp()
                        }

                        else -> false
                    }
            },
            viewLifecycleOwner,
        )

        val urlAttachment = arguments?.getString("image")

        if (urlAttachment != null) {
            binding.apply {
                urlAttachment.let {
                    val url = "${API_URL}/media/${it}"
                    photo.load(url)
                    photoContainer.visibility = View.VISIBLE
                }
            }
        } else {
            viewModel.photo.observe(viewLifecycleOwner) { photo ->
                if (photo?.uri == null) {
                    binding.photoContainer.visibility = View.GONE
                    return@observe
                }
                binding.photoContainer.visibility = View.VISIBLE
                binding.photo.setImageURI(photo.uri)
            }
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
            viewModel.removePhoto()
            binding.photoContainer.visibility = View.GONE
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
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
