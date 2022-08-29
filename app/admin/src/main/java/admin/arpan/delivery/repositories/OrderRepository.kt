package admin.arpan.delivery.repositories

import core.arpan.delivery.utils.networking.requests.GetOrdersRequest
import core.arpan.delivery.utils.networking.responses.GetOrdersResponse
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository
@Inject constructor(
    private val retrofitBuilder: admin.arpan.delivery.utils.networking.RetrofitBuilder,
    private val preference: Preference
) {

  suspend fun createNewOrder(orderItemMain: OrderItemMain): OrderItemMain {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      OrderItemMain(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createNewOrder("Bearer $accessToken", orderItemMain)
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

  suspend fun update(id: String, data: HashMap<String, Any>): OrderItemMain {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      OrderItemMain()
    } else {
      retrofitBuilder.apiService.updateOrder("Bearer $accessToken", id, data)
    }
  }
  suspend fun getItemById(id: String): OrderItemMain {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      OrderItemMain()
    } else {
      retrofitBuilder.apiService.getOrderById("Bearer $accessToken", id)
    }
  }

  suspend fun delete(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteOrder("Bearer $accessToken", id)
    }
  }

  private fun getAccessToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.access.token
    } else {
      null
    }
  }
}