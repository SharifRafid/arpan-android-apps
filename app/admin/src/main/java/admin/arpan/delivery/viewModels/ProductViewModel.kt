package admin.arpan.delivery.viewModels

import core.arpan.delivery.models.Product
import admin.arpan.delivery.utils.networking.responses.GetAllProductsResponse
import android.app.Application
import androidx.lifecycle.*
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val application: Application,
    private val productRepository: admin.arpan.delivery.repositories.ProductRepository,
) : ViewModel() {

  fun getProducts() =
    liveData(Dispatchers.IO) {
      var getAllProductsResponse: GetAllProductsResponse
      try {
        getAllProductsResponse = productRepository.getProducts()
      } catch (e: Exception) {
        getAllProductsResponse = GetAllProductsResponse(true, e.message.toString())
        e.printStackTrace()
      }
      emit(getAllProductsResponse)
    }

  fun getProductsByCategoryId(id: String, shop_key: String) =
    liveData(Dispatchers.IO) {
      var getAllProductsResponse: GetAllProductsResponse
      try {
        getAllProductsResponse = productRepository.getProductsByCategoryId(id, shop_key)
      } catch (e: Exception) {
        getAllProductsResponse = GetAllProductsResponse(true, e.message.toString())
        e.printStackTrace()
      }
      emit(getAllProductsResponse)
    }

  fun updateProductItem(id: String, item: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var productResponse: Product
    try {
      productResponse = productRepository.updateProduct(id, item)
    } catch (e: Exception) {
      productResponse = Product()
      e.printStackTrace()
    }
    emit(productResponse)
  }

  fun createProductItem(product: Product) =
    liveData(Dispatchers.IO) {
      var productResponse: Product
      try {
        productResponse = productRepository.createProduct(product)
      } catch (e: Exception) {
        productResponse = Product()
        e.printStackTrace()
      }
      emit(productResponse)
    }

  fun deleteProduct(id: String) =
    liveData(Dispatchers.IO) {
      var defaultResponse: DefaultResponse
      try {
        defaultResponse = productRepository.deleteProduct(id)
      } catch (e: Exception) {
        defaultResponse = DefaultResponse(true, "Error : ${e.message.toString()}")
        e.printStackTrace()
      }
      emit(defaultResponse)
    }
}