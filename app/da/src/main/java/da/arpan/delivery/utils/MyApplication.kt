package da.arpan.delivery.utils

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : MultiDexApplication(){

  override fun onCreate() {
    super.onCreate()
  }

}