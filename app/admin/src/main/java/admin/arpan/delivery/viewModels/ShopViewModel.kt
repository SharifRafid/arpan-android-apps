package admin.arpan.delivery.viewModels

import core.arpan.delivery.models.Shop
import admin.arpan.delivery.utils.networking.responses.GetAllShopsResponse
import android.app.Application
import androidx.lifecycle.*
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val application: Application,
    private val shopRepository: admin.arpan.delivery.repositories.ShopRepository,
) : ViewModel() {

  fun getShops() =
    liveData(Dispatchers.IO) {
      var getAllShopsResponse: GetAllShopsResponse
      try {
        getAllShopsResponse = shopRepository.getShops()
      } catch (e: Exception) {
        getAllShopsResponse =
          GetAllShopsResponse(true, e.message.toString(), ArrayList(), null, null, null, null)
        e.printStackTrace()
      }
      emit(getAllShopsResponse)
    }

  fun createShopItem(shop: Shop) =
    liveData(Dispatchers.IO) {
      var shopResponse: Shop
      try {
        shopResponse = shopRepository.createShop(shop)
      } catch (e: Exception) {
        shopResponse = Shop()
        e.printStackTrace()
      }
      emit(shopResponse)
    }

  fun updateShopItem(id: String, shop: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var shopResponse: Shop
    try {
      shopResponse = shopRepository.updateShop(id, shop)
    } catch (e: Exception) {
      shopResponse = Shop()
      e.printStackTrace()
    }
    emit(shopResponse)
  }

  fun deleteShopItem(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = shopRepository.deleteShop(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

  fun removeCategoryFromShop(shopId: String, categoryId: String) = liveData(Dispatchers.IO) {
    var shopResponse: DefaultResponse
    try {
      shopResponse = shopRepository.removeCategoryFromShop(shopId, categoryId)
    } catch (e: Exception) {
      shopResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(shopResponse)
  }

}