package da.arpan.delivery.repositories

import core.arpan.delivery.models.Location
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
class PublicRepository
@Inject constructor(
  private val retrofitBuilder: RetrofitBuilder) {

  suspend fun getLocations(): ArrayList<Location> {
    return retrofitBuilder.apiService.getLocations()
  }

}