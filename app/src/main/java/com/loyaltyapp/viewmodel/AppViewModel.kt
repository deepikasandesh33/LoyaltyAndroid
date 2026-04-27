package com.loyaltyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyaltyapp.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppState(
    val isLoggedIn: Boolean = false,
    val userId: Int? = null,
    val userName: String = "",
    val userPhone: String = "",
    val userZipCode: String = "",
    val userBirthDate: String = "",
    val pendingPhone: String = "",
    val pendingProfile: UserProfile? = null,
    val allStores: List<StoreItem> = emptyList(),
    val userStoreIds: Set<Int> = emptySet(),
    val allRestaurants: List<RestaurantItem> = emptyList(),
    val userRestaurantIds: Set<Int> = emptySet(),
    val storeOffers: List<OfferItem> = emptyList(),
    val restaurantOffers: List<OfferItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AppViewModel : ViewModel() {
    private val api = NetworkModule.api

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // ── Auth ──

    fun lookupPhone(phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(phone))
                if (response.success && response.user != null) {
                    _state.value = _state.value.copy(
                        pendingPhone = phone,
                        pendingProfile = response.user
                    )
                    onSuccess()
                } else {
                    onError(response.message)
                }
            } catch (e: Exception) {
                onError("Could not connect to server.")
            }
        }
    }

    fun verifyOtp(otp: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (otp == "000000") { onError("Invalid code."); return }
        val profile = _state.value.pendingProfile ?: return
        _state.value = _state.value.copy(
            isLoggedIn = true,
            userId = profile.id,
            userName = profile.name,
            userPhone = profile.phone,
            userZipCode = profile.zipCode,
            userBirthDate = profile.birthDate,
            pendingPhone = "",
            pendingProfile = null
        )
        onSuccess()
        loadAll(profile.id)
    }

    fun register(
        name: String, phone: String, zipCode: String, birthDate: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.register(RegistrationRequest(name, phone, zipCode, birthDate))
                if (response.success) {
                    _state.value = _state.value.copy(pendingPhone = phone)
                    onSuccess()
                } else {
                    onError(response.message)
                }
            } catch (e: Exception) {
                onError("Could not connect to server.")
            }
        }
    }

    fun completeSignupOtp(otp: String, name: String, phone: String, zipCode: String, birthDate: String,
                          onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (otp == "000000") { onError("Invalid code."); return }
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(phone))
                if (response.success && response.user != null) {
                    val profile = response.user
                    _state.value = _state.value.copy(
                        isLoggedIn = true,
                        userId = profile.id,
                        userName = profile.name,
                        userPhone = profile.phone,
                        userZipCode = profile.zipCode,
                        userBirthDate = profile.birthDate
                    )
                    onSuccess()
                    loadAll(profile.id)
                } else {
                    onError("Registration error. Please try again.")
                }
            } catch (e: Exception) {
                onError("Could not connect to server.")
            }
        }
    }

    fun logout() {
        _state.value = AppState()
    }

    // ── Data Loading ──

    private fun loadAll(userId: Int) {
        loadStores(userId)
        loadRestaurants(userId)
        loadOffers(userId)
    }

    fun loadStores(userId: Int) {
        viewModelScope.launch {
            try {
                val all = api.getStores().stores
                val userStores = api.getUserStores(userId).stores.map { it.Id }.toSet()
                _state.value = _state.value.copy(allStores = all, userStoreIds = userStores)
            } catch (_: Exception) {}
        }
    }

    fun loadRestaurants(userId: Int) {
        viewModelScope.launch {
            try {
                val all = api.getRestaurants().restaurants
                val userRestaurants = api.getUserRestaurants(userId).restaurants.map { it.Id }.toSet()
                _state.value = _state.value.copy(allRestaurants = all, userRestaurantIds = userRestaurants)
            } catch (_: Exception) {}
        }
    }

    fun loadOffers(userId: Int) {
        viewModelScope.launch {
            try {
                val store = api.getUserOffers(userId).offers
                val restaurant = api.getUserRestaurantOffers(userId).offers
                _state.value = _state.value.copy(storeOffers = store, restaurantOffers = restaurant)
            } catch (_: Exception) {}
        }
    }

    // ── Store subscriptions ──

    fun addStore(storeId: Int) {
        val userId = _state.value.userId ?: return
        viewModelScope.launch {
            try {
                api.addUserStore(UserStoreBody(userId, storeId))
                _state.value = _state.value.copy(
                    userStoreIds = _state.value.userStoreIds + storeId
                )
            } catch (_: Exception) {}
        }
    }

    fun removeStore(storeId: Int) {
        val userId = _state.value.userId ?: return
        viewModelScope.launch {
            try {
                api.removeUserStore(UserStoreBody(userId, storeId))
                _state.value = _state.value.copy(
                    userStoreIds = _state.value.userStoreIds - storeId
                )
                loadOffers(userId)
            } catch (_: Exception) {}
        }
    }

    // ── Restaurant subscriptions ──

    fun addRestaurant(restaurantId: Int) {
        val userId = _state.value.userId ?: return
        viewModelScope.launch {
            try {
                api.addUserRestaurant(UserRestaurantBody(userId, restaurantId))
                _state.value = _state.value.copy(
                    userRestaurantIds = _state.value.userRestaurantIds + restaurantId
                )
            } catch (_: Exception) {}
        }
    }

    fun removeRestaurant(restaurantId: Int) {
        val userId = _state.value.userId ?: return
        viewModelScope.launch {
            try {
                api.removeUserRestaurant(UserRestaurantBody(userId, restaurantId))
                _state.value = _state.value.copy(
                    userRestaurantIds = _state.value.userRestaurantIds - restaurantId
                )
                loadOffers(userId)
            } catch (_: Exception) {}
        }
    }
}
