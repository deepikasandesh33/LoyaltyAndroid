package com.loyaltyapp.ui.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loyaltyapp.viewmodel.AppViewModel

@Composable
fun OTPScreen(
    viewModel: AppViewModel,
    phone: String,
    isSignup: Boolean = false,
    signupName: String = "",
    signupZip: String = "",
    signupBirthDate: String = "",
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(24.dp))

        Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Enter OTP", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Sent to +1 $phone", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(Modifier.height(40.dp))

        // 6-box OTP input
        BasicTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.focusRequester(focusRequester)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(6) { index ->
                    val digit = otp.getOrNull(index)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = if (otp.length == index) 2.dp else 1.dp,
                                color = if (otp.length == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(digit, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                isLoading = true; error = ""
                if (isSignup) {
                    viewModel.completeSignupOtp(
                        otp = otp, name = signupName, phone = phone,
                        zipCode = signupZip, birthDate = signupBirthDate,
                        onSuccess = { isLoading = false; onSuccess() },
                        onError = { msg -> isLoading = false; error = msg }
                    )
                } else {
                    viewModel.verifyOtp(
                        otp = otp,
                        onSuccess = { isLoading = false; onSuccess() },
                        onError = { msg -> isLoading = false; error = msg }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = otp.length == 6 && !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("Verify & Log In", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = {}) { Text("Resend Code") }
    }
}
