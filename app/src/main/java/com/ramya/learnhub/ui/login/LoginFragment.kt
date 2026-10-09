package com.ramya.learnhub.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.ramya.learnhub.R
import com.ramya.learnhub.databinding.FragmentLoginBinding
import com.ramya.learnhub.ui.common.appContainer
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        viewModelFactory { initializer { LoginViewModel(appContainer.authRepository) } }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.loginButton.setOnClickListener { submit() }
        binding.passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) submit()
            actionId == EditorInfo.IME_ACTION_DONE
        }
        binding.emailInput.doAfterTextChanged { binding.emailLayout.error = null }
        binding.passwordInput.doAfterTextChanged { binding.passwordLayout.error = null }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::render)
            }
        }
    }

    private fun submit() {
        viewModel.onLoginClicked(
            email = binding.emailInput.text?.toString().orEmpty(),
            password = binding.passwordInput.text?.toString().orEmpty()
        )
    }

    private fun render(state: LoginUiState) = with(binding) {
        loading.isVisible = state.isLoading
        loginButton.isEnabled = !state.isLoading
        emailInput.isEnabled = !state.isLoading
        passwordInput.isEnabled = !state.isLoading
        emailLayout.error = state.emailError?.let(::getString)
        passwordLayout.error = state.passwordError?.let(::getString)
        formError.isVisible = state.formError != null
        formError.text = state.formError?.let(::getString)

        if (state.isLoggedIn && findNavController().currentDestination?.id == R.id.loginFragment) {
            findNavController().navigate(R.id.action_login_to_dashboard)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
