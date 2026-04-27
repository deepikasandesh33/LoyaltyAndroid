package com.loyaltyapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loyaltyapp.ui.utils.generateQrBitmap
import com.loyaltyapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScreen(viewModel: AppViewModel, onProfileClick: () -> Unit) {
    val state by viewModel.state.collectAsState()

    val qrPayload = remember(state.userId, state.userPhone) {
        val id = state.userId ?: 0
        val phone = state.userPhone.filter { it.isDigit() }
        "LOYALTY-$id-$phone"
    }

    val qrBitmap = remember(qrPayload) { generateQrBitmap(qrPayload) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My QR Code") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(Color(0xFF5856D6), Color(0xFFAF52DE))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // QR card
            Card(
                modifier = Modifier.size(260.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(210.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = if (state.userName.isNotEmpty()) state.userName else "User",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            state.userId?.let { uid ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Member #$uid",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Show this code at checkout to earn points",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
