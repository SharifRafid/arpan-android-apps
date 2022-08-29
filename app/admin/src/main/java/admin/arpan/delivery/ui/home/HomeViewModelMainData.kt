package admin.arpan.delivery.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.SavedPrefClientTf
import kotlin.collections.ArrayList

class HomeViewModelMainData : ViewModel() {
    var mainUserSavedPrefClientTfArrayList = MutableLiveData(ArrayList<SavedPrefClientTf>())
    fun setUserSavedPrefClientTfArrayList(tempUserSavedPrefClientTfArrayList: java.util.ArrayList<SavedPrefClientTf>) {
        mainUserSavedPrefClientTfArrayList.value = tempUserSavedPrefClientTfArrayList
    }
    fun getUserSavedPrefClientTfArrayList() : MutableLiveData<ArrayList<SavedPrefClientTf>> {
        return mainUserSavedPrefClientTfArrayList
    }
    var currentSelectedOrderItemToEdit = OrderItemMain()

    var todayOrdersMainArrayList : MutableLiveData<ArrayList<OrderItemMain>> = MutableLiveData(ArrayList<OrderItemMain>())
    fun setOrdersOneDayDataMainList(tempOrdersMainArrayList: ArrayList<OrderItemMain>) {
        todayOrdersMainArrayList.value = tempOrdersMainArrayList
    }
    fun getOrdersOneDayDataMainList() : MutableLiveData<ArrayList<OrderItemMain>>{
        return todayOrdersMainArrayList
    }

}