package admin.arpan.delivery.ui.fragments

import core.arpan.delivery.utils.CalculationLogics
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldMainItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderOldSubItemRecyclerAdapterInterface
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.OrderOldItems
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import core.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.viewModels.HomeViewModel
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.getDate
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.*
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.cancelledOrdersTextView
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.completedOrdersTextView
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.noProductsText
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.ordersTotalTextView
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.progressBar
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.recyclerView
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.totalIncomeTextView
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OrdersFilterDate.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class OrdersFilterDate : Fragment(), OrderOldSubItemRecyclerAdapterInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var contextMain : Context
    private lateinit var selectedStartDate : String
    private lateinit var selectedEndDate : String
    private lateinit var homeViewModelMainData: HomeViewModelMainData
    private lateinit var homeMainNewInterface: HomeMainNewInterface
    private val TAG = "OrdersFilterDate"
    private val viewModel: HomeViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextMain = context
        try {
            homeMainNewInterface = context as HomeMainNewInterface
        }catch (classCastException : ClassCastException){
            Log.e(TAG, "This activity does not implement the interface / listener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders_filter_date, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initVars(view)
        initLogic(view)
    }

    private fun initLogic(view: View) {
        view.startDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yy = calendar.get(Calendar.YEAR)
            val mm = calendar.get(Calendar.MONTH)
            val dd = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(contextMain,object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view2: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    selectedStartDate = "Date"+dayOfMonth+"-"+month+"-"+year
                    view.startDateButton.text = "Start Date"+selectedStartDate
                }
            }, yy, mm, dd);
            datePicker.show();
        }
        view.endDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yy = calendar.get(Calendar.YEAR)
            val mm = calendar.get(Calendar.MONTH)
            val dd = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(contextMain,object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view2: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    selectedEndDate = "Date"+dayOfMonth+"-"+month+"-"+year
                    view.endDateButton.text = "End Date"+selectedStartDate
                }
            }, yy, mm, dd)
            datePicker.show()
        }
        view.ordersDateMonthRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.pendingRadioButton){
                loadThisMonthdata(view)
            }else{
                loadLastMonthData(view)
            }
        }
        view.ordersDateMonthRadioGroup.check(R.id.pendingRadioButton)
    }

    private fun loadLastMonthData(view : View) {
        val c = Calendar.getInstance() // this takes current date
        c.add(Calendar.MONTH, -1)
        c[Calendar.DAY_OF_MONTH] = 1
        c[Calendar.HOUR_OF_DAY] = 0

        val d = Calendar.getInstance() // this takes current date
        d.add(Calendar.MONTH, -1)
        d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        d[Calendar.HOUR_OF_DAY] = 24

        val startTimeMillis = c.timeInMillis
        val endTimeMillis = d.timeInMillis

        LiveDataUtil.observeOnce(viewModel.getOrders(
            GetOrdersRequest(
                startTimeMillis,
                endTimeMillis,
                100,
                1
            )
        )){
            if(it.error == true){
                placeOrderMainData(ArrayList(), view)
            }else{
                if(it.results.isNullOrEmpty()){
                    placeOrderMainData(ArrayList(), view)
                }else{
                    placeOrderMainData(it.results!!, view)
                }
            }
        }
    }

    private fun loadThisMonthdata(view: View) {

        val c = Calendar.getInstance() // this takes current date
        c[Calendar.DAY_OF_MONTH] = 1
        c[Calendar.HOUR_OF_DAY] = 0

        val d = Calendar.getInstance() // this takes current date
        d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        d[Calendar.HOUR_OF_DAY] = 24

        val startTimeMillis = c.timeInMillis
        val endTimeMillis = d.timeInMillis

        LiveDataUtil.observeOnce(viewModel.getOrders(
            GetOrdersRequest(
                startTimeMillis,
                endTimeMillis,
                100,
                1
            )
        )){
            if(it.error == true){
                placeOrderMainData(ArrayList(), view)
            }else{
                if(it.results.isNullOrEmpty()){
                    placeOrderMainData(ArrayList(), view)
                }else{
                    placeOrderMainData(it.results!!, view)
                }
            }
        }
    }

    private fun placeOrderMainData(ordersMainArrayList : ArrayList<OrderItemMain>, view: View) {
        val ordersMainHashMap = HashMap<String, ArrayList<OrderItemMain>>()
        val ordersMainOldItemsArrayList = ArrayList<OrderOldItems>()
        val calculationResult = CalculationLogics().calculateArpansStatsForArpan(ordersMainArrayList)
        view.ordersTotalTextView.text = calculationResult.totalOrders.toString()
        view.totalIncomeTextView.text = calculationResult.arpansIncome.toString()
        view.completedOrdersTextView.text = calculationResult.completed.toString()
        view.cancelledOrdersTextView.text = calculationResult.cancelled.toString()

        ordersMainHashMap.clear()
        ordersMainOldItemsArrayList.clear()
        for(order in ordersMainArrayList){
            val date = getDate(order.orderPlacingTimeStamp!!, "dd-MM-yyyy")
            if(ordersMainHashMap.containsKey(date)){
                ordersMainHashMap[date]!!.add(order)
            }else{
                ordersMainHashMap[date!!] = ArrayList()
                ordersMainHashMap[date]!!.add(order)
            }
        }
        for(item in ordersMainHashMap.entries){
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
            o1.orders[0].orderPlacingTimeStamp!!.compareTo(o2.orders[0].orderPlacingTimeStamp!!)
        })
        ordersMainOldItemsArrayList.reverse()
        view.recyclerView.layoutManager = LinearLayoutManager(contextMain)
        val orderAdapterMain = OrderOldMainItemRecyclerAdapter(
          contextMain, ordersMainOldItemsArrayList, this,
          showStats = true,
          showDaStatsMode = false,
          da_category = "",
            viewModel
        )
        view.recyclerView.adapter = orderAdapterMain

        view.noProductsText.visibility = View.GONE
        view.progressBar.visibility = View.GONE
        view.recyclerView.visibility = View.VISIBLE
    }

    private fun initVars(view: View) {
        homeViewModelMainData = activity?.let { ViewModelProvider(it).get(HomeViewModelMainData::class.java) }!!
        view.title_text_view.setOnClickListener {
            homeMainNewInterface.callOnBackPressed()
        }
        view.title_text_view.setOnLongClickListener {
            homeMainNewInterface.navigateToFragment(R.id.ordersFilterByDay)
            true
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OrdersFilterDate.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OrdersFilterDate().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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
        val bundle = Bundle()
        bundle.putString("orderID",docId)
        bundle.putString("customerId",userId)
        homeMainNewInterface.navigateToFragment(R.id.orderHistoryFragment, bundle)
    }
}