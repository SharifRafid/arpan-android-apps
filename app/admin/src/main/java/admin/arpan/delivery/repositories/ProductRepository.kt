package admin.arpan.delivery.repositories

import core.arpan.delivery.models.Product
import admin.arpan.delivery.utils.networking.responses.GetAllProductsResponse
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.networking.responses.DefaultResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository
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

  suspend fun createProduct(product: Product): Product {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Product()
    } else {
      retrofitBuilder.apiService.createNewProduct("Bearer $accessToken", product)
    }
  }

  suspend fun getProducts(): GetAllProductsResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllProductsResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getAllProducts("Bearer $accessToken", 100, 1)
    }
  }


  suspend fun getProductsByCategoryId(id: String, shop_key: String): GetAllProductsResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllProductsResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getProductsByCategoryId("Bearer $accessToken", id,shop_key, 100, 1)
    }
  }

  suspend fun getProductsByShop(shop_key: String): GetAllProductsResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllProductsResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getProductsByShop("Bearer $accessToken", shop_key, 100, 1)
    }
  }


  suspend fun updateProduct(id: String, item: HashMap<String, Any>): Product {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Product()
    } else {
      retrofitBuilder.apiService.updateProduct("Bearer $accessToken", id, item)
    }
  }

  suspend fun deleteProduct(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteProduct("Bearer $accessToken", id)
    }
  }


}