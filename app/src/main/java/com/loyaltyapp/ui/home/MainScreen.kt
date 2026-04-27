package com.loyaltyapp.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.loyaltyapp.ui.profile.ProfileScreen
import com.loyaltyapp.viewmodel.AppViewModel

@Composable
fun MainScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfile by remember { mutableStateOf(false) }

    val onProfileClick: () -> Unit = { showProfile = true }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.QrCode, null) },
                    label = { Text("QR Code") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ShoppingBag, null) },
                    label = { Text("Stores") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Restaurant, null) },
                    label = { Text("Restaurants") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.LocalOffer, null) },
                    label = { Text("Offers") }
                )
            }
        }
    ) { _ ->
        when (selectedTab) {
            0 -> QRCodeScreen(viewModel = viewModel, onProfileClick = onProfileClick)
            1 -> StoresScreen(viewModel = viewModel, onProfileClick = onProfileClick)
            2 -> RestaurantsScreen(viewModel = viewModel, onProfileClick = onProfileClick)
            3 -> OffersScreen(viewModel = viewModel, onProfileClick = onProfileClick)
        }
    }

    if (showProfile) {
        ProfileScreen(viewModel = viewModel, onDismiss = { showProfile = false })
    }
}
