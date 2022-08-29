package admin.arpan.delivery.repositories

import core.arpan.delivery.models.Location
import admin.arpan.delivery.utils.networking.responses.GetAllLocationResponse
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository
@Inject constructor(
    private val retrofitBuilder: admin.arpan.delivery.utils.networking.RetrofitBuilder,
    private val preference: Preference
) {

  private fun getAccessToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.access.token
    } else {
      null
    }
  }

  suspend fun getAll(): GetAllLocationResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllLocationResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getAllLocations("Bearer $accessToken", 100, 1)
    }
  }

  suspend fun update(id: String, data: HashMap<String, Any>): Location {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Location(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.updateLocation("Bearer $accessToken", id, data)
    }
  }

  suspend fun create(data: Location): Location {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Location(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createLocation("Bearer $accessToken", data)
    }
  }

  suspend fun delete(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteLocation("Bearer $accessToken", id)
    }
  }

}