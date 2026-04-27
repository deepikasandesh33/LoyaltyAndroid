package com.loyaltyapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loyaltyapp.viewmodel.AppViewModel

@Composable
fun SignupScreen(
    viewModel: AppViewModel,
    onOtpRequired: (name: String, phone: String, zip: String, birthDate: String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() &&
            phone.length == 10 &&
            zipCode.length == 5 &&
            birthDate.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) { Text("Cancel") }
        }

        Spacer(Modifier.height(16.dp))

        Icon(
            Icons.Default.PersonAdd, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("Create your account", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Join to start tracking your loyalty points",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 14.sp
        )

        Spacer(Modifier.height(28.dp))

        // Full Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(14.dp))

        // Phone
        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
            label = { Text("Phone Number") },
            leadingIcon = { Text("+1", modifier = Modifier.padding(start = 8.dp)) },
            trailingIcon = { Icon(Icons.Default.Phone, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(14.dp))

        // Birth Date (MM/DD/YYYY text field)
        OutlinedTextField(
            value = birthDate,
            onValueChange = { if (it.length <= 10) birthDate = it },
            label = { Text("Birth Date (MM/DD/YYYY)") },
            leadingIcon = { Icon(Icons.Default.Cake, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            placeholder = { Text("01/01/1990") }
        )

        Spacer(Modifier.height(14.dp))

        // Zip Code
        OutlinedTextField(
            value = zipCode,
            onValueChange = { if (it.length <= 5) zipCode = it.filter { c -> c.isDigit() } },
            label = { Text("Zip Code") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true; error = ""
                viewModel.register(
                    name = name.trim(), phone = phone, zipCode = zipCode, birthDate = birthDate,
                    onSuccess = { isLoading = false; onOtpRequired(name.trim(), phone, zipCode, birthDate) },
                    onError = { msg -> isLoading = false; error = msg }
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = isFormValid && !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) CircularProgressIndicator(
                Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp
            )
            else Text("Create Account", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(32.dp))
    }
}
