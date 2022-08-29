package da.arpan.delivery.viewModels

import androidx.lifecycle.*
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.User
import core.arpan.delivery.utils.networking.requests.GetOrdersRequest
import core.arpan.delivery.utils.networking.requests.SendNotificationRequest
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import core.arpan.delivery.utils.networking.responses.GetOrdersResponse
import da.arpan.delivery.repositories.DARepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class DAViewModel @Inject constructor(
  private val daRepository: DARepository
) : ViewModel() {

  fun getSelfProfile() =
    liveData(Dispatchers.IO) {
      var data: User
      try {
        data = daRepository.getSelfProfile()
      } catch (e: Exception) {
        data = User(true, "Error : ${e.message.toString()}")
        e.printStackTrace()
      }
      emit(data)
    }

  fun updateSelfProfile(hashMap: HashMap<String, Any>) =
    liveData(Dispatchers.IO) {
      var data: User
      try {
        data = daRepository.updateSelfProfile(hashMap)
      } catch (e: Exception) {
        data = User(true, "Error : ${e.message.toString()}")
        e.printStackTrace()
      }
      emit(data)
    }

  fun getOrders(getOrdersRequest: GetOrdersRequest) =
    liveData(Dispatchers.IO) {
      var dataResponse: GetOrdersResponse
      try {
        dataResponse = daRepository.getOrders(getOrdersRequest)
      } catch (e: Exception) {
        dataResponse =
          GetOrdersResponse(
            true,
            e.message.toString(),
            ArrayList(), null,
            null, null,
            null
          )
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun getOrderById(id: String) =
    liveData(Dispatchers.IO) {
      var dataResponse: OrderItemMain
      try {
        dataResponse = daRepository.getOrderById(id)
      } catch (e: Exception) {
        dataResponse =
          OrderItemMain(
            true,
            e.message.toString())
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun addRegTokenDA(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = daRepository.addRegistrationTokenDA(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

  fun sendNotificationToUser(sendNotificationRequest: SendNotificationRequest) =
    liveData(Dispatchers.IO) {
      var dataResponse: DefaultResponse
      try {
        dataResponse = daRepository.sendNotificationToUser(sendNotificationRequest)
      } catch (e: Exception) {
        dataResponse =
          DefaultResponse(
            true,
            e.message.toString()
          )
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun requestPayment(id: String, notification: Boolean) =
    liveData(Dispatchers.IO) {
      var dataResponse: OrderItemMain
      try {
        dataResponse = daRepository.requestPayment(id, notification)
      } catch (e: Exception) {
        dataResponse =
          OrderItemMain(
            true,
            e.message.toString())
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun completeOrder(id: String, notification: Boolean) =
    liveData(Dispatchers.IO) {
      var dataResponse: OrderItemMain
      try {
        dataResponse = daRepository.completeOrder(id, notification)
      } catch (e: Exception) {
        dataResponse =
          OrderItemMain(
            true,
            e.message.toString())
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun pickUpOrder(id: String, notification: Boolean) =
    liveData(Dispatchers.IO) {
      var dataResponse: OrderItemMain
      try {
        dataResponse = daRepository.pickUpOrder(id, notification)
      } catch (e: Exception) {
        dataResponse =
          OrderItemMain(
            true,
            e.message.toString())
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun acceptOrder(orderId: String, accept: Boolean) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = daRepository.acceptOrder(orderId, accept)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }
}