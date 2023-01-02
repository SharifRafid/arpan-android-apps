package da.arpan.delivery.adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.createProgressDialog
import core.arpan.delivery.utils.orderNumberToString
import core.arpan.delivery.utils.showToast
import da.arpan.delivery.R
import da.arpan.delivery.ui.home.HomeActivity
import da.arpan.delivery.ui.order.OrderHistoryFragmentNew
import da.arpan.delivery.viewModels.DAViewModel
import kotlinx.android.synthetic.main.dialog_alert_layout_main.view.*
import kotlinx.android.synthetic.main.old_orders_list_view.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderOldSubItemRecyclerAdapter(
  private val context: Context,
  private val productItems: ArrayList<OrderItemMain>,
  private val mainItemPositions: Int,
  private val daViewModel: DAViewModel
) : RecyclerView.Adapter
<OrderOldSubItemRecyclerAdapter.RecyclerViewHolder>() {

  private lateinit var dialog: Dialog

  class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val orderIdTextView = itemView.textView as TextView
    val timeTextView = itemView.time as TextView
    val statusTextView = itemView.status as TextView
    val cancelAcceptButtonLinear = itemView.cancelAcceptButtonLinear as LinearLayout
    val buttonCancel = itemView.buttonCancel as Button
    val buttonAccept = itemView.buttonAccept as Button
    val cardView = itemView.cardView as CardView
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
    val view = LayoutInflater.from(context).inflate(
      R.layout.old_orders_list_view, parent,
      false
    )
    return RecyclerViewHolder(view)
  }

  override fun getItemCount(): Int {
    return productItems.size
  }

  override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
    holder.orderIdTextView.text = orderNumberToString(productItems[position].orderId.toString())
    holder.timeTextView.text = getDate(productItems[position].orderPlacingTimeStamp, "hh:mm a")
    when (productItems[position].orderStatus) {
      "PLACED" -> holder.statusTextView.setBackgroundColor(Color.parseColor("#262626"))
      "VERIFIED" -> holder.statusTextView.setBackgroundColor(Color.parseColor("#FA831B"))
      "PROCESSING" -> holder.statusTextView.setBackgroundColor(Color.parseColor("#ED9D34"))
      "PICKED UP" -> holder.statusTextView.setBackgroundColor(Color.parseColor("#ED9D34"))
      "COMPLETED" -> holder.statusTextView.setBackgroundColor(Color.parseColor("#43A047"))
      "CANCELLED" -> holder.statusTextView.setBackgroundColor(Color.parseColor("#EA594D"))
    }
    if (productItems[position].orderStatus == "VERIFIED") {
      holder.cancelAcceptButtonLinear.visibility = View.VISIBLE
      holder.buttonAccept.setOnClickListener {
        dialog = context.createProgressDialog()
        val view = LayoutInflater.from(context)
          .inflate(R.layout.dialog_alert_layout_main, null)
        val dialog2 = AlertDialog.Builder(context)
          .setView(view).create()
        dialog2.setCancelable(false)
        view.btnNoDialogAlertMain.text = "No"
        view.btnYesDialogAlertMain.text = "Yes"
        view.titleTextView.text = "Do you want to accept the order?"
        view.messageTextView.text = "You're about to accept this order , are you sure ?"
        view.btnNoDialogAlertMain.setOnClickListener {
          dialog2.dismiss()
        }
        view.btnYesDialogAlertMain.setOnClickListener {
          dialog.show()
          LiveDataUtil.observeOnce(
            daViewModel.acceptOrder(
              productItems[position].id!!,
              true
            )
          ) {
            if (it.error != true) {
              productItems[position].orderStatus = "PROCESSING"
              notifyItemChanged(position)
            } else {
              context.showToast(it.message.toString(), FancyToast.ERROR)
            }
            dialog.dismiss()
          }
          dialog2.dismiss()
        }
        dialog2.show()
      }
      holder.buttonCancel.setOnClickListener {
        dialog = context.createProgressDialog()
        val view = LayoutInflater.from(context)
          .inflate(R.layout.dialog_alert_layout_main, null)
        val dialog2 = AlertDialog.Builder(context)
          .setView(view).create()
        dialog2.setCancelable(false)
        view.btnNoDialogAlertMain.text = "No"
        view.btnYesDialogAlertMain.text = "Yes"
        view.titleTextView.text = "Do you want to Reject the order?"
        view.messageTextView.text = "You're about to reject this order , are you sure ?"
        view.btnNoDialogAlertMain.setOnClickListener {
          dialog2.dismiss()
        }
        view.btnYesDialogAlertMain.setOnClickListener {
          dialog.show()
          LiveDataUtil.observeOnce(
            daViewModel.acceptOrder(
              productItems[position].id!!,
              false
            )
          ) {
            if (it.error != true) {
              productItems.removeAt(position)
              notifyItemRemoved(position)
              notifyItemRangeChanged(position, productItems.size)
            } else {
              context.showToast(it.message.toString(), FancyToast.ERROR)
            }
            dialog.dismiss()
          }
          dialog2.dismiss()
        }
        dialog2.show()
      }
    } else {
      holder.cancelAcceptButtonLinear.visibility = View.GONE
    }
    holder.statusTextView.text = productItems[position].orderStatus
    holder.cardView.setOnClickListener {
      (context as HomeActivity).selectedRecyclerAdapterItem = position
      (context as HomeActivity).mainItemPositionsRecyclerAdapter = mainItemPositions
      val bundle = Bundle()
      bundle.putString("orderID", productItems[position].id!!)
      bundle.putString("customerId", productItems[position].userId)
      val fg = OrderHistoryFragmentNew(context)
      fg.arguments = bundle
      fg.show((context as HomeActivity).supportFragmentManager, "")
    }
  }

  fun getDate(milliSeconds: Long, dateFormat: String?): String? {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar: Calendar = Calendar.getInstance()
    calendar.setTimeInMillis(milliSeconds)
    return formatter.format(calendar.getTime())
  }
}