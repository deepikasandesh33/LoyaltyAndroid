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
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.loyaltyapp.data.StoreItem
import com.loyaltyapp.data.colorFromString
import com.loyaltyapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoresScreen(viewModel: AppViewModel, onProfileClick: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showBrowse by remember { mutableStateOf(false) }
    var selectedStore by remember { mutableStateOf<StoreItem?>(null) }
    var searchText by remember { mutableStateOf("") }

    val myStores = state.allStores.filter { state.userStoreIds.contains(it.Id) }
    val filteredMine = if (searchText.isBlank()) myStores
    else myStores.filter { it.Name.contains(searchText, ignoreCase = true) }

    val filteredAll = if (searchText.isBlank()) state.allStores
    else state.allStores.filter { it.Name.contains(searchText, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showBrowse) "Browse Stores" else "My Stores") },
                navigationIcon = {
                    if (showBrowse) {
                        TextButton(onClick = { showBrowse = false }) { Text("Done") }
                    } else {
                        IconButton(onClick = { showBrowse = true }) {
                            Icon(Icons.Default.Add, "Browse stores", tint = MaterialTheme.colorScheme.primary)
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
            // Search bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search stores") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!showBrowse) {
                // My Stores
                if (myStores.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No stores added yet", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Browse and add stores to see their offers",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showBrowse = true },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Browse Stores") }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredMine) { store ->
                            StoreIconCard(store = store, onClick = { selectedStore = store })
                        }
                    }
                }
            } else {
                // Browse all stores
                Text(
                    "Tap + to add a store to your list",
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
                    items(filteredAll) { store ->
                        val isAdded = state.userStoreIds.contains(store.Id)
                        StoreIconCard(
                            store = store,
                            isAdded = isAdded,
                            showAddButton = true,
                            onAdd = {
                                if (isAdded) viewModel.removeStore(store.Id)
                                else viewModel.addStore(store.Id)
                            },
                            onClick = { selectedStore = store }
                        )
                    }
                }
            }
        }
    }

    // Store Detail Sheet
    selectedStore?.let { store ->
        StoreDetailSheet(
            store = store,
            isUserStore = state.userStoreIds.contains(store.Id),
            userId = state.userId,
            onRemove = { viewModel.removeStore(store.Id) },
            onDismiss = { selectedStore = null }
        )
    }
}

@Composable
fun StoreIconCard(
    store: StoreItem,
    isAdded: Boolean = false,
    showAddButton: Boolean = false,
    onAdd: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val color = colorFromString(store.Color)

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
                    Text(store.Icon, fontSize = 22.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    store.Name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 12.sp
                )
                Text(
                    "${store.Points} pts",
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
                        ),
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
fun StoreDetailSheet(
    store: StoreItem,
    isUserStore: Boolean,
    userId: Int?,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val color = colorFromString(store.Color)
    var accumulated by remember { mutableIntStateOf(0) }
    var visitCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(store.Id) {
        if (userId != null) {
            scope.launch {
                try {
                    val r = NetworkModule.api.getUserPoints(userId = userId, storeId = store.Id)
                    if (r.success) { accumulated = r.accumulated; visitCount = r.visits }
                } catch (_: Exception) {}
            }
        }
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
                Text(store.Icon, fontSize = 32.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(store.Name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(store.Tier, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(20.dp))

            // Accumulated points — big number
            Text("Your Points Here", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text("$accumulated", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = color)
            Text("$visitCount visit${if (visitCount == 1) "" else "s"} · ${store.Points} pts/visit",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            Spacer(Modifier.height(20.dp))
            HStack {
                InfoPill(label = "Tier", value = store.Tier)
                Spacer(Modifier.width(12.dp))
                InfoPill(label = "Pts/Visit", value = "${store.Points}")
            }

            if (isUserStore) {
                Spacer(Modifier.height(20.dp))
                OutlinedButton(
                    onClick = { onRemove(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove from My Stores")
                }
            }
        }
    }
}

@Composable
private fun HStack(content: @Composable RowScope.() -> Unit) {
    Row(content = content)
}

@Composable
fun InfoPill(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
