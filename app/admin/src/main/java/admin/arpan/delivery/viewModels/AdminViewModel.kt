package admin.arpan.delivery.viewModels

import admin.arpan.delivery.repositories.AdminRepository
import core.arpan.delivery.models.Setting
import android.app.Application
import androidx.lifecycle.*
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import core.arpan.delivery.utils.networking.responses.FeedbacksResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val application: Application,
    private val adminRepository: AdminRepository,
) : ViewModel() {

  fun clearRedisCache() =
    liveData(Dispatchers.IO) {
      var dataResponse: DefaultResponse
      try {
        dataResponse = adminRepository.clearRedisCache()
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

  fun getAllFeedbacks() = liveData(Dispatchers.IO) {
    var dataResponse: FeedbacksResponse
    try {
      dataResponse = adminRepository.getFeedbacksResponse()
    } catch (e: Exception) {
      dataResponse =
        FeedbacksResponse(
          true,
          e.message.toString()
        )
      e.printStackTrace()
    }
    emit(dataResponse)
  }

}