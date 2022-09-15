package da.arpan.delivery.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.OrderOldItems
import core.arpan.delivery.models.User
import core.arpan.delivery.utils.*
import core.arpan.delivery.utils.networking.requests.GetOrdersRequest
import da.arpan.delivery.R
import da.arpan.delivery.adapters.OrderOldMainItemRecyclerAdapter
import da.arpan.delivery.ui.auth.MainActivity
import da.arpan.delivery.ui.fragments.MyIdFragment
import da.arpan.delivery.ui.order.OrderHistoryFragmentNew
import da.arpan.delivery.ui.order.OrderHistoryPage
import da.arpan.delivery.viewModels.AuthViewModel
import da.arpan.delivery.viewModels.DAViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_alert_layout_main.view.*
import kotlinx.android.synthetic.main.dialog_text_input_da_status.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), OrderHistoryPage {

  private var ordersMainArrayList = ArrayList<OrderItemMain>()
  var ordersMainArrayListCompleted = ArrayList<OrderItemMain>()
  private var ordersMainHashMap = HashMap<String, ArrayList<OrderItemMain>>()
  private var ordersMainHashMapCompletedOrders = HashMap<String, ArrayList<OrderItemMain>>()
  var ordersMainOldItemsArrayList = ArrayList<OrderOldItems>()
  var ordersMainOldItemsArrayListCompleted = ArrayList<OrderOldItems>()
  lateinit var orderAdapterMain: OrderOldMainItemRecyclerAdapter
  lateinit var orderAdapterMainCompleted: OrderOldMainItemRecyclerAdapter
  var selectedRecyclerAdapterItem = 0
  var mainItemPositionsRecyclerAdapter = 0
  private var startTimeMonthMillis = 0L
  private var endTimeMonthMillis = 0L
  var firestoreDatabaseSnapshot: QuerySnapshot? = null
  var thisMonthsMyIncome = 0
  var thisMonthsArpanBokeya = 0
  var thisMonthsTotalOrder = 0
  var daAgent = User()
  private val daViewModel: DAViewModel by viewModels()
  private val authViewModel: AuthViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)
    initVars()
    initLogics()
    initFirebaseMessaging()
    checkIntentStatus()
  }

  private fun checkIntentStatus() {
    if (intent.getStringExtra("orderID").isNullOrEmpty() && intent.getStringExtra("user_id")
        .isNullOrEmpty()
    ) {

    } else {
      val bundle = Bundle()
      bundle.putString("orderID", intent.getStringExtra("orderID").toString())
      bundle.putString("customerId", intent.getStringExtra("user_id").toString())
      val fg = OrderHistoryFragmentNew(this)
      fg.arguments = bundle
      fg.show(supportFragmentManager, "")
    }
  }

  private fun initLogics() {
    title_text_view.setOnClickListener {
      WalletDialogFragment().show(supportFragmentManager, "")
    }
    val mBottomSheetLayout = findViewById<View>(R.id.bottom_sheet_layout)
    val sheetBehavior = BottomSheetBehavior.from<View>(mBottomSheetLayout)

    val header_Arrow_Image = findViewById<View>(R.id.bottom_sheet_arrow)

    header_Arrow_Image.setOnClickListener(View.OnClickListener {
      if (sheetBehavior.getState() !== BottomSheetBehavior.STATE_EXPANDED) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
      } else {
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
      }
    })
    sheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {}
      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        header_Arrow_Image.setRotation(slideOffset * 180)
      }
    })
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
          LiveDataUtil.observeOnce(daViewModel.addRegTokenDA(token)) {
            Log.e("FCM", token.toString())
          }
        })
    }
  }

  override fun onResume() {
    super.onResume()
    loadSecondData()
  }

  private fun initVars() {
    orderAdapterMain =
      OrderOldMainItemRecyclerAdapter(this, ordersMainOldItemsArrayList, daViewModel)
    orderAdapterMainCompleted =
      OrderOldMainItemRecyclerAdapter(this, ordersMainOldItemsArrayListCompleted, daViewModel)

    val c = Calendar.getInstance() // this takes current date
    c[Calendar.DAY_OF_MONTH] = 1    //Date set to start
    c[Calendar.HOUR_OF_DAY] = 0

    val d = Calendar.getInstance() // this takes current date
    d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH) //Date set to end month
    d[Calendar.HOUR_OF_DAY] = 24

    startTimeMonthMillis = c.timeInMillis
    endTimeMonthMillis = d.timeInMillis
  }

  private fun placeOrderMainData() {
    ordersMainHashMap.clear()
    ordersMainOldItemsArrayList.clear()
    for (order in ordersMainArrayList) {
      val date = getDate(order.orderPlacingTimeStamp, "dd-MM-yyyy")
      if (ordersMainHashMap.containsKey(date)) {
        ordersMainHashMap[date]!!.add(order)
      } else {
        ordersMainHashMap[date!!] = ArrayList()
        ordersMainHashMap[date]!!.add(order)
      }
    }
    for (item in ordersMainHashMap.entries) {
      val order = OrderOldItems(
        date = item.key,
        orders = item.value
      )
      order.orders.reverse()
      ordersMainOldItemsArrayList.add(
        order
      )
    }
    Collections.sort(ordersMainOldItemsArrayList, kotlin.Comparator { o1, o2 ->
      o1.orders[0].orderPlacingTimeStamp.compareTo(o2.orders[0].orderPlacingTimeStamp)
    })
    ordersMainOldItemsArrayList.reverse()
    ordersRecyclerView.layoutManager = LinearLayoutManager(this)
    ordersRecyclerView.adapter = orderAdapterMain

    noProductsText.visibility = View.GONE
    progressBar.visibility = View.GONE
    ordersRecyclerView.visibility = View.VISIBLE
  }

  private fun loadSecondData() {
    swipeRefresh.setOnRefreshListener {
      swipeRefresh.isRefreshing = false
      loadSecondData()
    }
    val progressDialog = createProgressDialog()
    noProductsText.visibility = View.GONE
    progressBar.visibility = View.VISIBLE
    ordersRecyclerView.visibility = View.GONE
    LiveDataUtil.observeOnce(daViewModel.getSelfProfile()) { selfProfile ->
      daAgent = selfProfile
      if (selfProfile.error == true) {
        Log.e("ERROR : ", selfProfile.toString())
        showToast("Failed to fetch data", FancyToast.ERROR)
      } else {
        if (selfProfile.daStatus == true) {
          if (!selfProfile.daStatusTitle.isNullOrEmpty()) {
            currentStatusTextView.visibility = View.VISIBLE
            currentStatusTextView.text = selfProfile.daStatusTitle.toString()
          } else {
            currentStatusTextView.visibility = View.GONE
          }
          title_text_view.text = selfProfile.name.toString()
          if (selfProfile.activeNow == true) {
            radioGroup.check(R.id.activeRadio)
          } else {
            radioGroup.check(R.id.inactiveRadio)
          }
          changeStatusCard.setOnClickListener {
            val view = LayoutInflater.from(this@HomeActivity)
              .inflate(R.layout.dialog_alert_layout_main, null)
            val dialog = AlertDialog.Builder(this@HomeActivity)
              .setView(view).create()
            view.btnNoDialogAlertMain.text = "No"
            view.btnYesDialogAlertMain.text = "Yes"
            view.titleTextView.text = " You are going to change your activity status! "
            view.messageTextView.visibility = View.GONE
            view.btnNoDialogAlertMain.setOnClickListener {
              dialog.dismiss()
            }
            view.btnYesDialogAlertMain.setOnClickListener {
              dialog.dismiss()
              changeStatusCard.isEnabled = false
              val hashMap = HashMap<String, Any>()
              if (radioGroup.checkedRadioButtonId == R.id.activeRadio) {
                radioGroup.check(R.id.inactiveRadio)
                hashMap["activeNow"] = false
              } else {
                radioGroup.check(R.id.activeRadio)
                hashMap["activeNow"] = true
              }
              LiveDataUtil.observeOnce(daViewModel.updateSelfProfile(hashMap)) {
                changeStatusCard.isEnabled = true
              }
            }
            dialog.show()
          }
          changeStatusCard.setOnLongClickListener {
            val dialogForStatusTextChange = AlertDialog.Builder(this).create()
            val dialogForStatusTextChangeView =
              LayoutInflater.from(this).inflate(R.layout.dialog_text_input_da_status, null)
            if (selfProfile.daStatusTitle.isNullOrEmpty()) {
              dialogForStatusTextChangeView.textDialogTextInputDaStatusFieldEdittext.setText(
                selfProfile.daStatusTitle.toString()
              )
            }
            dialogForStatusTextChangeView.textDialogTextInputDaStatusButton.setOnClickListener {
              progressDialog.show()
              val hashMap = HashMap<String, Any>()
              hashMap["daStatusTitle"] =
                dialogForStatusTextChangeView.textDialogTextInputDaStatusFieldEdittext.text.toString()
              LiveDataUtil.observeOnce(daViewModel.updateSelfProfile(hashMap)) {
                progressDialog.dismiss()
                showToast("Successfully Set Status", FancyToast.SUCCESS)
              }
              dialogForStatusTextChange.dismiss()
            }
            dialogForStatusTextChangeView.textDialogTextInputDaStatusButtonRemove.setOnClickListener {
              progressDialog.show()
              val hashMap = HashMap<String, Any>()
              hashMap["daStatusTitle"] = ""
              LiveDataUtil.observeOnce(daViewModel.updateSelfProfile(hashMap)) {
                progressDialog.dismiss()
                showToast("Successfully Removed Status", FancyToast.SUCCESS)
              }
              dialogForStatusTextChange.dismiss()
            }
            dialogForStatusTextChange.setView(dialogForStatusTextChangeView)
            dialogForStatusTextChange.show()
            true
          }
          val getOrdersRequest = GetOrdersRequest()
          getOrdersRequest.startTimeMillis = startTimeMonthMillis
          getOrdersRequest.endTimeMillis = endTimeMonthMillis
          LiveDataUtil.observeOnce(daViewModel.getOrders(getOrdersRequest)) {
            if (it.error == true) {
              showToast("Failed to load orders", FancyToast.ERROR)
            } else {
              ordersMainArrayList.clear()
              ordersMainArrayListCompleted.clear()
              for (document in it.results!!) {
                if (document.orderStatus == "COMPLETED") {
                  ordersMainArrayListCompleted.add(document)
                } else {
                  ordersMainArrayList.add(document)
                }
              }
              if (ordersMainArrayList.isNotEmpty()) {
                placeOrderMainData()
              } else {
                noProductsText.visibility = View.VISIBLE
                noProductsTextView.text = "You don't have any order now!"
                progressBar.visibility = View.GONE
                ordersRecyclerView.visibility = View.GONE
              }
              if (ordersMainArrayListCompleted.isNotEmpty()) {
                placeOrderMainDataCompletedOrders()
              } else {
                noProductsTextBottomSheet.visibility = View.VISIBLE
                noProductsTextViewBottomSheet.text = "You don't have any order now!"
                progressBarBottomSheet.visibility = View.GONE
                ordersRecyclerViewBottomSheet.visibility = View.GONE
              }

            }
          }
        } else {
          val view = LayoutInflater.from(this@HomeActivity)
            .inflate(R.layout.dialog_alert_layout_main, null)
          val dialog = AlertDialog.Builder(this@HomeActivity)
            .setView(view).create()
          view.btnNoDialogAlertMain.visibility = View.GONE
          view.btnYesDialogAlertMain.text = "Ok"
          view.titleTextView.text = "Your account is disabled by Arpan.\n" +
                  "Please contact with admin."
          view.messageTextView.visibility = View.GONE
          view.btnNoDialogAlertMain.setOnClickListener {
            dialog.dismiss()
          }
          view.btnYesDialogAlertMain.setOnClickListener {
            dialog.dismiss()
            finish()
          }
          dialog.show()
        }
      }
    }
  }

  private fun placeOrderMainDataCompletedOrders() {
    ordersMainHashMapCompletedOrders.clear()
    ordersMainOldItemsArrayListCompleted.clear()
    for (order in ordersMainArrayListCompleted) {
      if (order.orderCompletedStatus != "CANCELLED") {
        val date = getDate(order.orderPlacingTimeStamp, "dd-MM-yyyy").toString()
        if (ordersMainHashMapCompletedOrders.containsKey(date)) {
          ordersMainHashMapCompletedOrders[date]!!.add(order)
        } else {
          ordersMainHashMapCompletedOrders[date!!] = ArrayList()
          ordersMainHashMapCompletedOrders[date]!!.add(order)
        }
      }
    }
    val calculationToday = CalculationLogics()
      .calculateArpansStatsForArpan(ordersMainArrayListCompleted.filter {
        getDate(System.currentTimeMillis(), "dd-MM-yyyy") == getDate(
          it.orderPlacingTimeStamp,
          "dd-MM-yyyy"
        ).toString()
      } as ArrayList<OrderItemMain>)
    val calculationMonth =
      CalculationLogics().calculateArpansStatsForArpan(ordersMainArrayListCompleted)
    var yourIncome = 0
    var arpansDue = 0
    var todaysOrders = 0
    Log.e("TEsT",calculationToday.agentsIncome.toString())
    Log.e("TEsT",calculationToday.agentsDueToArpan.toString())
    Log.e("TEsT",calculationToday.agentsDueToArpanPermanent.toString())
    Log.e("TEsT",calculationToday.agentsIncomePermanent.toString())
    if (daAgent.daCategory == Constants.DA_PERM) {
      yourIncome = calculationToday.agentsIncomePermanent
      arpansDue = calculationToday.agentsDueToArpanPermanent
      todaysOrders = calculationToday.totalOrders
      thisMonthsMyIncome = calculationMonth.agentsIncomePermanent
      thisMonthsArpanBokeya = calculationMonth.agentsDueToArpanPermanent
      thisMonthsTotalOrder = calculationMonth.totalOrders
    } else {
      yourIncome = calculationToday.agentsIncome
      arpansDue = calculationToday.agentsDueToArpan
      todaysOrders = calculationToday.totalOrders
      thisMonthsMyIncome = calculationMonth.agentsIncome
      thisMonthsArpanBokeya = calculationMonth.agentsDueToArpan
      thisMonthsTotalOrder = calculationMonth.totalOrders
    }

    myIncomeTextView.text = yourIncome.toString()
    todayCompletedOrdersTextView.text = todaysOrders.toString()
    todayDueArpanOrdersTextView.text = arpansDue.toString()

    for (item in ordersMainHashMapCompletedOrders.entries) {
      val order = OrderOldItems(
        date = item.key,
        orders = item.value
      )
      order.orders.reverse()
      ordersMainOldItemsArrayListCompleted.add(
        order
      )
    }
    Collections.sort(ordersMainOldItemsArrayListCompleted, kotlin.Comparator { o1, o2 ->
      o1.orders[0].orderPlacingTimeStamp.compareTo(o2.orders[0].orderPlacingTimeStamp)
    })
    ordersMainOldItemsArrayListCompleted.reverse()
    ordersRecyclerViewBottomSheet.layoutManager = LinearLayoutManager(this)
    ordersRecyclerViewBottomSheet.adapter = orderAdapterMainCompleted

    noProductsTextBottomSheet.visibility = View.GONE
    progressBarBottomSheet.visibility = View.GONE
    ordersRecyclerViewBottomSheet.visibility = View.VISIBLE
  }

  fun logOutNowTheUser(view: View) {
    if (drawerMainHome.isDrawerOpen(GravityCompat.START)) {
      drawerMainHome.closeDrawer(GravityCompat.START)
    }
    val view = LayoutInflater.from(this@HomeActivity)
      .inflate(R.layout.dialog_alert_layout_main, null)
    val dialog = AlertDialog.Builder(this@HomeActivity)
      .setView(view).create()
    view.btnNoDialogAlertMain.text = "No"
    view.btnYesDialogAlertMain.text = "Yes"
    view.titleTextView.text = "You are about to log out!"
    view.messageTextView.visibility = View.GONE
    view.btnNoDialogAlertMain.setOnClickListener {
      dialog.dismiss()
    }
    view.btnYesDialogAlertMain.setOnClickListener {
      dialog.dismiss()
      AlertDialog.Builder(this)
        .setTitle("Sure to logout ?")
        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
          dialog.dismiss()
          val dia = createProgressDialog()
          dia.show()
          FirebaseMessaging.getInstance().token.addOnCompleteListener {
            LiveDataUtil.observeOnce(authViewModel.getLogoutResponse(it.result.toString())) {
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
    dialog.show()
  }

  fun openDrawerImageView(view: View) {
    if (!drawerMainHome.isDrawerOpen(GravityCompat.START)) {
      drawerMainHome.openDrawer(GravityCompat.START)
    }
  }

  fun openMyIdPage(view: View) {
    if (drawerMainHome.isDrawerOpen(GravityCompat.START)) {
      drawerMainHome.closeDrawer(GravityCompat.START)
    }
    MyIdFragment().show(supportFragmentManager, "")
  }

  fun openDeliveryChargeChart(view: View) {
    if (drawerMainHome.isDrawerOpen(GravityCompat.START)) {
      drawerMainHome.closeDrawer(GravityCompat.START)
    }
    DeliveryChargeChartFragment().show(supportFragmentManager, "")
  }

  override fun closeDialog(instance: DialogFragment) {
    instance.dismiss()
    loadSecondData()
  }
}