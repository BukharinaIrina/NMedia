package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityEditPostBinding

class EditPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editPost.requestFocus()

        binding.editPost.setText(intent.getStringExtra(Intent.EXTRA_TEXT))

        binding.saveButton.setOnClickListener {
            if (binding.editPost.text.isNullOrBlank()) {
                Toast.makeText(
                    this,
                    getString(R.string.error_empty_content),
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_CANCELED, intent)
            } else {
                val result = Intent().putExtra(Intent.EXTRA_TEXT, binding.editPost.text.toString())
                setResult(RESULT_OK, result)
            }
            finish()
        }

        binding.cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }
}
