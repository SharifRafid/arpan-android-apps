package admin.arpan.delivery.repositories

import admin.arpan.delivery.utils.networking.RetrofitBuilder
import android.util.Log
import core.arpan.delivery.models.Tokens
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.requests.LoginRequest
import core.arpan.delivery.utils.networking.requests.RefreshRequest
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import core.arpan.delivery.utils.networking.responses.LoginResponse
import core.arpan.delivery.utils.networking.responses.RefreshResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository
@Inject constructor(
  private val retrofitBuilder: RetrofitBuilder,
  private val preference: Preference
) {

  private fun getAccessToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.access.token
    } else {
      null
    }
  }
  suspend fun getLoginResponse(email: String, password: String) =
    retrofitBuilder.apiService.login(LoginRequest(email, password))

  suspend fun getLogoutResponse(token:String): DefaultResponse {
    val data = HashMap<String, Any>()
    data["refreshToken"] = getRefreshToken()!!
    data["registrationToken"] = token
    return retrofitBuilder.apiService.logout(
      "Bearer ${getAccessToken()!!}", data
    )
  }

  suspend fun getRefreshResponse(): RefreshResponse {
    val refreshToken = getRefreshToken()
    return if (refreshToken == null) {
      RefreshResponse(true, "Not logged in", null, null)
    } else {
      retrofitBuilder.apiService.refreshSession(RefreshRequest(refreshToken))
    }
  }

  private fun getRefreshToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.refresh.token
    } else {
      null
    }
  }

  fun saveLoginResponse(loginResponse: LoginResponse) {
    if (loginResponse.user != null) {
      preference.saveUser(loginResponse.user!!)
    }
    if (loginResponse.tokens != null) {
      preference.saveTokens(loginResponse.tokens!!)
    }
  }

  fun saveTokens(tokens: Tokens) {
    preference.saveTokens(tokens)
  }
}