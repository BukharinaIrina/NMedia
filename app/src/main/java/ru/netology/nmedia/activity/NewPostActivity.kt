package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityNewPostBinding

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addPost.requestFocus()

        binding.saveButton.setOnClickListener {
            if (binding.addPost.text.isNullOrBlank()) {
                Toast.makeText(
                    this,
                    getString(R.string.error_empty_content),
                    Toast.LENGTH_SHORT
                ).show()
                setResult(Activity.RESULT_CANCELED, intent)
            } else {
                val content = binding.addPost.text.toString()
                intent.putExtra(Intent.EXTRA_TEXT, content)
                setResult(RESULT_OK, intent)
            }
            finish()
        }

        val newPostActivity = this
        newPostActivity.onBackPressedDispatcher.addCallback(
            newPostActivity, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(newPostActivity).apply {
                        setTitle(getString(R.string.confirmation))
                        setMessage(getString(R.string.exit_post_save_mode))
                        setPositiveButton(getString(R.string.yes)) { _, _ ->
                            setResult(RESULT_CANCELED, intent)
                            finish()
                        }
                        setNegativeButton(getString(R.string.no)) { _, _ -> }
                        setCancelable(true)
                    }.create().show()
                }
            }
        )
    }
}
