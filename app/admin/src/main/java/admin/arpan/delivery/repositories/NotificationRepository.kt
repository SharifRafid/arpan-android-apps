package admin.arpan.delivery.repositories

import core.arpan.delivery.utils.networking.requests.SendNotificationRequest
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository
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

  suspend fun sendNotificationToUser(notificationRequest: SendNotificationRequest): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.sendNotificationToUser("Bearer $accessToken", notificationRequest)
    }
  }

  suspend fun sendNotificationToDA(notificationRequest: SendNotificationRequest): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.sendNotificationToDA("Bearer $accessToken", notificationRequest)
    }
  }


}