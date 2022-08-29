package admin.arpan.delivery.viewModels

import core.arpan.delivery.models.User
import admin.arpan.delivery.utils.networking.responses.GetAllDAResponse
import admin.arpan.delivery.utils.networking.responses.GetAllUserResponse
import android.app.Application
import androidx.lifecycle.*
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class DAViewModel @Inject constructor(
    private val application: Application,
    private val daRepository: admin.arpan.delivery.repositories.DARepository,
) : ViewModel() {

  fun getAllItems() =
    liveData(Dispatchers.IO) {
      var dataResponse: GetAllDAResponse
      try {
        dataResponse = daRepository.getAll()
      } catch (e: Exception) {
        dataResponse =
          GetAllDAResponse(
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

  fun getActiveDas() =
    liveData(Dispatchers.IO) {
      var dataResponse: GetAllUserResponse
      try {
        dataResponse = daRepository.getActiveDas()
      } catch (e: Exception) {
        dataResponse =
          GetAllUserResponse(
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

  fun createItem(data: User) =
    liveData(Dispatchers.IO) {
      var dataResponse: User
      try {
        dataResponse = daRepository.create(data)
      } catch (e: Exception) {
        dataResponse = User()
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun updateItem(id: String, data: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var dataResponse: User
    try {
      dataResponse = daRepository.update(id, data)
    } catch (e: Exception) {
      dataResponse = User()
      e.printStackTrace()
    }
    emit(dataResponse)
  }

  fun deleteItem(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = daRepository.delete(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

}