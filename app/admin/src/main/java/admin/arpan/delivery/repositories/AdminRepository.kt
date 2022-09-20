package admin.arpan.delivery.repositories

import core.arpan.delivery.models.Setting
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import core.arpan.delivery.utils.networking.responses.FeedbacksResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository
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

  suspend fun clearRedisCache(): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.clearRedisCache("Bearer $accessToken")
    }
  }
  suspend fun getFeedbacksResponse(): FeedbacksResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      FeedbacksResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getFeedbacksResponse("Bearer $accessToken")
    }
  }

}