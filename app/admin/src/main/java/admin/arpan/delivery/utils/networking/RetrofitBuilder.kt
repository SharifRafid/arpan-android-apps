package admin.arpan.delivery.utils.networking

import com.google.gson.GsonBuilder
import core.arpan.delivery.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilder {

  private lateinit var retrofit: Retrofit

  fun getRetrofit(): Retrofit {

    if (!this::retrofit.isInitialized) {
          retrofit = Retrofit.Builder()
              .baseUrl(Constants.SERVER_BASE_URL)
              .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder()
                  .setLenient()
                  .create()
              ))
              .build() //Doesn't require the adapter
      }
      return retrofit
  }

  val apiService: admin.arpan.delivery.utils.networking.ApiService = getRetrofit().create(admin.arpan.delivery.utils.networking.ApiService::class.java)
}