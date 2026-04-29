package com.loyaltyapp.data

import retrofit2.http.*

interface ApiService {

    @POST("register")
    suspend fun register(@Body body: RegistrationRequest): RegistrationResponse

    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("stores")
    suspend fun getStores(): StoresResponse

    @GET("user-stores")
    suspend fun getUserStores(@Query("userId") userId: Int): StoresResponse

    @POST("user-stores/add")
    suspend fun addUserStore(@Body body: UserStoreBody): RegistrationResponse

    @POST("user-stores/remove")
    suspend fun removeUserStore(@Body body: UserStoreBody): RegistrationResponse

    @GET("restaurants")
    suspend fun getRestaurants(): RestaurantsResponse

    @GET("user-restaurants")
    suspend fun getUserRestaurants(@Query("userId") userId: Int): RestaurantsResponse

    @POST("user-restaurants/add")
    suspend fun addUserRestaurant(@Body body: UserRestaurantBody): RegistrationResponse

    @POST("user-restaurants/remove")
    suspend fun removeUserRestaurant(@Body body: UserRestaurantBody): RegistrationResponse

    @GET("offers")
    suspend fun getUserOffers(@Query("userId") userId: Int): OffersResponse

    @GET("restaurant-offers")
    suspend fun getUserRestaurantOffers(@Query("userId") userId: Int): OffersResponse

    @GET("visit-history/{userId}")
    suspend fun getVisitHistory(@Path("userId") userId: Int): VisitHistoryResponse

    @GET("user-points")
    suspend fun getUserPoints(
        @Query("userId") userId: Int,
        @Query("restaurantId") restaurantId: Int? = null,
        @Query("storeId") storeId: Int? = null
    ): UserPointsResponse
}
