package com.loyaltyapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loyaltyapp.data.NetworkModule
import com.loyaltyapp.data.RedeemPointsRequest
import com.loyaltyapp.data.RestaurantItem
import com.loyaltyapp.data.colorFromString
import com.loyaltyapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantsScreen(viewModel: AppViewModel, onProfileClick: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showBrowse by remember { mutableStateOf(false) }
    var selectedRestaurant by remember { mutableStateOf<RestaurantItem?>(null) }
    var searchText by remember { mutableStateOf("") }

    val myRestaurants = state.allRestaurants.filter { state.userRestaurantIds.contains(it.Id) }

    val filteredMine = if (searchText.isBlank()) myRestaurants
    else myRestaurants.filter {
        it.Name.contains(searchText, ignoreCase = true) ||
                it.Cuisine.contains(searchText, ignoreCase = true)
    }

    val filteredAll = if (searchText.isBlank()) state.allRestaurants
    else state.allRestaurants.filter {
        it.Name.contains(searchText, ignoreCase = true) ||
                it.Cuisine.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showBrowse) "Browse Restaurants" else "My Restaurants") },
                navigationIcon = {
                    if (showBrowse) {
                        TextButton(onClick = { showBrowse = false }) { Text("Done") }
                    } else {
                        IconButton(onClick = { showBrowse = true }) {
                            Icon(Icons.Default.Add, "Browse restaurants", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search restaurants or cuisine") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (!showBrowse) {
                if (myRestaurants.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No restaurants added yet", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Browse and add restaurants to see their offers",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showBrowse = true },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Browse Restaurants") }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredMine) { r ->
                            RestaurantIconCard(restaurant = r, onClick = { selectedRestaurant = r })
                        }
                    }
                }
            } else {
                Text(
                    "Tap + to add a restaurant to your list",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(100.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAll) { r ->
                        val isAdded = state.userRestaurantIds.contains(r.Id)
                        RestaurantIconCard(
                            restaurant = r,
                            isAdded = isAdded,
                            showAddButton = true,
                            onAdd = {
                                if (isAdded) viewModel.removeRestaurant(r.Id)
                                else viewModel.addRestaurant(r.Id)
                            },
                            onClick = { selectedRestaurant = r }
                        )
                    }
                }
            }
        }
    }

    selectedRestaurant?.let { r ->
        RestaurantDetailSheet(
            restaurant = r,
            isUserRestaurant = state.userRestaurantIds.contains(r.Id),
            userId = state.userId,
            onRemove = { viewModel.removeRestaurant(r.Id) },
            onDismiss = { selectedRestaurant = null }
        )
    }
}

@Composable
fun RestaurantIconCard(
    restaurant: RestaurantItem,
    isAdded: Boolean = false,
    showAddButton: Boolean = false,
    onAdd: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val color = colorFromString(restaurant.Color)

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(restaurant.Icon, fontSize = 22.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    restaurant.Name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 12.sp
                )
                Text(
                    restaurant.Cuisine,
                    fontSize = 9.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (showAddButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(
                            if (isAdded) color else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isAdded) Icons.Default.Check else Icons.Default.Add,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailSheet(
    restaurant: RestaurantItem,
    isUserRestaurant: Boolean,
    userId: Int?,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val color = colorFromString(restaurant.Color)
    var accumulated by remember { mutableIntStateOf(0) }
    var visitCount by remember { mutableIntStateOf(0) }
    var showRedeemDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(restaurant.Id) {
        if (userId != null) {
            scope.launch {
                try {
                    val r = NetworkModule.api.getUserPoints(userId = userId, restaurantId = restaurant.Id)
                    if (r.success) { accumulated = r.accumulated; visitCount = r.visits }
                } catch (_: Exception) {}
            }
        }
    }

    if (showRedeemDialog) {
        RedeemPointsDialog(
            placeName = restaurant.Name,
            color = color,
            currentBalance = accumulated,
            onDismiss = { showRedeemDialog = false },
            onRedeem = { pts ->
                if (userId != null) {
                    scope.launch {
                        try {
                            val r = NetworkModule.api.redeemPoints(
                                RedeemPointsRequest(userId = userId, restaurantId = restaurant.Id, pointsToRedeem = pts)
                            )
                            if (r.success) {
                                accumulated = r.newBalance ?: (accumulated - pts)
                                showRedeemDialog = false
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(restaurant.Icon, fontSize = 32.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(restaurant.Name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(restaurant.Cuisine, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(20.dp))

            // Accumulated points — big number
            Text("Your Points Here", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text("$accumulated", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = color)
            Text("$visitCount visit${if (visitCount == 1) "" else "s"} · ${restaurant.Points} pts/visit",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            Spacer(Modifier.height(20.dp))
            Row {
                InfoPill(label = "Tier", value = restaurant.Tier)
                Spacer(Modifier.width(12.dp))
                InfoPill(label = "Pts/Visit", value = "${restaurant.Points}")
            }

            if (isUserRestaurant && accumulated >= 100) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showRedeemDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Text("🎁 Redeem Points for Discount")
                }
            }

            if (isUserRestaurant) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onRemove(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove from My Restaurants")
                }
            }
        }
    }
}
