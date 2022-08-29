package admin.arpan.delivery.repositories

import core.arpan.delivery.models.User
import admin.arpan.delivery.utils.networking.responses.GetAllDAResponse
import admin.arpan.delivery.utils.networking.responses.GetAllUserResponse
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DARepository
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

  suspend fun getAll(): GetAllDAResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllDAResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getAllDAs("Bearer $accessToken", 100, 1)
    }
  }

  suspend fun getActiveDas(): GetAllUserResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllUserResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getActiveDAs("Bearer $accessToken", 100, 1)
    }
  }

  suspend fun update(id: String, data: HashMap<String, Any>): User {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      User()
    } else {
      retrofitBuilder.apiService.updateDA("Bearer $accessToken", id, data)
    }
  }

  suspend fun create(data: User): User {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      User(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createDA("Bearer $accessToken", data)
    }
  }

  suspend fun delete(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteDA("Bearer $accessToken", id)
    }
  }

}