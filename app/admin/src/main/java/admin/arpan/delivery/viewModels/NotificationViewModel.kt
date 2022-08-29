package admin.arpan.delivery.viewModels

import core.arpan.delivery.utils.networking.requests.SendNotificationRequest
import android.app.Application
import androidx.lifecycle.*
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val application: Application,
    private val notificationRepository: admin.arpan.delivery.repositories.NotificationRepository,
) : ViewModel() {

  fun sendNotificationToUser(sendNotificationRequest: SendNotificationRequest) =
    liveData(Dispatchers.IO) {
      var dataResponse: DefaultResponse
      try {
        dataResponse = notificationRepository.sendNotificationToUser(sendNotificationRequest)
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

  fun sendNotificationToDA(sendNotificationRequest: SendNotificationRequest) =
    liveData(Dispatchers.IO) {
      var dataResponse: DefaultResponse
      try {
        dataResponse = notificationRepository.sendNotificationToDA(sendNotificationRequest)
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
}