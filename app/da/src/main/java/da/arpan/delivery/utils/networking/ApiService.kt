package da.arpan.delivery.utils.networking

import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.User
import core.arpan.delivery.utils.networking.requests.*
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import core.arpan.delivery.utils.networking.responses.GetOrdersResponse
import core.arpan.delivery.utils.networking.responses.LoginResponse
import core.arpan.delivery.utils.networking.responses.RefreshResponse
import retrofit2.http.*

interface ApiService {
  @POST("auth/send-otp")
  suspend fun sendOTP(@Body request: SendOTPRequest): DefaultResponse

  @POST("auth/login-with-phone")
  suspend fun login(@Body request: LoginOTPRequest): LoginResponse

  @POST("auth/refresh")
  suspend fun refreshSession(@Body request: RefreshRequest): RefreshResponse

  @POST("auth/logout")
  suspend fun logout(
    @Header("Authorization") accessToken: String,
    @Body refreshToken: HashMap<String, Any>
  ): DefaultResponse

  @POST("notifications/send-notification-to-user")
  suspend fun sendNotificationToUser(
    @Header("Authorization") accessToken: String,
    @Body sendNotificationRequest: SendNotificationRequest
  ): DefaultResponse

  @POST("notifications/send-notification-to-da")
  suspend fun sendNotificationToDA(
    @Header("Authorization") accessToken: String,
    @Body sendNotificationRequest: SendNotificationRequest
  ): DefaultResponse

  @GET("das/self")
  suspend fun getSelfProfile(
    @Header("Authorization") accessToken: String
  ): User

  @PATCH("das/self")
  suspend fun updateSelfProfile(
    @Header("Authorization") accessToken: String,
    @Body hashMap: HashMap<String,Any>
  ): User

  @POST("das/registration-tokens-da")
  suspend fun addDARegistrationToken(
    @Header("Authorization") accessToken: String,
    @Body fcmToken: HashMap<String, Any>
  ): DefaultResponse

  @POST("das/orders/accept")
  suspend fun acceptOrderDA(
    @Header("Authorization") accessToken: String,
    @Body data: HashMap<String, Any>
  ): DefaultResponse

  @POST("das/orders/single")
  suspend fun getOrderById(
    @Header("Authorization") accessToken: String,
    @Body data: HashMap<String, Any>
  ): OrderItemMain

  @POST("das/orders/single/pickup")
  suspend fun pickUpOrder(
    @Header("Authorization") accessToken: String,
    @Body data: HashMap<String, Any>
  ): OrderItemMain

  @POST("das/orders/single/complete")
  suspend fun completeOrder(
    @Header("Authorization") accessToken: String,
    @Body data: HashMap<String, Any>
  ): OrderItemMain

  @POST("das/orders/single/request-payment")
  suspend fun requestPayment(
    @Header("Authorization") accessToken: String,
    @Body data: HashMap<String, Any>
  ): OrderItemMain

  @POST("das/orders/filter")
  suspend fun getOrders(
    @Header("Authorization") accessToken: String,
    @Body getOrdersRequest: GetOrdersRequest
  ): GetOrdersResponse

}
