package da.arpan.delivery.viewModels

import androidx.lifecycle.*
import core.arpan.delivery.models.Location
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.User
import core.arpan.delivery.utils.networking.requests.GetOrdersRequest
import core.arpan.delivery.utils.networking.requests.SendNotificationRequest
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import core.arpan.delivery.utils.networking.responses.GetOrdersResponse
import da.arpan.delivery.repositories.DARepository
import da.arpan.delivery.repositories.PublicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class PublicViewModel @Inject constructor(
  private val publicRepository: PublicRepository
) : ViewModel() {

  fun getAllLocations() =
    liveData(Dispatchers.IO) {
      var data: ArrayList<Location>
      try {
        data = publicRepository.getLocations()
      } catch (e: Exception) {
        data = ArrayList()
        e.printStackTrace()
      }
      emit(data)
    }
}