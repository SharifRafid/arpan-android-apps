package admin.arpan.delivery.viewModels

import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.utils.networking.requests.GetOrdersRequest
import core.arpan.delivery.utils.networking.responses.GetOrdersResponse
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val application: Application,
  private val orderRepository: admin.arpan.delivery.repositories.OrderRepository
) : ViewModel() {

  fun createNewOrder(orderItemMain: OrderItemMain) =
    liveData(Dispatchers.IO) {
      var defaultResponse: OrderItemMain
      try {
        defaultResponse = orderRepository.createNewOrder(orderItemMain)
      } catch (e: Exception) {
        defaultResponse = OrderItemMain(true, "Error : ${e.message.toString()}")
        e.printStackTrace()
      }
      emit(defaultResponse)
    }

  fun getOrders(getOrdersRequest: GetOrdersRequest) =
    liveData(Dispatchers.IO) {
      var getOrdersResponse: GetOrdersResponse
      try {
        getOrdersResponse = orderRepository.getOrders(getOrdersRequest)
      } catch (e: Exception) {
        getOrdersResponse = GetOrdersResponse(
          true, "Error : ${e.message.toString()}",
          null, null, null, null, null
        )
        e.printStackTrace()
      }
      emit(getOrdersResponse)
    }

}