package da.arpan.delivery.utils.di

import android.app.Application
import core.arpan.delivery.utils.Preference
import da.arpan.delivery.utils.networking.RetrofitBuilder
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
  fun provideRetrofitBuilder(): RetrofitBuilder {
    return RetrofitBuilder()
  }

}