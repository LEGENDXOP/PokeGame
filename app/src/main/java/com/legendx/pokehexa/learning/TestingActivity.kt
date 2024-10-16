package com.legendx.pokehexa.learning

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.legendx.pokehexa.ui.theme.PokeHexaGameTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


class TestingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeHexaGameTheme {
                TestingScreen()
            }
        }
    }
}


@Composable
fun TestingScreen() {
    val myViewModel = koinViewModel<MyViewModel>()
    val registerState by myViewModel.registerState.collectAsStateWithLifecycle()
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                registerState.email,
                onValueChange = {
                    myViewModel.updateEmail(it)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (registerState.isValidEmail) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Default.Block, contentDescription = null)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                registerState.password,
                onValueChange = {
                    myViewModel.updatePassword(it)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            myViewModel.updatePasswordVisibility()
                        }
                    ) {
                        if (registerState.isPasswordVisible) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        } else {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )
            PasswordValidator(
                text = "At least 8 characters",
                isValid = registerState.passwordValidationState.hasMinLength,
                modifier = Modifier.padding(top = 8.dp)
            )
            PasswordValidator(
                text = "At least 1 uppercase letter",
                isValid = registerState.passwordValidationState.hasUpperCase,
                modifier = Modifier.padding(top = 8.dp)
            )
            PasswordValidator(
                text = "At least 1 lowercase letter",
                isValid = registerState.passwordValidationState.hasLowerCase,
                modifier = Modifier.padding(top = 8.dp)
            )
            PasswordValidator(
                text = "At least 1 number",
                isValid = registerState.passwordValidationState.hasNumber,
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedButton(
                onClick = {
                    myViewModel.onAction(RegisterActions.OnRegister)
                },
                enabled = registerState.canRegister
            ) {
                Text(text = "Register")
            }
        }
    }
}

@Composable
fun PasswordValidator(
    text: String,
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isValid) Icons.Default.Check else Icons.Default.Clear,
            contentDescription = null,
            tint = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}


interface PatternValidator {
    fun validateEmail(email: String): Boolean
}

class EmailValidator : PatternValidator {
    override fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }
}

data class PasswordValidationState(
    val hasMinLength: Boolean = false,
    val hasUpperCase: Boolean = false,
    val hasLowerCase: Boolean = false,
    val hasNumber: Boolean = false,
) {
    val isPasswordValid: Boolean
        get() = hasMinLength && hasUpperCase && hasLowerCase && hasNumber
}

class UserValidator(
    private val emailValidator: PatternValidator
) {
    fun isValidEmail(email: String): Boolean {
        return emailValidator.validateEmail(email)
    }

    fun isValidPassword(password: String): PasswordValidationState {
        val hasMinLength = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasNumber = password.any { it.isDigit() }
        return PasswordValidationState(
            hasMinLength = hasMinLength,
            hasUpperCase = hasUpperCase,
            hasLowerCase = hasLowerCase,
            hasNumber = hasNumber
        )
    }
}

data class RegisterState(
    val email: String = "",
    val isValidEmail: Boolean = false,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordValidationState: PasswordValidationState = PasswordValidationState(),
    val isRegistering: Boolean = false,
    val canRegister: Boolean = false,
)

sealed interface RegisterActions {
    data object OnRegister : RegisterActions
    data object OnBack : RegisterActions
}

class MyViewModel(
    private val userValidator: UserValidator
) : ViewModel() {
    private val _registerState = MutableStateFlow(RegisterState())
    val registerState = _registerState.asStateFlow()

    init {
        viewModelScope.launch {
            _registerState.collectLatest { state ->
                val isValidEmail = userValidator.isValidEmail(state.email)
                val passwordValidationState = userValidator.isValidPassword(state.password)
                _registerState.value = state.copy(
                    isValidEmail = isValidEmail,
                    passwordValidationState = passwordValidationState,
                    canRegister = isValidEmail && passwordValidationState.isPasswordValid
                )
            }
        }
    }

    fun updateEmail(email: String) {
        _registerState.value = registerState.value.copy(
            email = email,
        )
    }

    fun updatePassword(password: String) {
        _registerState.value = registerState.value.copy(
            password = password,
        )
    }

    fun updatePasswordVisibility() {
        _registerState.value = registerState.value.copy(
            isPasswordVisible = !registerState.value.isPasswordVisible,
        )
    }

    fun onAction(actions: RegisterActions) {
        if (actions is RegisterActions.OnRegister) {
            _registerState.value = RegisterState()
        }
    }
}