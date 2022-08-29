package da.arpan.delivery.viewModels

import core.arpan.delivery.models.Tokens
import android.app.Application
import android.content.Intent
import androidx.lifecycle.*
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import core.arpan.delivery.utils.networking.responses.LoginResponse
import core.arpan.delivery.utils.networking.responses.RefreshResponse
import core.arpan.delivery.utils.showToast
import da.arpan.delivery.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
  private val application: Application,
  private val authRepository: AuthRepository
) : ViewModel() {

  fun getLoginResponse(phone: String, otp: String) =
    liveData(Dispatchers.IO) {
      var loginResponse: LoginResponse
      try {
        loginResponse = authRepository.getLoginResponse(phone, otp)
      } catch (e: Exception) {
        loginResponse = LoginResponse(true, "Error : ${e.message.toString()}", null, null)
        e.printStackTrace()
      }
      emit(loginResponse)
    }

  fun getSendOTPResponse(phone: String, signature: String) =
    liveData(Dispatchers.IO) {
      var loginResponse: DefaultResponse
      try {
        loginResponse = authRepository.getSendOTPResponse(phone, signature)
      } catch (e: Exception) {
        loginResponse = DefaultResponse(true, "Error : ${e.message.toString()}")
        e.printStackTrace()
      }
      emit(loginResponse)
    }

  fun getLogoutResponse(token:String) =
    liveData(Dispatchers.IO) {
      var loginResponse: DefaultResponse
      try {
        loginResponse = authRepository.getLogoutResponse(token)
      } catch (e: Exception) {
        loginResponse = DefaultResponse(true, "Error : ${e.message.toString()}")
        e.printStackTrace()
      }
      emit(loginResponse)
    }

  fun getRefreshResponse() = liveData(Dispatchers.IO) {
    var refreshResponse: RefreshResponse
    try {
      refreshResponse = authRepository.getRefreshResponse()
    } catch (e: Exception) {
      refreshResponse = RefreshResponse(true, "Error : ${e.message.toString()}", null, null)
      e.printStackTrace()
    }
    emit(refreshResponse)
  }

  fun switchActivity(loginResponse: LoginResponse, activity: Class<Any?>) {
    if (loginResponse.error != true) {
      if (loginResponse.user!!.roles.contains("da")) {
        val intent = Intent(
          application.applicationContext,
          activity
        )
        authRepository.saveLoginResponse(loginResponse)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        application.applicationContext.startActivity(intent)
      } else {
        application.applicationContext.showToast(
          "You're number is not registered as a delivery agent.",
          FancyToast.ERROR
        )
      }
    } else {
      application.applicationContext.showToast(
        loginResponse.message!!,
        FancyToast.ERROR
      )
    }
  }

  fun switchActivity(refreshResponse: RefreshResponse, activity: Class<Any?>) {
    if (refreshResponse.error != true) {
      if (refreshResponse.access != null &&
        refreshResponse.refresh != null
      ) {
        val intent = Intent(
          application.applicationContext,
          activity
        )
        authRepository.saveTokens(Tokens(refreshResponse.access!!, refreshResponse.refresh!!))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        application.applicationContext.startActivity(intent)
      } else {
        application.applicationContext.showToast(
          refreshResponse.message!!,
          FancyToast.ERROR
        )
      }
    } else {
      application.applicationContext.showToast(
        refreshResponse.message!!,
        FancyToast.ERROR
      )
    }
  }

}