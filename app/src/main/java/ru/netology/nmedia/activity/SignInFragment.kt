package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.SignInViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class SignInFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: SignInViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) {
            appAuth.setAuth(Token(it.id, it.token))
            findNavController().navigateUp()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.authError) {
                binding.password.error = getString(R.string.error_authorization)
            }
        }

        binding.apply {
            login.requestFocus()
            signInButton.setOnClickListener {
                viewModel.authorizationUser(login.text.toString(), password.text.toString())
            }
            AndroidUtils.hideKeyboard(signInButton)
        }

        return binding.root
    }
}