package admin.arpan.delivery.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldMainItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderOldSubItemRecyclerAdapterInterface
import core.arpan.delivery.models.UserItem
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.viewModels.HomeViewModel
import admin.arpan.delivery.viewModels.UserViewModel
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.OrderOldItems
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.getDate
import core.arpan.delivery.utils.networking.requests.GetOrdersRequest
import core.arpan.delivery.utils.showToast
import core.arpan.delivery.utils.toNotNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_orders_list.view.*
import kotlinx.android.synthetic.main.fragment_users.view.*
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class UsersFragment : Fragment(), OrderOldSubItemRecyclerAdapterInterface {

  private lateinit var contextMain: Context
  private lateinit var homeMainNewInterface: HomeMainNewInterface
  private val TAG = "UsersFragment"
  private lateinit var homeViewModelMainData: HomeViewModelMainData
  private val userViewModel: UserViewModel by viewModels()
  private val viewModel: HomeViewModel by viewModels()
  private lateinit var dialogUserOrders: AlertDialog

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_users, container, false)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    contextMain = context
    try {
      homeMainNewInterface = context as HomeMainNewInterface
    } catch (classCastException: ClassCastException) {
      Log.e(TAG, "This activity does not implement the interface / listener")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    homeViewModelMainData =
      activity?.let { ViewModelProvider(it)[HomeViewModelMainData::class.java] }!!

    LiveDataUtil.observeOnce(userViewModel.getAllItems()) {
      if (it.error == true) {
        contextMain.showToast(it.message.toString(), FancyToast.ERROR)
      } else {
        contextMain.showToast(
          "Total user : ${it.totalResults} | Loaded: ${it.results.size}",
          FancyToast.ERROR
        )
        val arrayListUsers = ArrayList<UserItem>()
        for (user in it.results) {
          arrayListUsers.add(
            UserItem(
              user.id.toString(),
              user.address.toNotNull(), user.name.toNotNull(),
              user.phone.toNotNull(), user.image.toNotNull()
            )
          )
        }
        if (arrayListUsers.isNotEmpty()) {
          Log.e(TAG, arrayListUsers.size.toString())
          val c = Calendar.getInstance() // this takes current date
          c[Calendar.DAY_OF_MONTH] = 1
          c[Calendar.HOUR_OF_DAY] = 0
          c[Calendar.MINUTE] = 0
          c[Calendar.SECOND] = 0

          val d = Calendar.getInstance() // this takes current date
          d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH)
          d[Calendar.HOUR_OF_DAY] = 23
          d[Calendar.MINUTE] = 59
          d[Calendar.SECOND] = 59

          val startTimeMillis = c.timeInMillis
          val endTimeMillis = d.timeInMillis

          Log.e("START TIME MONTH", startTimeMillis.toString())
          Log.e("END TIME MONTH", endTimeMillis.toString())

          LiveDataUtil.observeOnce(
            viewModel.getOrders(
              GetOrdersRequest(
                startTimeMillis,
                endTimeMillis,
                100,
                1
              )
            )
          ) { ordersResp ->
            if (ordersResp.error == true) {
              contextMain.showToast(ordersResp.message.toString(), FancyToast.ERROR)
            } else {
              if (ordersResp.results.isNullOrEmpty()) {
                contextMain.showToast(ordersResp.message.toString(), FancyToast.ERROR)
              } else {
                for (userItem in arrayListUsers) {
                  for (order in ordersResp.results!!) {
                    if (order.userId.equals(userItem.key)) {
                      userItem.ordersCountThisMonth += 1
                      userItem.orderThisMonth.add(order)
                    }
                  }
                }
                Collections.sort(arrayListUsers, kotlin.Comparator { o1, o2 ->
                  o2.ordersCountThisMonth.compareTo(o1.ordersCountThisMonth)
                })
                val arrayStringUsers = ArrayList<String>()
                for (userItem in arrayListUsers) {
                  arrayStringUsers.add("Name : " + userItem.name + "\nAddress : " + userItem.address + "\nPhone : " + userItem.phone + "\nOrders : " + userItem.ordersCountThisMonth)
                }
                view.recyclerViewUsers.adapter = ArrayAdapter(
                  contextMain,
                  R.layout.custom_spinner_item_view,
                  arrayStringUsers
                )
                view.recyclerViewUsers.setOnItemClickListener { adapterView, view, i, l ->
                  dialogUserOrders = AlertDialog.Builder(contextMain).create()
                  dialogUserOrders.window!!.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                  )
                  val dialogForRealPickUpItemsPriceView =
                    LayoutInflater.from(contextMain).inflate(R.layout.dialog_orders_list, null)
                  dialogForRealPickUpItemsPriceView.recyclerViewProducts.layoutManager =
                    LinearLayoutManager(contextMain)
                  val ordersMainHashMap = HashMap<String, ArrayList<OrderItemMain>>()
                  val ordersMainOldItemsArrayList = ArrayList<OrderOldItems>()

                  for (order in arrayListUsers[i].orderThisMonth) {
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
                    ordersMainOldItemsArrayList.add(order)
                  }
                  Collections.sort(ordersMainOldItemsArrayList, kotlin.Comparator { o1, o2 ->
                    o1.orders[0].orderPlacingTimeStamp.compareTo(o2.orders[0].orderPlacingTimeStamp)
                  })
                  ordersMainOldItemsArrayList.reverse()
                  val orderAdapterMain = OrderOldMainItemRecyclerAdapter(
                    contextMain, ordersMainOldItemsArrayList, this, false,
                    showDaStatsMode = false, "", viewModel
                  )
                  dialogForRealPickUpItemsPriceView.recyclerViewProducts.adapter = orderAdapterMain
                  dialogUserOrders.setView(dialogForRealPickUpItemsPriceView)
                  dialogUserOrders.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                  dialogUserOrders.show()
                }
              }
            }
          }
        }
      }
    }
  }

  override fun openSelectedOrderItemAsDialog(
    position: Int,
    mainItemPositions: Int,
    docId: String,
    userId: String,
    orderItemMain: OrderItemMain
  ) {
    if (dialogUserOrders.isShowing) {
      dialogUserOrders.dismiss()
    }
    homeMainNewInterface.openSelectedOrderItemAsDialog(
      position,
      mainItemPositions,
      docId,
      userId,
      orderItemMain,
    )
  }

}