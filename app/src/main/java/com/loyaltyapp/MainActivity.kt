package com.loyaltyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.loyaltyapp.ui.auth.AuthLandingScreen
import com.loyaltyapp.ui.auth.LoginScreen
import com.loyaltyapp.ui.auth.OTPScreen
import com.loyaltyapp.ui.auth.SignupScreen
import com.loyaltyapp.ui.home.MainScreen
import com.loyaltyapp.ui.theme.LoyaltyAppTheme
import com.loyaltyapp.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoyaltyAppTheme {
                LoyaltyApp(viewModel)
            }
        }
    }
}

sealed class Screen {
    object Landing : Screen()
    object Login : Screen()
    object Signup : Screen()
    data class OTP(
        val phone: String,
        val isSignup: Boolean = false,
        val signupName: String = "",
        val signupZip: String = "",
        val signupBirthDate: String = ""
    ) : Screen()
    object Main : Screen()
}

@Composable
fun LoyaltyApp(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    var screen by remember { mutableStateOf<Screen>(Screen.Landing) }

    // Once logged in, always show Main
    if (state.isLoggedIn) {
        MainScreen(viewModel = viewModel)
        return
    }

    when (val s = screen) {
        is Screen.Landing -> AuthLandingScreen(
            onLoginClick = { screen = Screen.Login },
            onSignupClick = { screen = Screen.Signup }
        )

        is Screen.Login -> LoginScreen(
            viewModel = viewModel,
            onOtpRequired = {
                val phone = viewModel.state.value.pendingPhone
                screen = Screen.OTP(phone = phone)
            },
            onBack = { screen = Screen.Landing }
        )

        is Screen.Signup -> SignupScreen(
            viewModel = viewModel,
            onOtpRequired = { name, phone, zip, birthDate ->
                screen = Screen.OTP(
                    phone = phone,
                    isSignup = true,
                    signupName = name,
                    signupZip = zip,
                    signupBirthDate = birthDate
                )
            },
            onBack = { screen = Screen.Landing }
        )

        is Screen.OTP -> OTPScreen(
            viewModel = viewModel,
            phone = s.phone,
            isSignup = s.isSignup,
            signupName = s.signupName,
            signupZip = s.signupZip,
            signupBirthDate = s.signupBirthDate,
            onSuccess = { /* state.isLoggedIn triggers MainScreen */ },
            onBack = {
                screen = if (s.isSignup) Screen.Signup else Screen.Login
            }
        )

        is Screen.Main -> MainScreen(viewModel = viewModel)
    }
}
