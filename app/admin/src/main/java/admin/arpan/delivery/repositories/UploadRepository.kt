package admin.arpan.delivery.repositories

import core.arpan.delivery.models.CommonResponse
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository
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

  suspend fun uploadFile(file: MultipartBody.Part, path: String): CommonResponse<String>? {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      null
    } else {
      retrofitBuilder.apiService.uploadFile("Bearer $accessToken", file, path)
    }

  }

  suspend fun deleteCategory(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteCategory("Bearer $accessToken", id)
    }
  }
}