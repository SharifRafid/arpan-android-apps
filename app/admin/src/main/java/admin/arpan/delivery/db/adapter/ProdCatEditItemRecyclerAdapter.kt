package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import core.arpan.delivery.models.Product
import admin.arpan.delivery.viewModels.ProductViewModel
import core.arpan.delivery.utils.Constants
import core.arpan.delivery.utils.createProgressDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import core.arpan.delivery.utils.LiveDataUtil
import kotlinx.android.synthetic.main.product_cat_edit_item_view.view.*
import kotlinx.android.synthetic.main.product_item_view.view.mainCardView
import kotlinx.android.synthetic.main.product_item_view.view.priceTextView
import kotlinx.android.synthetic.main.product_item_view.view.shopImageItem
import kotlinx.android.synthetic.main.product_item_view.view.titleTextView

class ProdCatEditItemRecyclerAdapter(
  private val context: Context,
  private val productItems: ArrayList<Product>,
  private val shopKey: String,
  private val categoryNames: ArrayList<String>,
  private val categoryKeys: ArrayList<String>,
  private val productViewModel: ProductViewModel,
) : RecyclerView.Adapter
<ProdCatEditItemRecyclerAdapter.RecyclerViewHolder>() {

  class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView = itemView.shopImageItem as ImageView
    val textView = itemView.titleTextView as TextView
    val catTextView = itemView.categoryTextView as TextView
    val price = itemView.priceTextView as TextView
    val cardView = itemView.mainCardView as CardView
    val spinner = itemView.categorySpinner as Spinner
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
    val view = LayoutInflater.from(context).inflate(
      R.layout.product_cat_edit_item_view, parent,
      false
    )
    return RecyclerViewHolder(view)
  }

  override fun getItemCount(): Int {
    return productItems.size
  }

  override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
    holder.textView.text = productItems[position].name
    holder.catTextView.text = ""
    val adapter = ArrayAdapter(
      context,
      R.layout.custom_spinner_view,
      categoryNames
    )
    adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)
    holder.spinner.adapter = adapter
    var initInd = 0
    if (!productItems[holder.adapterPosition].categories.isNullOrEmpty()) {
      initInd = categoryKeys.indexOfFirst { item -> item == productItems[holder.adapterPosition].categories!!.first().toString() }
      holder.spinner.setSelection(initInd)
    }
    holder.spinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if(p2 != initInd){
          val pd = context.createProgressDialog()
          pd.show()
          val hMap = HashMap<String, Any>()
          hMap["categories"] = arrayListOf(categoryKeys[p2])
          LiveDataUtil.observeOnce(productViewModel.updateProductItem(productItems[holder.adapterPosition].id!!, hMap)){
            if(it.id != null){
              productItems[holder.adapterPosition].categories = arrayListOf(categoryKeys[p2])
              initInd = p2
              Log.e("STATUS","UPDATE CAT SUCCESS")
            }else{
              Log.e("STATUS","UPDATE CAT FAILED")
            }
            pd.dismiss()
          }
        }
      }

      override fun onNothingSelected(p0: AdapterView<*>?) {

      }

    }
    holder.price.text = "মুল্যঃ ${productItems[position].price} ৳"
    if (productItems[position].icon != null) {
      Glide.with(context)
        .load(Constants.SERVER_FILES_BASE_URL + productItems[position].icon!!)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()
        .override(300, 300)
        .placeholder(R.drawable.test_shop_image).into(holder.imageView)
    }

  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getItemViewType(position: Int): Int {
    return position
  }
}
