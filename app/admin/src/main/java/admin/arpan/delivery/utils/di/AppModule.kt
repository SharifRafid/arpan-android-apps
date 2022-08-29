package admin.arpan.delivery.utils.di

import android.app.Application
import core.arpan.delivery.utils.Preference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

  @Singleton
  @Provides
  fun providePreference(application: Application): Preference {
    return Preference(application)
  }

  @Singleton
  @Provides
  fun provideRetrofitBuilder(): admin.arpan.delivery.utils.networking.RetrofitBuilder {
    return admin.arpan.delivery.utils.networking.RetrofitBuilder()
  }

}