package da.arpan.delivery.repositories

import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.Tokens
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.requests.*
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import core.arpan.delivery.utils.networking.responses.GetOrdersResponse
import core.arpan.delivery.utils.networking.responses.LoginResponse
import core.arpan.delivery.utils.networking.responses.RefreshResponse
import da.arpan.delivery.utils.networking.RetrofitBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DARepository
@Inject constructor(
  private val retrofitBuilder: RetrofitBuilder,
  private val preference: Preference
) {

  private fun getRefreshToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.refresh.token
    } else {
      null
    }
  }

  private fun getAccessToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.access.token
    } else {
      null
    }
  }

  suspend fun getSelfProfile() =
    retrofitBuilder.apiService.getSelfProfile("Bearer ${getAccessToken()!!}")

  suspend fun updateSelfProfile(hashMap: HashMap<String, Any>) =
    retrofitBuilder.apiService.updateSelfProfile("Bearer ${getAccessToken()!!}", hashMap)

  suspend fun addRegistrationTokenDA(data: String): DefaultResponse {
    val accessToken = getAccessToken()
    val fcmToken = HashMap<String, Any>()
    fcmToken["fcmToken"] = data
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.addDARegistrationToken("Bearer $accessToken", fcmToken)
    }
  }

  suspend fun acceptOrder(orderId: String, data: Boolean): DefaultResponse {
    val accessToken = getAccessToken()
    val hashMap = HashMap<String, Any>()
    hashMap["orderId"] = orderId
    hashMap["acceptOrder"] = data
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.acceptOrderDA("Bearer $accessToken", hashMap)
    }
  }

  suspend fun getOrders(getOrdersRequest: GetOrdersRequest): GetOrdersResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetOrdersResponse(true, "Not logged in", null, null, null, null, null)
    } else {
      retrofitBuilder.apiService.getOrders("Bearer $accessToken", getOrdersRequest)
    }
  }

  suspend fun getOrderById(id: String): OrderItemMain {
    val accessToken = getAccessToken()
    val hashMap = HashMap<String, Any>()
    hashMap["orderId"] = id
    return if (accessToken == null) {
      OrderItemMain(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getOrderById("Bearer $accessToken", hashMap)
    }
  }

  suspend fun pickUpOrder(id: String, notification: Boolean): OrderItemMain {
    val accessToken = getAccessToken()
    val hashMap = HashMap<String, Any>()
    hashMap["orderId"] = id
    hashMap["notification"] = notification
    return if (accessToken == null) {
      OrderItemMain(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.pickUpOrder("Bearer $accessToken", hashMap)
    }
  }

  suspend fun completeOrder(id: String, notification: Boolean): OrderItemMain {
    val accessToken = getAccessToken()
    val hashMap = HashMap<String, Any>()
    hashMap["orderId"] = id
    hashMap["notification"] = notification
    return if (accessToken == null) {
      OrderItemMain(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.completeOrder("Bearer $accessToken", hashMap)
    }
  }

  suspend fun requestPayment(id: String, notification: Boolean): OrderItemMain {
    val accessToken = getAccessToken()
    val hashMap = HashMap<String, Any>()
    hashMap["orderId"] = id
    hashMap["notification"] = notification
    return if (accessToken == null) {
      OrderItemMain(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.requestPayment("Bearer $accessToken", hashMap)
    }
  }

  suspend fun sendNotificationToUser(notificationRequest: SendNotificationRequest): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.sendNotificationToUser("Bearer $accessToken", notificationRequest)
    }
  }
}