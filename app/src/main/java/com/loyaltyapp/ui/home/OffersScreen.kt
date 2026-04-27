package com.loyaltyapp.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loyaltyapp.data.OfferItem
import com.loyaltyapp.data.colorFromString
import com.loyaltyapp.ui.utils.generateQrBitmap
import com.loyaltyapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(viewModel: AppViewModel, onProfileClick: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedOffer by remember { mutableStateOf<OfferItem?>(null) }
    var searchText by remember { mutableStateOf("") }

    val allOffers = (state.storeOffers + state.restaurantOffers).sortedBy { it.StoreName }

    val categories = listOf("All") + allOffers.map { it.Category }.distinct().sorted()

    val filteredOffers = allOffers
        .filter { selectedCategory == "All" || it.Category == selectedCategory }
        .filter {
            searchText.isBlank() ||
                    it.StoreName.contains(searchText, ignoreCase = true) ||
                    it.Title.contains(searchText, ignoreCase = true) ||
                    it.Category.contains(searchText, ignoreCase = true)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offers") },
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
            // Search
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search offers or stores") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category filter chips
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Divider()

            if (allOffers.isEmpty()) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.LocalOffer,
                        null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No offers available", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        "Add stores or restaurants to see their offers",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    "${filteredOffers.size} offer${if (filteredOffers.size == 1) "" else "s"} available",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOffers, key = { it.Id }) { offer ->
                        OfferCard(offer = offer, onClick = { selectedOffer = offer })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    selectedOffer?.let { offer ->
        OfferDetailSheet(
            offer = offer,
            userId = viewModel.state.collectAsState().value.userId ?: 0,
            onDismiss = { selectedOffer = null }
        )
    }
}

@Composable
fun OfferCard(offer: OfferItem, onClick: () -> Unit) {
    val color = colorFromString(offer.StoreColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            // Colored left strip
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(offer.StoreIcon, fontSize = 22.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        offer.Discount,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Content
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        offer.StoreName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime, null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "Expires ${offer.Expiry}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(offer.Title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(
                    offer.Description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(offer.Category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailSheet(offer: OfferItem, userId: Int, onDismiss: () -> Unit) {
    val color = colorFromString(offer.StoreColor)
    var redeemed by remember { mutableStateOf(false) }

    val redeemPayload = remember {
        val ts = System.currentTimeMillis() / 1000
        "REDEEM-${offer.Id}-$userId-$ts"
    }
    val redeemBitmap = remember(redeemed) {
        if (redeemed) generateQrBitmap(redeemPayload) else null
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth()) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(color.copy(alpha = 0.7f), color)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(offer.StoreIcon, fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(offer.StoreName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(offer.Discount, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = color)
                Spacer(Modifier.height(8.dp))
                Text(offer.Title, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(
                    offer.Description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Category", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(offer.Category, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(Modifier.height(36.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Expires", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(offer.Expiry, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (redeemed && redeemBitmap != null) {
                    Text(
                        "Show this QR code to the cashier",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.size(200.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                bitmap = redeemBitmap.asImageBitmap(),
                                contentDescription = "Redemption QR",
                                modifier = Modifier.size(160.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QrCode, null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Offer Redeemed", color = Color(0xFF34C759), fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = { redeemed = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color)
                    ) {
                        Icon(Icons.Default.QrCode, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Redeem Offer", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
