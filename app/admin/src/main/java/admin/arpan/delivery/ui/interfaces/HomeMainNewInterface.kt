package admin.arpan.delivery.ui.interfaces

import core.arpan.delivery.models.OrderItemMain
import android.os.Bundle

interface HomeMainNewInterface {
    fun navigateToFragment(index : Int)
    fun openSelectedOrderItemAsDialog(position: Int, mainItemPositions: Int, docId: String, userId: String, orderItemMain: OrderItemMain)
    fun callOnBackPressed()
    fun openFeedBackDialog()
    fun navigateToFragment(index: Int, bundle: Bundle)
    fun logOutUser()
}