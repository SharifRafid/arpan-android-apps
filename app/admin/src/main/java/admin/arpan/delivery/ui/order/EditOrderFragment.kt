package admin.arpan.delivery.ui.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.ShopProductAddItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.ShopProductAddOrderInterface
import core.arpan.delivery.models.Category
import core.arpan.delivery.models.Location
import core.arpan.delivery.models.Shop
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.*
import admin.arpan.delivery.viewModels.*
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.format.DateUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.shashank.sony.fancytoastlib.FancyToast
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import core.arpan.delivery.models.CartProductEntity
import core.arpan.delivery.models.MainShopCartItem
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.SavedPrefClientTf
import core.arpan.delivery.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.assign_da_list_view.view.*
import kotlinx.android.synthetic.main.dialog_list_common.view.*
import kotlinx.android.synthetic.main.fragment_add_custom_order.view.*
import kotlinx.android.synthetic.main.fragment_edit_order.*
import kotlinx.android.synthetic.main.fragment_edit_order.view.*
import kotlinx.android.synthetic.main.fragment_edit_order.view.autofillAllTextBoxesId
import kotlinx.android.synthetic.main.fragment_edit_order.view.pickUpDateTextView
import kotlinx.android.synthetic.main.fragment_edit_order.view.pickUpTimeTextView
import kotlinx.android.synthetic.main.fragment_edit_order.view.radioGroup
import kotlinx.android.synthetic.main.fragment_edit_order.view.title_text_view
import java.lang.ClassCastException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditOrderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class EditOrderFragment(val orderItemMain: OrderItemMain) : DialogFragment(),
  ShopProductAddOrderInterface,
  DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
  // TODO: Rename and change types of parameters
  private var param1: String? = null
  private var param2: String? = null
  private var TAG: String = "EditOrderFragment"
  lateinit var contextMain: Context
  lateinit var viewMain: View
  private lateinit var progressDialog: Dialog
  private var currentCalc = 0
  private lateinit var homeMainNewInterface: HomeMainNewInterface

  private var deliveryChargeMain = 0
  private var daChargeMain = 0
  private var totalChargeMain = 0
  private var bkashChargeMain = 0

  private val BKASH_CHARGE_PERCENTAGE = 0.0185f

  private var productsArrayList = ArrayList<CartProductEntity>()
  private val mainShopItemHashMap = ArrayList<MainShopCartItem>()
  private lateinit var shopProductAddItemRecyclerAdapter: ShopProductAddItemRecyclerAdapter

  private val locationsNames = ArrayList<String>()
  private val locationsItems = ArrayList<Location>()

  private var dateLong = 0L
  private lateinit var homeViewModelMainData: HomeViewModelMainData

  private val shopViewModel: ShopViewModel by viewModels()
  private val orderViewModel: OrderViewModel by viewModels()
  private val locationViewModel: LocationViewModel by viewModels()
  private val categoryViewModel: CategoryViewModel by viewModels()
  private val productViewModel: ProductViewModel by viewModels()
  private val uploadViewModel: UploadViewModel by viewModels()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    contextMain = context
    try {
      homeMainNewInterface = context as HomeMainNewInterface
    } catch (classCastException: ClassCastException) {
      Log.e(TAG, "This activity does not implement the interface / listener")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      param1 = it.getString(ARG_PARAM1)
      param2 = it.getString(ARG_PARAM2)
    }
    setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AdminArpan)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_edit_order, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    homeViewModelMainData =
      activity?.let { ViewModelProvider(it)[HomeViewModelMainData::class.java] }!!
    viewMain = view
    initRadioButtonForDateAndTimePickingStuff(view)

    view.title_text_view.setOnClickListener {
      dismiss()
    }

    view.deliveryChargeTotalEditOrder.doOnTextChanged { text, start, before, count ->
      deliveryChargeMain = view.deliveryChargeTotalEditOrder.getNumValue()
    }
    view.daChargeTotalEditOrder.doOnTextChanged { text, start, before, count ->
      daChargeMain = view.daChargeTotalEditOrder.getNumValue()
    }

    val orderId = orderItemMain.id!!

    if (orderItemMain.paymentMethod == "COD") {
      view.radioGroup.check(R.id.rb2)
    } else {
      view.radioGroup.check(R.id.rb1)
    }

    viewMain.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
      if (i == R.id.rb2) {
        bkashChargeMain = 0
      } else {
        bkashChargeMain = roundNumberPriceTotal(
          (totalChargeMain + deliveryChargeMain)
                  * BKASH_CHARGE_PERCENTAGE
        ).toInt()
      }
      updateTotalProductsPricing()
    }

    setLocationData(viewMain)

    // Setting the prefetched or previous values of the order item
    view.nameEdittextEditOrder.setText(orderItemMain.userName)
    view.phoneEdittextEditOrder.setText(orderItemMain.userNumber)
    view.addressEdittextEditOrder.setText(orderItemMain.userAddress)

    view.noteEdittextEditOrder.setText(orderItemMain.userNote)
    view.deliveryChargeTotalEditOrder.setText(orderItemMain.deliveryCharge.toString())
    view.daChargeTotalEditOrder.setText(orderItemMain.daCharge.toString())
    view.totalChargeEdittextEditOrder.setText((orderItemMain.totalPrice + bkashChargeMain).toString())
    view.adminNoteEdittextEditOrder.setText(orderItemMain.adminOrderNote)
    view.cancellationReasonEdittext.setText(orderItemMain.cancelledOrderReasonFromAdmin)
    view.orderIdEdittextMain.setText(orderItemMain.orderId)
    view.pickUpTimeTextView.setText(orderItemMain.pickUpTime)

    view.pickUpDateTextView.text = findPickUpDate(orderItemMain.orderPlacingTimeStamp)

    //The total value should only be changed in case of custom orders and pickup and drops
    // this field will not be enabled for shop orders because that might mess things up
    // at least for now
    var changeTotalValue = false
    // Variables required for making the order item separation easier
    var customOrder = false
    var medicineOrder = false
    var parcelOrder = false
    var shopOrder = false
    // Pick up and drop remains in a entirely separate category than the other shops and
    // Custom orders, so this check is for that
    if (orderItemMain.pickDropOrder) {

      initDataSavingOnFirebaseLogic(
        view.nameEdittextEditOrder,
        view.phoneEdittextEditOrder,
        view.addressEdittextEditOrder,
        view.noteEdittextEditOrder,
        view.detailsCustomOrderEdittextEditOrder,
        view
      )

      view.totalChargeEdittextEditOrder.visibility = View.VISIBLE
      view.pickDropRecieverDetailsLinear.visibility = View.VISIBLE
      view.senderLocationTextView.visibility = View.VISIBLE

      view.nameEdittextEditOrderContainer.hint = "Sender Name"
      view.phoneEdittextEditOrderContainer.hint = "Sender Mobile"
      view.addressEdittextEditOrderContainer.hint = "Sender Address"

      view.nameEdittextEditOrder.setText(orderItemMain.pickDropOrderItem!!.senderName)
      view.phoneEdittextEditOrder.setText(orderItemMain.pickDropOrderItem!!.senderPhone)
      view.addressEdittextEditOrder.setText(orderItemMain.pickDropOrderItem!!.senderAddress)

      view.recieverNameEdittextEditOrder.setText(orderItemMain.pickDropOrderItem!!.recieverName)
      view.recieverPhoneEdittextEditOrder.setText(orderItemMain.pickDropOrderItem!!.recieverPhone)
      view.recieverAddressEdittextEditOrder.setText(orderItemMain.pickDropOrderItem!!.recieverAddress)

      view.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
      view.productsRecyclerView.visibility = View.GONE
      view.imageButtonAdd.visibility = View.GONE

      view.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.pickDropOrderItem!!.parcelDetails)

      if (locationsNames.contains(orderItemMain.pickDropOrderItem!!.senderLocation)) {
        view.locationsArrayList.setSelection(locationsNames.indexOf(orderItemMain.pickDropOrderItem!!.senderLocation))
      } else {
        view.locationsArrayList.setSelection(0)
      }
      if (locationsNames.contains(orderItemMain.pickDropOrderItem!!.recieverLocation)) {
        view.locationsRecieverArrayList.setSelection(locationsNames.indexOf(orderItemMain.pickDropOrderItem!!.recieverLocation))
      } else {
        view.locationsRecieverArrayList.setSelection(0)
      }

      changeTotalValue = true
    } else {
      /*This check basically helps separate between product orders and custom orders
      Custom orders (Parcel,Medicine,Custom) are right now only one per each OrderItemMain
      so we check when the products size is 1 and also there are 4(Product Item, Parcel,
      Custom Order, Medicine) booleans in the orderItemMain
      class which determines the type of product it is
       */
      if (orderItemMain.products.size == 1 && !orderItemMain.products[0].product_item) {
        //In case of custom orders the total value is easily editable and mandatory to be edited
        // be cause the admins later decide the price of the order

        view.totalChargeEdittextEditOrderContainer.visibility = View.VISIBLE
        changeTotalValue = true
        when {
          orderItemMain.products[0].parcel_item -> {
            parcelOrder = true
            view.detailsCustomOrderEdittextEditOrderContainer.hint = "ডিটেইলস"
            view.titleCustomOrderEdittextEditOrderContainer.hint = "কুরিয়ার নেম"
            view.titleCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].parcel_order_text)
            view.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].parcel_order_text_2)
            initDataSavingOnFirebaseLogic(
              view.nameEdittextEditOrder,
              view.phoneEdittextEditOrder,
              view.addressEdittextEditOrder,
              view.noteEdittextEditOrder,
              view.detailsCustomOrderEdittextEditOrder,
              view
            )
          }
          orderItemMain.products[0].medicine_item -> {
            medicineOrder = true
            view.detailsCustomOrderEdittextEditOrderContainer.hint = "ঔষোধ"
            view.titleCustomOrderEdittextEditOrderContainer.hint = "ফার্মেসি "
            view.titleCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].medicine_order_text)
            view.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].medicine_order_text_2)
            initDataSavingOnFirebaseLogic(
              view.nameEdittextEditOrder,
              view.phoneEdittextEditOrder,
              view.addressEdittextEditOrder,
              view.noteEdittextEditOrder,
              view.detailsCustomOrderEdittextEditOrder,
              view
            )
          }
          orderItemMain.products[0].custom_order_item -> {
            customOrder = true
            view.detailsCustomOrderEdittextEditOrderContainer.hint = "ডিটেইলস"
            view.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].custom_order_text)
            view.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
            initDataSavingOnFirebaseLogic(
              view.nameEdittextEditOrder,
              view.phoneEdittextEditOrder,
              view.addressEdittextEditOrder,
              view.noteEdittextEditOrder,
              view.detailsCustomOrderEdittextEditOrder,
              view
            )
          }
        }
      } else {
        // in shop order the total value will not be editable and the title and details field will be empty
        shopOrder = true
        changeTotalValue = true
        view.totalChargeEdittextEditOrderContainer.visibility = View.VISIBLE
        view.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
        view.detailsCustomOrderEdittextEditOrderContainer.visibility = View.GONE
        initDataSavingOnFirebaseLogic(
          view.nameEdittextEditOrder,
          view.phoneEdittextEditOrder,
          view.addressEdittextEditOrder,
          view.noteEdittextEditOrder,
          view.detailsCustomOrderEdittextEditOrder,
          view
        )
        productsArrayList.clear()
        for (orderItem in orderItemMain.products) {
          productsArrayList.add(orderItem.copy())
        }
        val shopsArrayListKeys = ArrayList<String>()
        val shopsArrayListNames = ArrayList<String>()
        LiveDataUtil.observeOnce(shopViewModel.getShops()) { shopResults ->
          if (shopResults.error == true) {
            contextMain.showToast(shopResults.message.toString(), FancyToast.ERROR)
          } else {
            for (shopItem in shopResults.results) {
              shopsArrayListKeys.add(shopItem.id!!)
              shopsArrayListNames.add(shopItem.name!!)
            }
            for (cartItemEntity in productsArrayList) {
              val filteredArray =
                mainShopItemHashMap.filter { it -> it.shop_doc_id == cartItemEntity.product_item_shop_key }
              if (filteredArray.isEmpty()) {
                val shopItem = MainShopCartItem()
                shopItem.shop_doc_id = cartItemEntity.product_item_shop_key
                shopItem.cart_products.add(cartItemEntity)
                mainShopItemHashMap.add(shopItem)
              } else {
                mainShopItemHashMap[mainShopItemHashMap.indexOf(filteredArray[0])]
                  .cart_products.add(cartItemEntity)
              }
            }
            shopProductAddItemRecyclerAdapter = ShopProductAddItemRecyclerAdapter(
              contextMain,
              mainShopItemHashMap, this
            )
            progressDialog = contextMain.createProgressDialog()
            if (mainShopItemHashMap.isNotEmpty()) {
              progressDialog.show()
              currentCalc = 0
              view.productsRecyclerView.layoutManager = LinearLayoutManager(view.context)
              view.productsRecyclerView.adapter = shopProductAddItemRecyclerAdapter
              updateTotalProductsPricing()
              progressDialog.dismiss()
            }
            view.productsRecyclerView.layoutManager = LinearLayoutManager(contextMain)
            view.productsRecyclerView.visibility = View.VISIBLE
            view.imageButtonAdd.visibility = View.VISIBLE
            val shopsMainList = ArrayList<Shop>()
            view.imageButtonAdd.setOnClickListener {
              val dialogShopSelectList = AlertDialog.Builder(contextMain).create()
              val dialogShopSelectListView =
                LayoutInflater.from(contextMain).inflate(R.layout.dialog_list_common, null)
              dialogShopSelectListView.dialogTitleCommon.text = "Choose Shop"
              val shopsList = ArrayList<String>()
              if (shopsMainList.isEmpty()) {
                LiveDataUtil.observeOnce(shopViewModel.getShops()) { shopResults ->
                  if (shopResults.error == true) {
                    Log.e("Error", shopResults.message.toString())
                  } else {
                    shopsMainList.addAll(shopResults.results)
                    for (shopItem in shopsMainList) {
                      shopsList.add(shopItem.name!!)
                    }
                    dialogShopSelectListView.dialogListViewCommon.adapter = ArrayAdapter(
                      contextMain,
                      androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, shopsList
                    )
                    dialogShopSelectListView.dialogListViewCommon.setOnItemClickListener { parent, view, position, id ->
                      mainShopItemHashMap.add(
                        MainShopCartItem(
                          "",
                          shopsMainList[position].id!!,
                          ArrayList(),
                          shopsMainList[position]
                        )
                      )
                      shopProductAddItemRecyclerAdapter.notifyItemInserted(mainShopItemHashMap.size - 1)
                      dialogShopSelectList.dismiss()
                    }
                    dialogShopSelectList.setView(dialogShopSelectListView)
                    dialogShopSelectList.show()
                  }
                }
              } else {
                for (shopItem in shopsMainList) {
                  shopsList.add(shopItem.name!!)
                }
                dialogShopSelectListView.dialogListViewCommon.adapter = ArrayAdapter(
                  contextMain,
                  androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, shopsList
                )
                dialogShopSelectListView.dialogListViewCommon.setOnItemClickListener { parent, view, position, id ->
                  mainShopItemHashMap.add(
                    MainShopCartItem(
                      "",
                      shopsMainList[position].id!!,
                      ArrayList(),
                      shopsMainList[position]
                    )
                  )
                  shopProductAddItemRecyclerAdapter.notifyItemInserted(mainShopItemHashMap.size - 1)
                  dialogShopSelectList.dismiss()
                }
                dialogShopSelectList.setView(dialogShopSelectListView)
                dialogShopSelectList.show()
              }
            }
          }
        }
      }
    }
    view.saveNowEditOrderButton.setOnClickListener {
      val updateOrderItem = HashMap<String, Any>()
      if (orderItemMain.pickDropOrder) {
        val pickDropOrderItem = orderItemMain.pickDropOrderItem!!.copy()
        pickDropOrderItem.senderName = view.nameEdittextEditOrder.text.toString()
        pickDropOrderItem.senderPhone = view.phoneEdittextEditOrder.text.toString()
        pickDropOrderItem.senderAddress = view.addressEdittextEditOrder.text.toString()

        pickDropOrderItem.recieverName = view.recieverNameEdittextEditOrder.text.toString()
        pickDropOrderItem.recieverPhone = view.recieverPhoneEdittextEditOrder.text.toString()
        pickDropOrderItem.recieverAddress = view.recieverAddressEdittextEditOrder.text.toString()

        pickDropOrderItem.parcelDetails = view.detailsCustomOrderEdittextEditOrder.text.toString()
        updateOrderItem["pickDropOrderItem"] = pickDropOrderItem
      } else {
        updateOrderItem["userName"] = view.nameEdittextEditOrder.text.toString()
        updateOrderItem["userNumber"] = view.phoneEdittextEditOrder.text.toString()
        updateOrderItem["userAddress"] = view.addressEdittextEditOrder.text.toString()
        updateOrderItem["userNote"] = view.noteEdittextEditOrder.text.toString()
      }
      updateOrderItem["daCharge"] = view.daChargeTotalEditOrder.text.toString().toInt()
      updateOrderItem["orderId"] = view.orderIdEdittextMain.text.toString()
      updateOrderItem["pickUpTime"] = view.pickUpTimeTextView.text.toString()
      updateOrderItem["orderPlacingTimeStamp"] = if (dateLong == 0L) {
        System.currentTimeMillis()
      } else {
        dateLong
      }
      updateOrderItem["locationItem"] = locationsItems[view.locationsArrayList.selectedItemPosition]
      updateOrderItem["deliveryCharge"] =
        view.deliveryChargeTotalEditOrder.text.toString().toInt()
      updateOrderItem["cancelledOrderReasonFromAdmin"] =
        view.cancellationReasonEdittext.text.toString()
      updateOrderItem["adminOrderNote"] = view.adminNoteEdittextEditOrder.text.toString()
      val products = ArrayList<CartProductEntity>()
      if (orderItemMain.products.isNotEmpty()) {
        for (itemPrd in orderItemMain.products) {
          products.add(itemPrd.copy())
        }
      }
      if (changeTotalValue) {
        updateOrderItem["totalPrice"] = view.totalChargeEdittextEditOrder.text.toString().toInt()
      }
      when {
        parcelOrder -> {
          products[0].parcel_order_text =
            view.titleCustomOrderEdittextEditOrder.text.toString()
          products[0].parcel_order_text_2 =
            view.detailsCustomOrderEdittextEditOrder.text.toString()
          updateOrderItem["products"] = products
        }
        medicineOrder -> {
          products[0].medicine_order_text =
            view.titleCustomOrderEdittextEditOrder.text.toString()
          products[0].medicine_order_text_2 =
            view.detailsCustomOrderEdittextEditOrder.text.toString()
          updateOrderItem["products"] = products
        }
        customOrder -> {
          products[0].custom_order_text =
            view.detailsCustomOrderEdittextEditOrder.text.toString()
          updateOrderItem["products"] = products
        }
        shopOrder -> {
          val productsAllArrayList = ArrayList<CartProductEntity>()
          for (shopItem in mainShopItemHashMap) {
            productsAllArrayList.addAll(shopItem.cart_products)
          }
          updateOrderItem["products"] = productsAllArrayList
        }
      }
      updateOrderItem["paymentMethod"] = if (view.radioGroup.checkedRadioButtonId == R.id.rb1) {
        "bKash"
      } else {
        "COD"
      }
      LiveDataUtil.observeOnce(orderViewModel.updateItem(orderId, updateOrderItem)) {
        if (it.id != null) {
          requireContext().showToast("Successfully Changed", FancyToast.SUCCESS)
          dismiss()
        } else {
          requireContext().showToast(it.message.toString(), FancyToast.ERROR)
        }
      }
    }

  }

  private fun setLocationData(view: View) {
    LiveDataUtil.observeOnce(locationViewModel.getAllItems()) {
      if (it.error == true) {
        contextMain.showToast(it.message.toString(), FancyToast.ERROR)
      } else {
        locationsNames.clear()
        locationsItems.clear()
        for (locationItem in it.results) {
          locationsNames.add(locationItem.locationName!!)
          locationsItems.add(locationItem.copy())
        }
        val adapter = ArrayAdapter(view.context, R.layout.custom_spinner_view, locationsNames)
        adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)
        view.locationsArrayList.adapter = adapter
        view.locationsRecieverArrayList.adapter = adapter

        if (locationsNames.contains(orderItemMain.locationItem!!.locationName)) {
          view.locationsArrayList.setSelection(locationsNames.indexOf(orderItemMain.locationItem!!.locationName))
        } else {
          view.locationsArrayList.setSelection(0)
        }
      }
    }
  }

  private fun findPickUpDate(orderPlacingTimeStamp: Long): String {
    if (DateUtils.isToday(orderPlacingTimeStamp)) {
      return "Today"
    } else {
      return getDate(orderPlacingTimeStamp, "dd/MM/yyyy").toString()
    }
  }

  private fun initRadioButtonForDateAndTimePickingStuff(view: View) {
    view.pickUpDateTextView.setOnClickListener {
      val now = Calendar.getInstance()
      val dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
        this,
        now.get(Calendar.YEAR), // Initial year selection
        now.get(Calendar.MONTH), // Initial month selection
        now.get(Calendar.DAY_OF_MONTH)
      )
      dpd.show(childFragmentManager, "Datepickerdialog")
    }

    view.pickUpDateTextView.setOnLongClickListener {
      view.pickUpDateTextView.text = "Today"
      dateLong = 0
      true
    }

    view.pickUpTimeTextView.setOnClickListener {
      val now = Calendar.getInstance()
      val dpd: TimePickerDialog = TimePickerDialog.newInstance(this, false)
      dpd.show(childFragmentManager, "Datepickerdialog")
    }

    view.pickUpTimeTextView.setOnLongClickListener {
      view.pickUpTimeTextView.text = "Now"
      true
    }

  }

  private fun initDataSavingOnFirebaseLogic(
    txt_name: EditText,
    txt_number: EditText,
    txt_address: EditText,
    txt_note: EditText,
    txt_details: EditText,
    view: View
  ) {
    view.autofillAllTextBoxesId.setOnClickListener {
      val arrayListPrefs = ArrayList<String>()
      val arrayListPrefsMain = ArrayList<SavedPrefClientTf>()
      for (item in homeViewModelMainData.getUserSavedPrefClientTfArrayList().value!!) {
        arrayListPrefs.add(item.user_name)
        arrayListPrefsMain.add(item)
      }
      val arrayAdapter =
        ArrayAdapter(contextMain, R.layout.custom_spinner_item_view, arrayListPrefs)
      val alertDialogPrefs = AlertDialog.Builder(contextMain).create()
      val alertDialogPrefsView =
        LayoutInflater.from(contextMain).inflate(R.layout.assign_da_list_view, null)
      alertDialogPrefsView.txtAllPrice.text = "Select User Profile"
      alertDialogPrefsView.listView.adapter = arrayAdapter
      alertDialogPrefsView.listView.setOnItemClickListener { parent, view2, position, id ->
        txt_name.setText(arrayListPrefsMain[position].user_name)
        txt_number.setText(arrayListPrefsMain[position].user_mobile)
        txt_address.setText(arrayListPrefsMain[position].user_address)
        txt_note.setText(arrayListPrefsMain[position].user_note)
        txt_details.setText(arrayListPrefsMain[position].user_order_details)
        view.deliveryChargeTotalEditOrder.setText(arrayListPrefsMain[position].delivery_charge)
        view.daChargeTotalEditOrder.setText(arrayListPrefsMain[position].da_charge)
        view.totalChargeEdittextEditOrder.setText((arrayListPrefsMain[position].total_charge + bkashChargeMain))
        alertDialogPrefs.dismiss()
      }
      alertDialogPrefsView.listView.setOnItemLongClickListener { parent, view, position, id ->
        AlertDialog.Builder(contextMain)
          .setTitle("Delete ?")
          .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            FirebaseDatabase.getInstance().reference
              .child("SavedPrefClientTf")
              .child(arrayListPrefsMain[position].key)
              .removeValue()
            dialog.dismiss()
          })
          .create()
          .show()
        alertDialogPrefs.dismiss()
        true
      }
      alertDialogPrefs.setView(alertDialogPrefsView)
      alertDialogPrefs.show()
    }
    view.autofillAllTextBoxesId.setOnLongClickListener {
      val savedPrefClientTf = SavedPrefClientTf()
      savedPrefClientTf.key = "SPC" + System.currentTimeMillis()
      savedPrefClientTf.user_name = txt_name.text.toString()
      savedPrefClientTf.user_mobile = txt_number.text.toString()
      savedPrefClientTf.user_address = txt_address.text.toString()
      savedPrefClientTf.user_note = txt_note.text.toString()
      savedPrefClientTf.user_order_details = txt_details.text.toString()
      savedPrefClientTf.delivery_charge = view.deliveryChargeTotalEditOrder.text.toString()
      savedPrefClientTf.da_charge = view.daChargeTotalEditOrder.text.toString()
      savedPrefClientTf.total_charge = view.totalChargeEdittextEditOrder.text.toString()
      if (savedPrefClientTf.key.isNotEmpty() && savedPrefClientTf.user_name.isNotEmpty()
        && savedPrefClientTf.user_mobile.isNotEmpty() && savedPrefClientTf.user_address.isNotEmpty()
      ) {
        if (homeViewModelMainData.getUserSavedPrefClientTfArrayList().value!!.any {
            it.user_address == savedPrefClientTf.user_address &&
                    it.user_name == savedPrefClientTf.user_name &&
                    it.user_note == savedPrefClientTf.user_note &&
                    it.user_order_details == savedPrefClientTf.user_order_details &&
                    it.delivery_charge == savedPrefClientTf.delivery_charge &&
                    it.da_charge == savedPrefClientTf.da_charge &&
                    it.total_charge == savedPrefClientTf.total_charge &&
                    it.user_mobile == savedPrefClientTf.user_mobile
          }) {
          contextMain.showToast("Already Added", FancyToast.ERROR)
        } else {
          FirebaseDatabase.getInstance().reference
            .child("SavedPrefClientTf")
            .child(savedPrefClientTf.key)
            .setValue(savedPrefClientTf)
            .addOnCompleteListener {
              contextMain.showToast("Success", FancyToast.SUCCESS)
            }
        }
      } else {
        contextMain.showToast("Is Empty", FancyToast.ERROR)
      }
      true
    }
  }

  override fun addProductToShop(position: Int, shopDocId: MainShopCartItem) {
    val dialogShopSelectList = AlertDialog.Builder(context).create()
    val dialogShopSelectListView =
      LayoutInflater.from(context).inflate(R.layout.dialog_list_common, null)
    dialogShopSelectListView.dialogTitleCommon.text = "Choose Category"

    LiveDataUtil.observeOnce(categoryViewModel.getProductCategoriesOfShop(shopDocId.shop_doc_id)) { categoryItemsArray ->
      if (categoryItemsArray != null) {
        val categoryItemsNames = ArrayList<String>()
        for (catItem in categoryItemsArray) {
          categoryItemsNames.add(catItem.name!!)
        }
        dialogShopSelectListView.dialogListViewCommon.adapter = ArrayAdapter(
          contextMain,
          androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, categoryItemsNames
        )
        dialogShopSelectListView.dialogListViewCommon.setOnItemClickListener { parent, view, position2, id ->
          showProductAddDialogList(position, shopDocId, categoryItemsArray[position2])
          dialogShopSelectList.dismiss()
        }
      } else {
        contextMain.showToast(
          "Error : Failed to fetch categories.",
          FancyToast.ERROR
        )
      }
    }
    dialogShopSelectList.setView(dialogShopSelectListView)
    dialogShopSelectList.show()
  }

  private fun showProductAddDialogList(
    position: Int,
    shopDocId: MainShopCartItem,
    productCategoryItem: Category
  ) {
    val dialogShopSelectList = AlertDialog.Builder(context).create()
    val dialogShopSelectListView =
      LayoutInflater.from(context).inflate(admin.arpan.delivery.R.layout.dialog_list_common, null)
    dialogShopSelectListView.dialogTitleCommon.text = "Choose Product"

    LiveDataUtil.observeOnce(
      productViewModel.getProductsByCategoryId(
        productCategoryItem.id!!,
        shopDocId.shop_doc_id
      )
    ) {
      if (it.error == true) {
        contextMain.showToast("Error : ${it.message}", FancyToast.ERROR)
      } else {
        val productsMainArrayList = it.results!!
        val productsMainArrayListNames = ArrayList<String>()
        for (document in productsMainArrayList) {
          productsMainArrayListNames.add(document.name!!)
        }
        dialogShopSelectListView.dialogListViewCommon.adapter = ArrayAdapter(
          contextMain,
          androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, productsMainArrayListNames
        )
        dialogShopSelectListView.dialogListViewCommon.setOnItemClickListener { parent, view, position2, id ->
          mainShopItemHashMap[position].cart_products.add(
            CartProductEntity(
              id = 0,
              product_item = true,
              product_item_price = productsMainArrayList[position2].price!! + productsMainArrayList[position2].arpanCharge!!,
              product_arpan_profit = productsMainArrayList[position2].arpanCharge!!,
              product_item_amount = 1,
              product_item_category_tag = productCategoryItem.name!!,
              product_item_shop_key = shopDocId.shop_doc_id,
              product_item_shop_name = shopDocId.shop_details.name!!,
              product_item_name = productsMainArrayList[position2].name!!,
              product_item_desc = productsMainArrayList[position2].shortDescription!!
            )
          )
          shopProductAddItemRecyclerAdapter.notifyItemChanged(position)
          updateTotalProductsPricing()
          dialogShopSelectList.dismiss()
        }
      }
    }
    dialogShopSelectList.setView(dialogShopSelectListView)
    dialogShopSelectList.show()
  }

  override fun removeProductItem(rootAdapterPosition: Int, productItemPosition: Int) {
    mainShopItemHashMap[rootAdapterPosition].cart_products.removeAt(productItemPosition)
    shopProductAddItemRecyclerAdapter.notifyItemChanged(rootAdapterPosition)
  }

  override fun updateTotalProductsPricing() {
    totalChargeMain = 0
    for (shop in mainShopItemHashMap) {
      shop.cart_products.forEach {
        totalChargeMain += (it.product_item_price * it.product_item_amount)
      }
    }
    viewMain.totalChargeEdittextEditOrder.setText((totalChargeMain + bkashChargeMain).toString())
  }

  private fun roundNumberPriceTotal(d: Float): Int {
    //This  is a special round function exclusively for this  page of the app
    //not usable for general parts and other parts of   the code or apps
    return if (d > d.toInt()) {
      d.toInt() + 1
    } else {
      d.roundToInt()
    }
  }

  override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
    val time = "$hourOfDay:$minute"

    try {
      val sdf = SimpleDateFormat("H:mm")
      val dateObj = sdf.parse(time)
      System.out.println(dateObj)
      println(SimpleDateFormat("K:mm a").format(dateObj))
      viewMain.pickUpTimeTextView.text = SimpleDateFormat("K:mm a").format(dateObj).toString()
    } catch (e: ParseException) {
      e.printStackTrace()
    }
  }

  override fun onDateSet(
    view: com.wdullaer.materialdatetimepicker.date.DatePickerDialog?,
    year: Int,
    monthOfYear: Int,
    dayOfMonth: Int
  ) {
    val monthOfYear2 = monthOfYear + 1
    Log.e("time", year.toString())
    Log.e("time", monthOfYear2.toString())
    Log.e("time", dayOfMonth.toString())
    val now = Calendar.getInstance()
    val time =
      "$year/$monthOfYear2/$dayOfMonth-${now.get(Calendar.HOUR_OF_DAY)}/${now.get(Calendar.MINUTE)}"
    try {
      val sdf = SimpleDateFormat("yyyy/MM/dd-hh/mm")
      val dateObj = sdf.parse(time)
      System.out.println(dateObj)
      Log.e("time", dateObj.time.toString())
      dateLong = dateObj.time
      viewMain.pickUpDateTextView.text = "$dayOfMonth-$monthOfYear2-$year"
    } catch (e: ParseException) {
      e.printStackTrace()
    }
  }
}