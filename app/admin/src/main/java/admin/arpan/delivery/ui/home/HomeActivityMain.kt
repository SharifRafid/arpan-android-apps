package admin.arpan.delivery.ui.home

import admin.arpan.delivery.R
import admin.arpan.delivery.ui.feedback.UserFeedBackFragment
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.viewModels.AuthViewModel
import admin.arpan.delivery.viewModels.UserViewModel
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.Preference
import core.arpan.delivery.utils.showToast
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.SavedPrefClientTf
import core.arpan.delivery.utils.createProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeActivityMain : AppCompatActivity(), HomeMainNewInterface {

  private final val TAG = "HomeActivityMain"

  private lateinit var navController: NavController

  private var selectedRecyclerAdapterItem = 0
  private var mainItemPositionsRecyclerAdapter = 0

  private lateinit var homeViewModelMainData: HomeViewModelMainData
  private val authViewModel: AuthViewModel by viewModels()
  private val userViewModel: UserViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home_main)
    initVars()
    initLogics()
  }

  private fun initLogics() {
    initFirebaseMessaging()
    loadSavedClientsPrefData()
  }

  private fun initVars() {
    homeViewModelMainData = ViewModelProvider(this).get(HomeViewModelMainData::class.java)
    navController = Navigation.findNavController(this, R.id.main_home_fragment_container)
  }

  private fun loadSavedClientsPrefData() {
    FirebaseDatabase.getInstance().reference
      .child("SavedPrefClientTf")
      .addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val tempUserSavedPrefClientTfArrayList = ArrayList<SavedPrefClientTf>()
          for (snap in snapshot.children) {
            tempUserSavedPrefClientTfArrayList.add(snap.getValue(SavedPrefClientTf::class.java)!!)
          }
          homeViewModelMainData.setUserSavedPrefClientTfArrayList(
            tempUserSavedPrefClientTfArrayList
          )
        }

        override fun onCancelled(error: DatabaseError) {
          error.toException().printStackTrace()
        }

      })
  }

  override fun navigateToFragment(id: Int) {
    navController.navigate(id)
  }

  override fun navigateToFragment(index: Int, bundle: Bundle) {
    navController.navigate(index, bundle)
  }

  private fun initFirebaseMessaging() {
    if (Preference(this.application).getUser() != null) {
      FirebaseMessaging.getInstance().isAutoInitEnabled = true
      FirebaseMessaging.getInstance().token.addOnCompleteListener(
        OnCompleteListener { task ->
          if (!task.isSuccessful) {
            return@OnCompleteListener
          }
          val token = task.result!!
          LiveDataUtil.observeOnce(userViewModel.addRegTokenAdmin(token)) {
            Log.e("FCM", token.toString())
          }
        })
    }
  }

  override fun openSelectedOrderItemAsDialog(
    position: Int,
    mainItemPositions: Int,
    docId: String,
    userId: String,
    orderItemMain: OrderItemMain
  ) {
    selectedRecyclerAdapterItem = position
    mainItemPositionsRecyclerAdapter = mainItemPositions
    val bundle = Bundle()
    bundle.putString("orderID", docId)
    bundle.putString("customerId", userId)
    navController.navigate(R.id.orderHistoryFragment, bundle)
  }

  override fun callOnBackPressed() {
    onBackPressed()
  }

  override fun openFeedBackDialog() {
    UserFeedBackFragment().show(supportFragmentManager, "")
  }

  override fun logOutUser() {
    AlertDialog.Builder(this)
      .setTitle("Sure to logout ?")
      .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
        val dia = createProgressDialog()
        dia.show()
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
          LiveDataUtil.observeOnce(authViewModel.getLogoutResponse(it.result.toString())) {
            Log.e("TEST", it.toString())
            FirebaseMessaging.getInstance().deleteToken()
            Preference(this.application).clear()
            showToast("Logged Out", FancyToast.SUCCESS)
            dia.dismiss()
            finish()
          }
        }
      })
      .create().show()
  }

}