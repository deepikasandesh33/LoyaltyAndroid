package com.loyaltyapp.data

import androidx.compose.ui.graphics.Color

data class RegistrationRequest(
    val name: String,
    val phone: String,
    val zipCode: String,
    val birthDate: String
)

data class RegistrationResponse(
    val success: Boolean,
    val message: String,
    val userId: Int?
)

data class LoginRequest(val phone: String)

data class UserProfile(
    val id: Int,
    val name: String,
    val phone: String,
    val zipCode: String,
    val birthDate: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserProfile?
)

data class StoreItem(
    val Id: Int,
    val Name: String,
    val Icon: String,
    val Color: String,
    val Points: Int,
    val Tier: String
)

data class StoresResponse(
    val success: Boolean,
    val stores: List<StoreItem>
)

data class RestaurantItem(
    val Id: Int,
    val Name: String,
    val Icon: String,
    val Color: String,
    val Cuisine: String,
    val Points: Int,
    val Tier: String
)

data class RestaurantsResponse(
    val success: Boolean,
    val restaurants: List<RestaurantItem>
)

data class OfferItem(
    val Id: Int,
    val StoreId: Int,
    val StoreName: String,
    val StoreIcon: String,
    val StoreColor: String,
    val Title: String,
    val Description: String,
    val Discount: String,
    val Expiry: String,
    val Category: String
)

data class OffersResponse(
    val success: Boolean,
    val offers: List<OfferItem>
)

data class UserStoreBody(val userId: Int, val storeId: Int)
data class UserRestaurantBody(val userId: Int, val restaurantId: Int)

fun colorFromString(name: String): Color = when (name.lowercase()) {
    "green"  -> Color(0xFF34C759)
    "orange" -> Color(0xFFFF9500)
    "red"    -> Color(0xFFFF3B30)
    "blue"   -> Color(0xFF007AFF)
    "yellow" -> Color(0xFFFFCC00)
    "pink"   -> Color(0xFFFF2D55)
    "purple" -> Color(0xFFAF52DE)
    "teal"   -> Color(0xFF5AC8FA)
    "brown"  -> Color(0xFFA2845E)
    "black"  -> Color.Black
    "gray"   -> Color(0xFF8E8E93)
    else     -> Color(0xFF5856D6) // indigo default
}
