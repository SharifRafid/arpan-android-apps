package da.arpan.delivery.ui.order

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.baoyachi.stepview.HorizontalStepView
import com.baoyachi.stepview.bean.StepBean
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.models.CartProductEntity
import core.arpan.delivery.models.MainShopCartItem
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.models.Shop
import core.arpan.delivery.utils.*
import da.arpan.delivery.R
import da.arpan.delivery.adapters.OrderProductItemRecyclerAdapter
import da.arpan.delivery.viewModels.DAViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_alert_layout_main.view.*
import kotlinx.android.synthetic.main.dialog_restaurant_pay_pick_up.view.*
import kotlinx.android.synthetic.main.fragment_order_history_new.view.*
import kotlinx.android.synthetic.main.product_image_big_view.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class OrderHistoryFragmentNew(private val orderHistoryPage: OrderHistoryPage) : DialogFragment() {
  private val mainShopItemHashMap = ArrayList<MainShopCartItem>()
  private lateinit var productRecyclerViewAdapter: OrderProductItemRecyclerAdapter
  private lateinit var progressDialog: Dialog
  private var orderId = ""
  private var customerId = ""
  private var orderItemMain: OrderItemMain? = null
  lateinit var contextMain: Context
  private val daViewModel: DAViewModel by viewModels()

  override fun onAttach(activity: Activity) {
    // TODO Auto-generated method stub
    super.onAttach(activity)
    contextMain = activity
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_order_history_new, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(
      DialogFragment.STYLE_NORMAL,
      R.style.Theme_ArpanDA
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    initVars(view)
  }

  private fun initVars(view: View) {
    progressDialog = contextMain.createProgressDialog()
    productRecyclerViewAdapter = OrderProductItemRecyclerAdapter(
      contextMain,
      mainShopItemHashMap,
      true
    )
    view.title_text_view.setOnClickListener {
      orderHistoryPage.closeDialog(this)
    }
    orderId = arguments?.getString("orderID").toString()
    customerId = arguments?.getString("customerId").toString()
    fetchOrderData(view)
    view.swipeRefreshMain.setOnRefreshListener {
      view.swipeRefreshMain.isRefreshing = false
      fetchOrderData(view)
    }
  }

  private fun fetchOrderData(view: View) {
    progressDialog.show()
    LiveDataUtil.observeOnce(daViewModel.getOrderById(orderId)) {
      progressDialog.dismiss()
      if (it.error == true) {
        view.orderHistoryProgressBarContainer.visibility = View.GONE
        view.noDataFoundLinearLayoutContainer.visibility = View.VISIBLE
        view.mainOrderDetailsDataContainerLinearLayout.visibility = View.GONE
      } else {
        view.orderHistoryProgressBarContainer.visibility = View.GONE
        view.noDataFoundLinearLayoutContainer.visibility = View.GONE
        view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
        workWithTheDocumentData(view, it)
      }
    }
  }

  private fun workWithTheDocumentData(view: View, orderItem: OrderItemMain) {
    // Here we have the snapshot of the "OrderItemMain" and can freely work with it
    // null check and empty check is already done in the previous stage
    orderItemMain = orderItem// Parsing to model class

    view.orderHistoryProgressBarContainer.visibility = View.GONE
    view.noDataFoundLinearLayoutContainer.visibility = View.GONE
    view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE

    initDefaultViewStates(view)
    setTextOnTextViewsOnMainUi(view, orderItemMain!!)
    setLogicForOrderStatusOnThirdRow(view, orderItemMain!!)
    setTextOnRestOrderDetailsTextView(view, orderItemMain!!)
    setLogicForPlacingOrderItemDetailsAndSeparatingProducts(view, orderItemMain!!)
    setLogicForCardsAndCountsOnStatistics(view, orderItemMain!!)
    setProductDateAndPickUpTimeOnTextViews(view, orderItemMain!!)

    view.orderHistoryProgressBarContainer.visibility = View.GONE
    view.noDataFoundLinearLayoutContainer.visibility = View.GONE
    view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
  }

  private fun setProductDateAndPickUpTimeOnTextViews(view: View, orderItemMain: OrderItemMain) {
    when (getDate(orderItemMain.orderPlacingTimeStamp, "dd-MM-yyyy")) {
      getDate(System.currentTimeMillis(), "dd-MM-yyyy") -> {
        view.orderDateTextView.text = "Today"
      }
      getDate(
        System.currentTimeMillis() - (24 * 3600 * 1000),
        "dd-MM-yyyy"
      ) -> {
        view.orderDateTextView.text = "Yesterday"
      }
      else -> {
        view.orderDateTextView.text = getDate(orderItemMain.orderPlacingTimeStamp, "dd-MM-yyyy")
      }
    }
    if (orderItemMain.pickUpTime.isNullOrEmpty()) {
      view.pickUpTimeTextView.text = "Now"
    } else {
      view.pickUpTimeTextView.text = orderItemMain.pickUpTime
    }
  }

  private fun setLogicForCardsAndCountsOnStatistics(view: View, orderItemMain: OrderItemMain) {
    view.orderTotalPrice.text = (orderItemMain.totalPrice + orderItemMain.deliveryCharge).toString()
    view.orderDeliveryPrice.text = orderItemMain.deliveryCharge.toString()
    view.orderDaPrice.text = orderItemMain.daCharge.toString()
    var arpanProfit = orderItemMain.deliveryCharge - orderItemMain.daCharge
    var totalProductItemsPrice = 0
    for (productItem in orderItemMain.products) {
      totalProductItemsPrice += (productItem.product_item_price * productItem.product_item_amount) -
              (productItem.product_arpan_profit * productItem.product_item_amount)
      arpanProfit += (productItem.product_arpan_profit * productItem.product_item_amount)
    }
    view.orderArpanChargePrice.text = arpanProfit.toString()
    view.orderTotalPrice.text = (orderItemMain.totalPrice + orderItemMain.deliveryCharge).toString()
    view.pickPriceRestaurantText.text = totalProductItemsPrice.toString()
  }

  private fun initDefaultViewStates(view: View) {
    view.orderImagePicture.visibility = View.GONE
    view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    view.orderTitleEdittextContainerLayout.visibility = View.GONE
    view.orderNoteEdittextContainerLayout.visibility = View.GONE
    view.orderItemsMainRecyclerView.visibility = View.GONE
    view.appliedPromoCodeButton.visibility = View.GONE
    view.cancelOrderButton.visibility = View.GONE
    view.orderAdminNoteEdittextContainerLayout.visibility = View.GONE
    view.acceptOrderButton.visibility = View.GONE
    view.paymentRequestSentToCustomerTextView.visibility = View.GONE
    view.linearLayoutCancelReasonContainer.visibility = View.GONE
    view.recieverMobileNumberContainer.visibility = View.GONE
  }

  private fun setLogicForPlacingOrderItemDetailsAndSeparatingProducts(
    view: View,
    orderItemMain: OrderItemMain
  ) {
    if (orderItemMain.pickDropOrder) {
      initPickDropDataLogicAndPlaceDataOnUi(view, orderItemMain)
      view.orderProductsDetailTextView.text = "পিক-আপ ড্রপ অর্ডার"
    } else {
      if (orderItemMain.products.size == 1 && !orderItemMain.products[0].product_item) {
        when {
          orderItemMain.products[0].parcel_item -> {
            view.orderProductsDetailTextView.text = "পার্সেল অর্ডার"
            placeParcelItemData(view, orderItemMain)
          }
          orderItemMain.products[0].medicine_item -> {
            view.orderProductsDetailTextView.text = "পার্সেল অর্ডার"
            placeMedicineItemData(view, orderItemMain)
          }
          orderItemMain.products[0].custom_order_item -> {
            if (orderItemMain.adminOrder) {
              view.orderProductsDetailTextView.text = "অ্যাডমিন অর্ডার"
            } else {
              view.orderProductsDetailTextView.text = "কাস্টম অর্ডার"
            }
            placeCustomOrderItemData(view, orderItemMain)
          }
        }
      } else {
        view.orderProductsDetailTextView.text = "শপ অর্ডার"
        workWithTheArrayList(orderItemMain.products, view)
      }
    }
  }

  private fun placeCustomOrderItemData(view: View, orderItemMain: OrderItemMain) {
    view.orderTitleEdittextContainerLayout.visibility = View.GONE
    view.orderItemsMainRecyclerView.visibility = View.GONE

    if (orderItemMain.products[0].custom_order_text.trim().isNotEmpty()) {
      view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
      view.orderDetailsEdittextContainerLayout.hint = "ডিটেইলস"
      view.orderDetailsEdittext.setText(orderItemMain.products[0].custom_order_text)
    } else {
      view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    }
    if (orderItemMain.products[0].custom_order_image.isNotEmpty()) {
      view.orderImagePicture.visibility = View.VISIBLE
      setOrderImageOnView(view, orderItemMain.products[0].custom_order_image)
    } else {
      view.orderImagePicture.visibility = View.GONE
    }
  }

  private fun placeMedicineItemData(view: View, orderItemMain: OrderItemMain) {
    view.orderItemsMainRecyclerView.visibility = View.GONE
    if (orderItemMain.products[0].medicine_order_text.trim().isNotEmpty()) {
      view.orderTitleEdittextContainerLayout.visibility = View.VISIBLE
      view.orderTitleEdittextContainerLayout.hint = "ফার্মেসি"
      view.orderTitleEdittext.setText(orderItemMain.products[0].medicine_order_text)
    } else {
      view.orderTitleEdittextContainerLayout.visibility = View.GONE
    }
    if (orderItemMain.products[0].medicine_order_text_2.trim().isNotEmpty()) {
      view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
      view.orderDetailsEdittextContainerLayout.hint = "ঔষধ"
      view.orderDetailsEdittext.setText(orderItemMain.products[0].medicine_order_text_2)
    } else {
      view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    }
    if (orderItemMain.products[0].medicine_order_image.isNotEmpty()) {
      view.orderImagePicture.visibility = View.VISIBLE
      setOrderImageOnView(view, orderItemMain.products[0].medicine_order_image)
    } else {
      view.orderImagePicture.visibility = View.GONE
    }
  }

  private fun placeParcelItemData(view: View, orderItemMain: OrderItemMain) {
    view.orderItemsMainRecyclerView.visibility = View.GONE

    if (orderItemMain.products[0].parcel_order_text.trim().isNotEmpty()) {
      view.orderTitleEdittextContainerLayout.visibility = View.VISIBLE
      view.orderTitleEdittextContainerLayout.hint = "কুরিয়ার নেম"
      view.orderTitleEdittext.setText(orderItemMain.products[0].parcel_order_text)
    } else {
      view.orderTitleEdittextContainerLayout.visibility = View.GONE
    }
    if (orderItemMain.products[0].parcel_order_text_2.trim().isNotEmpty()) {
      view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
      view.orderDetailsEdittextContainerLayout.hint = "ডিটেইলস"
      view.orderDetailsEdittext.setText(orderItemMain.products[0].parcel_order_text_2)
    } else {
      view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    }

    if (orderItemMain.products[0].parcel_order_image.isNotEmpty()) {
      view.orderImagePicture.visibility = View.VISIBLE
      setOrderImageOnView(view, orderItemMain.products[0].parcel_order_image)
    } else {
      view.orderImagePicture.visibility = View.GONE
    }
  }

  private fun initPickDropDataLogicAndPlaceDataOnUi(view: View, orderItemMain: OrderItemMain) {
    view.orderTitleEdittextContainerLayout.visibility = View.GONE
    view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
    view.orderItemsMainRecyclerView.visibility = View.GONE
    view.orderDetailsEdittextContainerLayout.hint = "পিক-আপ এন্ড ড্রপ"
    view.callUserNowReciverImageButton.setOnClickListener {
      callNow(orderItemMain.pickDropOrderItem!!.senderPhone)
    }
    view.userNameEditText.setText(orderItemMain.pickDropOrderItem!!.senderName)
    view.userMobileEdittext.setText(orderItemMain.pickDropOrderItem!!.senderPhone)
    view.userLocationEdittext.setText(orderItemMain.pickDropOrderItem!!.senderName)
    if (orderItemMain.pickDropOrderItem!!.senderAddress.trim().isNotEmpty()) {
      view.userAddressEdittextContainerLayout.visibility = View.VISIBLE
      view.userAddressEdittext.setText(orderItemMain.pickDropOrderItem!!.senderAddress)
    } else {
      view.userAddressEdittextContainerLayout.visibility = View.GONE
    }
    view.orderDetailsEdittext.setText(
      StringBuilder()
        .append("প্রেরকের তথ্যঃ ")
        .append("\n")
        .append("নামঃ ")
        .append(orderItemMain.pickDropOrderItem!!.senderName)
        .append("\n")
        .append("মোবাইলঃ ")
        .append(orderItemMain.pickDropOrderItem!!.senderPhone)
        .append("\n")
        .append("ঠিকানাঃ ")
        .append(orderItemMain.pickDropOrderItem!!.senderAddress)
        .append("\n")
        .append("লোকেশনঃ")
        .append(orderItemMain.pickDropOrderItem!!.senderLocation)
        .append("\n")
        .append("\n")
        .append("প্রাপকের তথ্যঃ ")
        .append("\n")
        .append("নামঃ ")
        .append(orderItemMain.pickDropOrderItem!!.recieverName)
        .append("\n")
        .append("মোবাইলঃ ")
        .append(orderItemMain.pickDropOrderItem!!.recieverPhone)
        .append("\n")
        .append("ঠিকানাঃ ")
        .append(orderItemMain.pickDropOrderItem!!.recieverAddress)
        .append("\n")
        .append("লোকেশনঃ")
        .append(orderItemMain.pickDropOrderItem!!.recieverLocation)
        .append("\n")
        .append("\n")
        .append("পার্সেলের তথ্যঃ ")
        .append("\n")
        .append(orderItemMain.pickDropOrderItem!!.parcelDetails)
    )

    if (orderItemMain.pickDropOrderItem != null) {
      if (orderItemMain.pickDropOrderItem!!.recieverPhone.isNotEmpty()) {
        view.recieverMobileNumberContainer.visibility = View.VISIBLE
        view.userReciverMobileEdittext.setText(orderItemMain.pickDropOrderItem!!.recieverPhone)
        view.callUserNowReciverImageButton.setOnClickListener {
          callNow(orderItemMain.pickDropOrderItem!!.recieverPhone)
        }
      } else {
        view.recieverMobileNumberContainer.visibility = View.GONE
      }
      if (orderItemMain.pickDropOrderItem!!.parcelImage.isNotEmpty()) {
        view.orderImagePicture.visibility = View.VISIBLE
        setOrderImageOnView(view, orderItemMain.pickDropOrderItem!!.parcelImage)
      } else {
        view.orderImagePicture.visibility = View.GONE
      }
    }
  }

  private fun callNow(recieverPhone: String) {
    if (callPermissionCheck(contextMain, contextMain as Activity)) {
      val callIntent = Intent(
        Intent.ACTION_CALL,
        Uri.parse("tel:" + recieverPhone)
      )
      startActivity(callIntent)
    }
  }

  private fun setOrderImageOnView(view: View, path: String) {
    Glide.with(requireActivity())
      .load(Constants.SERVER_FILES_BASE_URL + path)
      .diskCacheStrategy(DiskCacheStrategy.ALL)
      .centerInside()
      .placeholder(R.drawable.loading_image_glide)
      .into(view.orderImagePicture)

    view.orderImagePicture.setOnClickListener {
      val dialog = AlertDialog.Builder(contextMain, R.style.Theme_ArpanDA).create()
      val view2 = LayoutInflater.from(contextMain).inflate(R.layout.product_image_big_view, null)
      view2.floatingActionButton.setOnClickListener {
        dialog.dismiss()
      }
      Glide.with(contextMain)
        .load(Constants.SERVER_FILES_BASE_URL + path)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerInside()
        .into(view2.imageView)
      dialog.setView(view2)
      //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      dialog.show()
    }
  }

  private fun workWithTheArrayList(products: List<CartProductEntity>, view: View) {
    view.orderTitleEdittextContainerLayout.visibility = View.GONE
    view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    view.orderItemsMainRecyclerView.visibility = View.VISIBLE

    mainShopItemHashMap.clear()
    for (cartItemEntity in products) {
      val filteredArray =
        mainShopItemHashMap.filter { it -> it.shop_doc_id == cartItemEntity.product_item_shop_key }
      if (filteredArray.isEmpty()) {
        val shopItem = MainShopCartItem()
        shopItem.shop_doc_id = cartItemEntity.product_item_shop_key
        shopItem.shop_details = Shop(name = cartItemEntity.product_item_shop_name)
        shopItem.cart_products.add(cartItemEntity)
        mainShopItemHashMap.add(shopItem)
      } else {
        mainShopItemHashMap[mainShopItemHashMap.indexOf(filteredArray[0])]
          .cart_products.add(cartItemEntity)
      }
    }
    if (mainShopItemHashMap.isNotEmpty()) {
      // This has shop specific products in the order :D
      view.orderItemsMainRecyclerView.layoutManager = LinearLayoutManager(contextMain)
      view.orderItemsMainRecyclerView.adapter = productRecyclerViewAdapter
    }
  }

  private fun setTextOnRestOrderDetailsTextView(view: View, orderItemMain: OrderItemMain) {
    if (orderItemMain.promoCodeApplied) {
      view.appliedPromoCodeButton.visibility = View.VISIBLE
      view.appliedPromoCodeButton.text =
        "প্রোমোকোড অ্যাড করা হয়েছেঃ ${orderItemMain.promoCode!!.promoCodeName}"
    } else {
      view.appliedPromoCodeButton.visibility = View.GONE
    }
    if (orderItemMain.paymentMethod == "COD") {
      view.pricePaymentStatusMain.text = "COD"
    } else {
      view.pricePaymentStatusMain.text = "bKash"
    }
    view.totalPriceTextViewMain.text =
      "Total: ${orderItemMain.totalPrice}+${orderItemMain.deliveryCharge} = ${orderItemMain.totalPrice + orderItemMain.deliveryCharge}"
  }

  private fun setLogicForOrderStatusOnThirdRow(view: View, orderItemMain: OrderItemMain) {
    view.step_view_order_progress.visibility = View.VISIBLE
    view.orderStatusTopButton.text = orderItemMain.orderStatus
    view.cancelOrderButton.visibility = View.GONE
    when (orderItemMain.orderStatus) {
      "PENDING" -> {
        val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
        val stepsBeanList: MutableList<StepBean> = ArrayList()
        val stepBean4 = StepBean("     PENDING     ", 1)
        val stepBean0 = StepBean("     VERIFIED     ", -1)
        val stepBean1 = StepBean("     PROCESSING     ", -1)
        val stepBean2 = StepBean("     PICKED UP     ", -1)
        val stepBean3 = StepBean("     COMPLETED     ", -1)
        if (orderItemMain.orderPlacingTimeStamp != 0L) {
          stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
        }
        stepsBeanList.add(stepBean4)
        stepsBeanList.add(stepBean0)
        stepsBeanList.add(stepBean1)
        stepsBeanList.add(stepBean2)
        stepsBeanList.add(stepBean3)
        setStepView(view, setpview5, stepsBeanList)
        view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#FA831B"))
      }
      "VERIFIED" -> {
        val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
        val stepsBeanList: MutableList<StepBean> = ArrayList()
        val stepBean4 = StepBean("     PENDING     ", 1)
        val stepBean0 = StepBean("     VERIFIED     ", 1)
        val stepBean1 = StepBean("     PROCESSING     ", -1)
        val stepBean2 = StepBean("     PICKED UP     ", -1)
        val stepBean3 = StepBean("     COMPLETED     ", -1)
        if (orderItemMain.verifiedTimeStampMillis != 0L) {
          stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.orderPlacingTimeStamp != 0L) {
          stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
        }
        stepsBeanList.add(stepBean4)
        stepsBeanList.add(stepBean0)
        stepsBeanList.add(stepBean1)
        stepsBeanList.add(stepBean2)
        stepsBeanList.add(stepBean3)
        setStepView(view, setpview5, stepsBeanList)
        view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#ED9D34"))

        view.cancelOrderButton.visibility = View.VISIBLE
        view.cancelOrderButton.text = "Reject Order"
        view.cancelOrderButton.setOnClickListener {
          // CANCEL ORDER FOR DA - TOTALLY A SEPARATE OPERATION
          cancelOrderItem(view, orderItemMain)
        }

        view.acceptOrderButton.text = "Accept Order"
        view.acceptOrderButton.visibility = View.VISIBLE
        view.acceptOrderButton.setOnClickListener {
          // ACCEPT ORDER FOR DA - TOTALLY A SEPARATE OPERATION
          acceptOrderItem(view, orderItemMain)
        }
      }
      "PROCESSING" -> {
        val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
        val stepsBeanList: MutableList<StepBean> = ArrayList()
        val stepBean4 = StepBean("     PENDING     ", 1)
        val stepBean0 = StepBean("     VERIFIED     ", 1)
        val stepBean1 = StepBean("     PROCESSING     ", 1)
        val stepBean2 = StepBean("     PICKED UP     ", -1)
        val stepBean3 = StepBean("     COMPLETED     ", -1)
        if (orderItemMain.processingTimeStampMillis != 0L) {
          stepBean1.name =
            "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.verifiedTimeStampMillis != 0L) {
          stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.orderPlacingTimeStamp != 0L) {
          stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
        }
        stepsBeanList.add(stepBean4)
        stepsBeanList.add(stepBean0)
        stepsBeanList.add(stepBean1)
        stepsBeanList.add(stepBean2)
        stepsBeanList.add(stepBean3)

        setStepView(view, setpview5, stepsBeanList)
        view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#ED9D34"))

        view.cancelOrderButton.visibility = View.GONE

        view.acceptOrderButton.text = "Pick Up Order"
        view.acceptOrderButton.visibility = View.VISIBLE
        view.acceptOrderButton.setOnClickListener {
          // ACCEPT ORDER FOR DA - TOTALLY A SEPARATE OPERATION
          pickUpOrderItem(view, orderItemMain)
        }
      }
      "PICKED UP" -> {
        view.step_view_order_progress.visibility = View.VISIBLE
        val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
        val stepsBeanList: MutableList<StepBean> = ArrayList()
        val stepBean4 = StepBean("     PENDING     ", 1)
        val stepBean0 = StepBean("     VERIFIED     ", 1)
        val stepBean1 = StepBean("     PROCESSING     ", 1)
        val stepBean2 = StepBean("     PICKED UP     ", 1)
        val stepBean3 = StepBean("     COMPLETED     ", -1)
        if (orderItemMain.processingTimeStampMillis != 0L) {
          stepBean1.name =
            "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.verifiedTimeStampMillis != 0L) {
          stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.orderPlacingTimeStamp != 0L) {
          stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
        }
        if (orderItemMain.pickedUpTimeStampMillis != 0L) {
          stepBean2.name = "PICKED UP\n(${getDate(orderItemMain.pickedUpTimeStampMillis, "hh:mm")})"
        }
        stepsBeanList.add(stepBean4)
        stepsBeanList.add(stepBean0)
        stepsBeanList.add(stepBean1)
        stepsBeanList.add(stepBean2)
        stepsBeanList.add(stepBean3)

        setStepView(view, setpview5, stepsBeanList)
        view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#ED9D34"))

        view.cancelOrderButton.visibility = View.GONE

        if (orderItemMain.paymentMethod == "COD") {
          view.acceptOrderButton.text = "Complete Order"
          view.acceptOrderButton.visibility = View.VISIBLE
          view.acceptOrderButton.setOnClickListener {
            // ACCEPT ORDER FOR DA - TOTALLY A SEPARATE OPERATION
            completeOrderDa(view, orderItemMain)
          }
        } else {
          if (orderItemMain.paymentRequested) {
            view.acceptOrderButton.text = "Complete Order"
            view.acceptOrderButton.visibility = View.VISIBLE
            view.acceptOrderButton.setOnClickListener {
              // ACCEPT ORDER FOR DA - TOTALLY A SEPARATE OPERATION
              view.paymentRequestSentToCustomerTextView.visibility = View.VISIBLE
              completeOrderDa(view, orderItemMain)
            }
          } else {
            view.acceptOrderButton.text = "Request Payment"
            view.acceptOrderButton.visibility = View.VISIBLE
            view.acceptOrderButton.setOnClickListener {
              // ACCEPT ORDER FOR DA - TOTALLY A SEPARATE OPERATION
              view.paymentRequestSentToCustomerTextView.visibility = View.GONE
              requestPaymentFromCustomer(view, orderItemMain)
            }
          }
        }
      }
      "COMPLETED" -> {
        if (orderItemMain.orderCompletedStatus == "CANCELLED") {
          view.step_view_order_progress.visibility = View.GONE
          view.orderStatusTopButton.text = "CANCELLED"
          if (orderItemMain.cancelledOrderReasonFromAdmin!!.trim().isNotEmpty()) {
            view.linearLayoutCancelReasonContainer.visibility = View.VISIBLE
            view.orderCancellationReasonDetails.text = orderItemMain.cancelledOrderReasonFromAdmin
          } else {
            view.linearLayoutCancelReasonContainer.visibility = View.GONE
          }
          view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#EA594D"))
        } else {
          val setpview5 =
            view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
          val stepsBeanList: MutableList<StepBean> = ArrayList()
          val stepBean4 = StepBean("     PENDING     ", 1)
          val stepBean0 = StepBean("     VERIFIED     ", 1)
          val stepBean1 = StepBean("     PROCESSING     ", 1)
          val stepBean2 = StepBean("     PICKED UP     ", 1)
          val stepBean3 = StepBean("     COMPLETED     ", 1)
          if (orderItemMain.processingTimeStampMillis != 0L) {
            stepBean1.name =
              "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis, "hh:mm")})"
          }
          if (orderItemMain.verifiedTimeStampMillis != 0L) {
            stepBean0.name =
              "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis, "hh:mm")})"
          }
          if (orderItemMain.orderPlacingTimeStamp != 0L) {
            stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
          }
          if (orderItemMain.pickedUpTimeStampMillis != 0L) {
            stepBean2.name =
              "PICKED UP\n(${getDate(orderItemMain.pickedUpTimeStampMillis, "hh:mm")})"
          }
          if (orderItemMain.completedTimeStampMillis != 0L) {
            stepBean3.name =
              "COMPLETED\n(${getDate(orderItemMain.completedTimeStampMillis, "hh:mm")})"
          }
          stepsBeanList.add(stepBean4)
          stepsBeanList.add(stepBean0)
          stepsBeanList.add(stepBean1)
          stepsBeanList.add(stepBean2)
          stepsBeanList.add(stepBean3)
          setStepView(view, setpview5, stepsBeanList)
          view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#43A047"))
        }
      }
    }
  }

  private fun requestPaymentFromCustomer(view: View, orderItemMain: OrderItemMain) {
    val dialog2 = contextMain.createProgressDialog()
    val view = LayoutInflater.from(contextMain)
      .inflate(R.layout.dialog_alert_layout_main, null)
    val dialog = AlertDialog.Builder(contextMain)
      .setView(view).create()
    dialog.setCancelable(false)
    view.btnNoDialogAlertMain.text = "No"
    view.btnYesDialogAlertMain.text = "Yes"
    view.titleTextView.text = "Did you deliver the order?"
    view.messageTextView.text = "Are you sure to ask the customer for payment ?"
    view.btnNoDialogAlertMain.setOnClickListener {
      dialog.dismiss()
    }
    view.btnYesDialogAlertMain.setOnClickListener {
      dialog.dismiss()
      dialog2.show()
      LiveDataUtil.observeOnce(daViewModel.requestPayment(orderId, true)) {
        if (it.error == true) {
          contextMain.showToast(it.message.toString(), FancyToast.ERROR)
        } else {
          contextMain.showToast("Success", FancyToast.SUCCESS)
          fetchOrderData(view)
        }
        dialog2.dismiss()
      }
    }
    dialog.show()
  }

  private fun completeOrderDa(view: View, orderItemMain: OrderItemMain) {
    if (orderItemMain.products.size == 1 && !orderItemMain.products.any { it.product_item }) {
      val dialogForRealPickUpItemsPrice = AlertDialog.Builder(contextMain).create()
      dialogForRealPickUpItemsPrice.window!!.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
      val dialogForRealPickUpItemsPriceView =
        LayoutInflater.from(contextMain).inflate(R.layout.dialog_restaurant_pay_pick_up, null)
      dialogForRealPickUpItemsPriceView.recyclerViewProducts.visibility = View.GONE
      dialogForRealPickUpItemsPriceView.totalPriceTextView.text =
        "Total: ${orderItemMain.totalPrice + orderItemMain.deliveryCharge}৳"
      dialogForRealPickUpItemsPriceView.buttonPickUp.setOnClickListener {
        dialogForRealPickUpItemsPrice.dismiss()
        progressDialog.show()
        LiveDataUtil.observeOnce(daViewModel.completeOrder(orderId, true)) {
          progressDialog.dismiss()
          if (it.error == true) {
            contextMain.showToast(it.message.toString(), FancyToast.ERROR)
          } else {
            contextMain.showToast("Order Completed", FancyToast.SUCCESS)
            orderHistoryPage.closeDialog(this)
          }
        }
      }
      dialogForRealPickUpItemsPriceView.buttonPickUp.text = "Complete"
      dialogForRealPickUpItemsPriceView.recyclerViewProducts.layoutManager =
        LinearLayoutManager(contextMain)
      dialogForRealPickUpItemsPriceView.recyclerViewProducts.adapter =
        OrderProductItemRecyclerAdapter(contextMain, mainShopItemHashMap, false)
      dialogForRealPickUpItemsPriceView.titleTextViewForPickUpShopDetailsDialog.text =
        "Amount to be collected from Customer"
      dialogForRealPickUpItemsPrice.setView(dialogForRealPickUpItemsPriceView)
      dialogForRealPickUpItemsPrice.window!!.setBackgroundDrawableResource(android.R.color.transparent)
      dialogForRealPickUpItemsPrice.show()
    } else {
      val dialogForRealPickUpItemsPriceView = LayoutInflater.from(contextMain)
        .inflate(R.layout.dialog_restaurant_pay_pick_up, null)
      val dialogForRealPickUpItemsPrice =
        AlertDialog.Builder(contextMain).create()
      dialogForRealPickUpItemsPrice.window!!.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
      dialogForRealPickUpItemsPriceView.recyclerViewProducts.visibility = View.VISIBLE
      dialogForRealPickUpItemsPriceView.totalPriceTextView.text = "Total: ${orderItemMain.totalPrice} + ${orderItemMain.deliveryCharge} = ${orderItemMain.totalPrice + orderItemMain.deliveryCharge}৳"
      dialogForRealPickUpItemsPriceView.buttonPickUp.text = "Complete"
      dialogForRealPickUpItemsPriceView.buttonPickUp.setOnClickListener {
        dialogForRealPickUpItemsPrice.dismiss()
        progressDialog.show()
        LiveDataUtil.observeOnce(daViewModel.completeOrder(orderId, true)) {
          progressDialog.dismiss()
          if (it.error == true) {
            contextMain.showToast(it.message.toString(), FancyToast.ERROR)
          } else {
            contextMain.showToast("Order Completed", FancyToast.SUCCESS)
            orderHistoryPage.closeDialog(this)
          }
        }
      }
      dialogForRealPickUpItemsPriceView.recyclerViewProducts.layoutManager =
        LinearLayoutManager(contextMain)
      dialogForRealPickUpItemsPriceView.recyclerViewProducts.adapter =
        OrderProductItemRecyclerAdapter(contextMain, mainShopItemHashMap, false)
      dialogForRealPickUpItemsPriceView.titleTextViewForPickUpShopDetailsDialog.text =
        "Amount to be collected from Customer"
      dialogForRealPickUpItemsPrice.setView(dialogForRealPickUpItemsPriceView)
      dialogForRealPickUpItemsPrice.window!!.setBackgroundDrawableResource(android.R.color.transparent)
      dialogForRealPickUpItemsPrice.show()
    }
  }

  private fun pickUpOrderItem(view: View, orderItemMain: OrderItemMain) {
    Log.e("TEST","0")
    if (orderItemMain.products.size == 1 && !orderItemMain.products[0].product_item) {
      Log.e("TEST","1")
      val dialogView = LayoutInflater.from(contextMain)
        .inflate(R.layout.dialog_alert_layout_main, null)
      val dialog = AlertDialog.Builder(contextMain).create()
      dialog.setCancelable(false)
      dialogView.btnNoDialogAlertMain.text = "No"
      dialogView.btnYesDialogAlertMain.text = "Yes"
      dialogView.titleTextView.text = "Do you want to pick the order?"
      dialogView.messageTextView.text = "Have you collected all the items ?"
      dialogView.btnNoDialogAlertMain.setOnClickListener {
        dialog.dismiss()
      }
      dialogView.btnYesDialogAlertMain.setOnClickListener {
        dialog.dismiss()
        LiveDataUtil.observeOnce(daViewModel.pickUpOrder(orderId, true)) {
          progressDialog.dismiss()
          if (it.error == true) {
            contextMain.showToast(it.message.toString(), FancyToast.ERROR)
          } else {
            contextMain.showToast("Order Picked", FancyToast.SUCCESS)
            fetchOrderData(view)
          }
        }
      }
      dialog.setView(dialogView)
      dialog.show()
    } else {
      val dialogForRealPickUpItemsPrice = AlertDialog.Builder(contextMain).create()
      dialogForRealPickUpItemsPrice.window!!.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
      val dialogForRealPickUpItemsPriceView =
        LayoutInflater.from(contextMain).inflate(R.layout.dialog_restaurant_pay_pick_up, null)
      if (orderItemMain.products.size == 1 && !orderItemMain.products.any { it.product_item }) {
        dialogForRealPickUpItemsPriceView.recyclerViewProducts.visibility = View.GONE
        dialogForRealPickUpItemsPriceView.totalPriceTextView.text = "${orderItemMain.totalPrice}৳"
      } else {
        dialogForRealPickUpItemsPriceView.recyclerViewProducts.visibility = View.VISIBLE
        var totalPriceAmountNew = 0
        for (item in mainShopItemHashMap) {
          for (cartProduct in item.cart_products) {
            totalPriceAmountNew += ((cartProduct.product_item_price-cartProduct.product_arpan_profit) * cartProduct.product_item_amount)
          }
        }
        dialogForRealPickUpItemsPriceView.totalPriceTextView.text = "Total: ${totalPriceAmountNew}৳"
      }
      dialogForRealPickUpItemsPriceView.buttonPickUp.setOnClickListener {
        dialogForRealPickUpItemsPrice.dismiss()
        LiveDataUtil.observeOnce(daViewModel.pickUpOrder(orderId, true)) {
          progressDialog.dismiss()
          if (it.error == true) {
            contextMain.showToast(it.message.toString(), FancyToast.ERROR)
          } else {
            contextMain.showToast("Order Picked", FancyToast.SUCCESS)
            fetchOrderData(view)
          }
        }
      }
      dialogForRealPickUpItemsPriceView.recyclerViewProducts.layoutManager =
        LinearLayoutManager(contextMain)
      dialogForRealPickUpItemsPriceView.recyclerViewProducts.adapter =
        OrderProductItemRecyclerAdapter(contextMain, mainShopItemHashMap, true)
      dialogForRealPickUpItemsPrice.setView(dialogForRealPickUpItemsPriceView)
      dialogForRealPickUpItemsPrice.window!!.setBackgroundDrawableResource(android.R.color.transparent)
      dialogForRealPickUpItemsPrice.show()
    }
  }

  private fun acceptOrderItem(view: View, orderItemMain: OrderItemMain) {
    // ACCEPT ORDER FOR DA - TOTALLY A SEPARATE OPERATION
    val dialog2 = contextMain.createProgressDialog()
    val view = LayoutInflater.from(contextMain)
      .inflate(R.layout.dialog_alert_layout_main, null)
    val dialog = AlertDialog.Builder(contextMain)
      .setView(view).create()
    dialog.setCancelable(false)
    view.btnNoDialogAlertMain.text = "No"
    view.btnYesDialogAlertMain.text = "Yes"
    view.titleTextView.text = "Do you want to accept the order?"
    view.messageTextView.text = "You're about to accept this order , are you sure ?"
    view.btnNoDialogAlertMain.setOnClickListener {
      dialog.dismiss()
    }
    view.btnYesDialogAlertMain.setOnClickListener {
      dialog2.show()
      LiveDataUtil.observeOnce(
        daViewModel.acceptOrder(
          orderId,
          true
        )
      ) {
        if (it.error != true) {
          fetchOrderData(view)
          contextMain.showToast("Sucess", FancyToast.SUCCESS)
        } else {
          contextMain.showToast(it.message.toString(), FancyToast.ERROR)
        }
        dialog2.dismiss()
      }
      dialog.dismiss()
    }
    dialog.show()
  }

  private fun setStepView(
    view: View,
    setpview5: HorizontalStepView,
    stepsBeanList: MutableList<StepBean>
  ) {
    setpview5.setStepViewTexts(stepsBeanList) //总步骤
      .setTextSize(10) //set textSize
      .setStepsViewIndicatorCompletedLineColor(
        ContextCompat.getColor(
          contextMain,
          R.color.colorPrimary
        )
      ) //设置StepsViewIndicator完成线的颜色
      .setStepsViewIndicatorUnCompletedLineColor(
        ContextCompat.getColor(
          contextMain,
          R.color.colorPrimary
        )
      ) //设置StepsViewIndicator未完成线的颜色
      .setStepViewComplectedTextColor(
        ContextCompat.getColor(
          contextMain!!,
          R.color.grey_normal
        )
      ) //设置StepsView text完成线的颜色
      .setStepViewUnComplectedTextColor(
        ContextCompat.getColor(
          contextMain,
          R.color.grey_normal
        )
      ) //设置StepsView text未完成线的颜色
      .setStepsViewIndicatorCompleteIcon(
        ContextCompat.getDrawable(
          contextMain,
          R.drawable.ic_baseline_checked
        )
      ) //设置StepsViewIndicator CompleteIcon
      .setStepsViewIndicatorDefaultIcon(
        ContextCompat.getDrawable(
          contextMain,
          R.drawable.unchecked_bg_stroked
        )
      ) //设置StepsViewIndicator DefaultIcon
      .setStepsViewIndicatorAttentionIcon(
        ContextCompat.getDrawable(
          contextMain,
          R.drawable.ic_arpan_icon_v2
        )
      ) //设置StepsViewIndicator AttentionIcon
  }

  fun getDate(milliSeconds: Long, dateFormat: String?): String? {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar: Calendar = Calendar.getInstance()
    calendar.setTimeInMillis(milliSeconds)
    return formatter.format(calendar.getTime())
  }

  private fun cancelOrderItem(view: View, orderItemMain: OrderItemMain) {
    // CANCEL ORDER FOR DA - TOTALLY A SEPARATE OPERATION
    val dialog2 = contextMain.createProgressDialog()
    val view = LayoutInflater.from(contextMain)
      .inflate(R.layout.dialog_alert_layout_main, null)
    val dialog3 = AlertDialog.Builder(contextMain)
      .setView(view).create()
    dialog3.setCancelable(false)
    view.btnNoDialogAlertMain.text = "No"
    view.btnYesDialogAlertMain.text = "Yes"
    view.titleTextView.text = "Do you want to reject the order?"
    view.messageTextView.text = "You're about to cancel this order , are you sure ?"
    view.btnNoDialogAlertMain.setOnClickListener {
      dialog3.dismiss()
    }
    view.btnYesDialogAlertMain.setOnClickListener {
      dialog2.show()
      LiveDataUtil.observeOnce(
        daViewModel.acceptOrder(
          orderId,
          false
        )
      ) {
        if (it.error != true) {
          contextMain.showToast("Sucess", FancyToast.SUCCESS)
          dismiss()
        } else {
          contextMain.showToast(it.message.toString(), FancyToast.ERROR)
        }
        dialog2.dismiss()
      }
      dialog3.dismiss()
    }
    dialog3.show()
  }

  private fun setTextOnTextViewsOnMainUi(view: View, orderItemMain: OrderItemMain) {
    view.orderIdTextView.text = "Order# " + orderItemMain.orderId

    view.userNameEditText.setText(orderItemMain.userName)

    view.userMobileEdittext.setText(orderItemMain.userNumber)

    view.callUserNowImageButton.setOnClickListener {
      callNow(orderItemMain.userNumber!!)
    }

    if (orderItemMain.locationItem!!.locationName!!.trim() == "মাগুরা সদর") {
      if (orderItemMain.userAddress!!.trim().isNotEmpty()) {
        view.userAddressEdittextContainerLayout.visibility = View.VISIBLE
        view.userAddressEdittext.setText(orderItemMain.userAddress)
      } else {
        view.userAddressEdittextContainerLayout.visibility = View.GONE
      }
    } else {
      view.userAddressEdittextContainerLayout.visibility = View.GONE
    }

    view.userLocationEdittext.setText(orderItemMain.locationItem!!.locationName)

    view.userNameEditText.setText(orderItemMain.userName)

    if (!orderItemMain.userNote.isNullOrEmpty()) {
      view.orderNoteEdittextContainerLayout.visibility = View.VISIBLE
      view.orderNoteEdittext.setText(orderItemMain.userNote)
    } else {
      view.orderNoteEdittextContainerLayout.visibility = View.GONE
    }
    if (!orderItemMain.adminOrderNote.isNullOrEmpty()) {
      view.orderAdminNoteEdittextContainerLayout.visibility = View.VISIBLE
      view.orderAdminNoteEdittext.setText(orderItemMain.adminOrderNote)
    } else {
      view.orderAdminNoteEdittextContainerLayout.visibility = View.GONE
    }
  }
}

interface OrderHistoryPage{
  fun closeDialog(instance : DialogFragment)
}