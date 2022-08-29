package admin.arpan.delivery.viewModels

import core.arpan.delivery.models.SlidingTextItem
import admin.arpan.delivery.utils.networking.responses.GetAllNoticesResponse
import android.app.Application
import androidx.lifecycle.*
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val application: Application,
    private val noticeRepository: admin.arpan.delivery.repositories.NoticeRepository,
) : ViewModel() {

  fun getAllItems() =
    liveData(Dispatchers.IO) {
      var dataResponse: GetAllNoticesResponse
      try {
        dataResponse = noticeRepository.getAll()
      } catch (e: Exception) {
        dataResponse =
          GetAllNoticesResponse(
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

  fun createItem(data: SlidingTextItem) =
    liveData(Dispatchers.IO) {
      var dataResponse: SlidingTextItem
      try {
        dataResponse = noticeRepository.create(data)
      } catch (e: Exception) {
        dataResponse = SlidingTextItem(true, e.message.toString())
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun updateItem(id: String, data: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var dataResponse: SlidingTextItem
    try {
      dataResponse = noticeRepository.update(id, data)
    } catch (e: Exception) {
      dataResponse = SlidingTextItem(true, e.message.toString())
      e.printStackTrace()
    }
    emit(dataResponse)
  }

  fun deleteItem(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = noticeRepository.delete(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

}