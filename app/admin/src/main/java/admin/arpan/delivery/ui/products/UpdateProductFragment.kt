package admin.arpan.delivery.ui.products

import admin.arpan.delivery.R
import core.arpan.delivery.models.Product
import admin.arpan.delivery.viewModels.ProductViewModel
import admin.arpan.delivery.viewModels.UploadViewModel
import core.arpan.delivery.utils.Constants
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.showToast
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
import com.shashank.sony.fancytoastlib.FancyToast
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_add_product.endTimeOrder
import kotlinx.android.synthetic.main.activity_add_product.imagePick
import kotlinx.android.synthetic.main.activity_add_product.startTimeOrder
import kotlinx.android.synthetic.main.activity_add_product.titleTextView
import kotlinx.android.synthetic.main.activity_add_product.upload
import kotlinx.android.synthetic.main.activity_add_product.view.*
import kotlinx.android.synthetic.main.activity_update_shop.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class UpdateProductFragment(private var itemToUpdate: Product) : DialogFragment() {
  private var imagePath = Uri.parse("")
  private var shop_key = ""
  private var product_order = ""
  private var imageName = "ProductImageName"
  private var key = ""
  private val productViewModel: ProductViewModel by viewModels()
  private val uploadViewModel: UploadViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.activity_add_product, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    initVars(view)
    initOnClicks(view)
  }

  private fun initVars(view: View) {
    shop_key = itemToUpdate.shop.toString()
    product_order = itemToUpdate.order.toString()

    upload.text = "Update"

    key = itemToUpdate.id!!

    Log.e("KEY", key)

    productTitle.setText(itemToUpdate.name)
    productDesc.setText(itemToUpdate.shortDescription)
    price.setText(itemToUpdate.price.toString())
    offerPrice.setText(itemToUpdate.offerPrice.toString())
    arpanProfitPrice.setText(itemToUpdate.arpanCharge.toString())

    if(!itemToUpdate.activeHours.isNullOrEmpty()){
      startTimeOrder.setText(itemToUpdate.activeHours!!.split("TO")[0])
      endTimeOrder.setText(itemToUpdate.activeHours!!.split("TO")[1])
    }

    if (itemToUpdate.icon != null) {
      Glide.with(this)
        .load(Constants.SERVER_FILES_BASE_URL + itemToUpdate.icon!!)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()
        .override(300, 300)
        .placeholder(R.drawable.test_shop_image).into(imagePick)
    }

    view.startTimeOrder.setOnClickListener {
      var timePicker = TimePickerDialog.newInstance(
        { view, hourOfDay, minute, second ->
          startTimeOrder.setText("$hourOfDay:$minute")
        }, false
      )
      timePicker.show(childFragmentManager, "Start_Time")
    }

    view.endTimeOrder.setOnClickListener {
      var timePicker = TimePickerDialog.newInstance(
        { view, hourOfDay, minute, second ->
          endTimeOrder.setText("$hourOfDay:$minute")
        }, false
      )
      timePicker.show(childFragmentManager, "End_Time")
    }

  }

  private fun initOnClicks(view: View) {
    imagePick.setOnClickListener {
//            val getImageIntent = Intent(Intent.ACTION_GET_CONTENT)
//            getImageIntent.type = "image/*"
//            startActivityForResult(
//                Intent.createChooser(
//                    getImageIntent,
//                    "Select Picture"
//                ), PICK_IMAGE_CODE
//            )
      ImagePicker.with(this)
        .cropSquare()            //Crop image(Optional), Check Customization for more option
        .compress(40)      //Final image size will be less than 1 MB(Optional)
        .maxResultSize(512, 512)  //Final image resolution will be less than 1080 x 1080(Optional)
        .start()
    }

    upload.setOnClickListener {
      if (price.text.isNotEmpty() &&
        productTitle.text.isNotEmpty()
      ) {
        cardViewButton.visibility = View.GONE
        progress_circular.visibility = View.VISIBLE
        if (imagePath.toString().isNotEmpty()) {
          uploadFile(view)
        } else {
          val hashMap = HashMap<String, Any>()
          //hashMap["image1"] = imageName
          hashMap["price"] = price.text.toString()
          //hashMap["shopKey"] = shop_key
          hashMap["name"] = productTitle.text.toString()
          if (offerPrice.text.toString().isNotEmpty()) {
            hashMap["offerPrice"] = offerPrice.text.toString()
          } else {
            hashMap["offerPrice"] = price.text.toString()
          }
          if (arpanProfitPrice.text.isEmpty()) {
            hashMap["arpanCharge"] = "0"
          } else {
            hashMap["arpanCharge"] = arpanProfitPrice.text.toString()
          }
          if(!view.startTimeOrder.text.isNullOrEmpty() && !view.endTimeOrder.text.isNullOrEmpty()){
            hashMap["activeHours"] = "${view.startTimeOrder.text}TO${view.endTimeOrder.text}"
          }
          hashMap["shortDescription"] = productDesc.text.toString()
          //hashMap["inStock"] = "active"
          //hashMap["offerStatus"] = "inactive"
          //hashMap["order"] = product_order
          //hashMap["offerDetails"] = "স্পেশাল ওফার"
          //hashMap["shopCategoryKey"] = product_category_tag
          LiveDataUtil.observeOnce(productViewModel.updateProductItem(key, hashMap)) {
            if (it.id != null) {
              (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex] =
                it
              (activity as ProductsActivity).productsItemAdapterMain.notifyItemChanged((activity as ProductsActivity).currentSelectedProductMainIndex)
              dismiss()
            } else {
              cardViewButton.visibility = View.VISIBLE
              progress_circular.visibility = View.GONE
              requireContext().showToast("Failed to update", FancyToast.ERROR)
            }
          }
        }
      } else {
        requireContext().showToast("Fill everything", FancyToast.ERROR)
      }
    }
  }

  private fun uploadFile(view: View) {
    val iconStringName = "icon${System.currentTimeMillis()}"
    // Pass it like this
    val file = imagePath.toFile()
    val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/png"), file)
    // MultipartBody.Part is used to send also the actual file name
    val body: MultipartBody.Part = MultipartBody.Part.createFormData(
      "fileName",
      key + "_" + iconStringName + ".png",
      requestFile
    )

    LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "products")) {
      if (it == null) {
        requireContext().showToast("Failed to upload image", FancyToast.ERROR)
      } else {
        val hashMap = HashMap<String, Any>()
        //hashMap["image1"] = imageName
        hashMap["price"] = price.text.toString()
        //hashMap["shopKey"] = shop_key
        hashMap["name"] = productTitle.text.toString()
        hashMap["icon"] = it
        if (offerPrice.text.toString().isNotEmpty()) {
          hashMap["offerPrice"] = offerPrice.text.toString()
        } else {
          hashMap["offerPrice"] = price.text.toString()
        }
        if (arpanProfitPrice.text.isEmpty()) {
          hashMap["arpanCharge"] = "0"
        } else {
          hashMap["arpanCharge"] = arpanProfitPrice.text.toString()
        }
        hashMap["shortDescription"] = productDesc.text.toString()
        if(!view.startTimeOrder.text.isNullOrEmpty() && !view.endTimeOrder.text.isNullOrEmpty()){
          hashMap["activeHours"] = "${view.startTimeOrder.text}TO${view.endTimeOrder.text}"
        }
        //hashMap["inStock"] = "active"
        //hashMap["offerStatus"] = "inactive"
        //hashMap["order"] = product_order
        //hashMap["offerDetails"] = "স্পেশাল ওফার"
        //hashMap["shopCategoryKey"] = product_category_tag
        LiveDataUtil.observeOnce(productViewModel.updateProductItem(key, hashMap)) { product ->
          if (product.id != null) {
            (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex] =
              product
            (activity as ProductsActivity).productsItemAdapterMain.notifyItemChanged((activity as ProductsActivity).currentSelectedProductMainIndex)
            dismiss()
          } else {
            cardViewButton.visibility = View.VISIBLE
            progress_circular.visibility = View.GONE
            requireContext().showToast("Failed to update", FancyToast.ERROR)
          }
        }
      }
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
//            val fullPhotoUri = data!!.data as Uri
//            UCrop.of(fullPhotoUri, Uri.fromFile(File(cacheDir, key)))
//                .withAspectRatio(1F, 1F)
//                .withMaxResultSize(250, 250)
//                .start(this)
//        }else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
//            imagePath = UCrop.getOutput(data!!) as Uri
//            imagePick.setImageURI(imagePath)
//            val file = File(imagePath.path!!)
//            titleTextView.text = "Image Size : ${file.length()/1024} KB"
//            imageName = key
//        } else if (resultCode == UCrop.RESULT_ERROR) {
//            Toast.makeText(this,UCrop.RESULT_ERROR,Toast.LENGTH_SHORT).show()
//        }

    if (resultCode == Activity.RESULT_OK) {
      imagePath = data!!.data!!
      imagePick.setImageURI(imagePath)
      imagePick.setPadding(0, 0, 0, 0)
      val file = File(imagePath.path!!)
      titleTextView.text = "Image Size : ${file.length() / 1024} KB"
      imageName = "prdctimg" + System.currentTimeMillis()
    } else if (resultCode == ImagePicker.RESULT_ERROR) {
      Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
    }
  }
}