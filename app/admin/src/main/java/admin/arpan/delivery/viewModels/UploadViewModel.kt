package admin.arpan.delivery.viewModels

import android.app.Application
import androidx.lifecycle.*
import core.arpan.delivery.models.CommonResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val application: Application,
    private val uploadRepository: admin.arpan.delivery.repositories.UploadRepository,
) : ViewModel() {

  fun uploadItem(body: MultipartBody.Part, path: String) =
    liveData(Dispatchers.IO) {
      var uploadResponse: CommonResponse<String>?
      try {
        uploadResponse = uploadRepository.uploadFile(body, path)
      } catch (e: Exception) {
        uploadResponse = null
        e.printStackTrace()
      }
      emit(uploadResponse)
    }
}