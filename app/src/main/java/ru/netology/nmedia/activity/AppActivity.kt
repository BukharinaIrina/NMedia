package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.ActivityAppBinding
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.viewmodel.AuthViewModel

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }
            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(binding.root, R.string.error_empty_content, LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        finish()
                    }
                    .show()
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_graph).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    textArg = text
                }
            )
        }

        val authViewModel by viewModels<AuthViewModel>()

        var currentMenuProvider: MenuProvider? = null
        authViewModel.state.observe(this) {
            currentMenuProvider?.also(::removeMenuProvider)

            addMenuProvider(
                object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                        menuInflater.inflate(R.menu.auth_menu, menu)

                        menu.let {
                            it.setGroupVisible(R.id.authenticated, authViewModel.authenticated)
                            it.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
                        }
                    }

                    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                        when (menuItem.itemId) {
                            R.id.signIn -> {
                                findNavController(R.id.nav_graph).navigate(R.id.action_feedFragment_to_signInFragment)
                                true
                            }

                            R.id.signUp -> {
                                findNavController(R.id.nav_graph).navigate(R.id.action_feedFragment_to_signUpFragment)
                                true
                            }

                            R.id.signOut -> {
                                MaterialAlertDialogBuilder(this@AppActivity)
                                    .setTitle(getString(R.string.confirmation))
                                    .setMessage(getString(R.string.exit_the_program))
                                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                        AppAuth.getInstance().removeAuth()
                                    }
                                    .setNegativeButton(getString(R.string.no)) { _, _ -> }
                                    .show()
                                true
                            }

                            else -> false
                        }
                }.also {
                    currentMenuProvider = it
                },
                this
            )
        }

        requestNotificationsPermission()
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(permission), 1)
    }
}


