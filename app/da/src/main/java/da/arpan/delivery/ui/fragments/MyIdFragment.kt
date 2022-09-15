package da.arpan.delivery.ui.fragments

import android.R.attr.bitmap
import android.app.AlertDialog
import android.content.Context.WINDOW_SERVICE
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.WriterException
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.utils.Constants
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.createProgressDialog
import core.arpan.delivery.utils.showToast
import da.arpan.delivery.R
import da.arpan.delivery.viewModels.DAViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_alert_layout_main.view.*
import kotlinx.android.synthetic.main.dialog_text_input_da_status.view.*
import kotlinx.android.synthetic.main.fragment_my_id.view.*
import kotlinx.android.synthetic.main.fragment_my_id.view.title_text_view
import kotlinx.android.synthetic.main.fragment_wallet_dialog.view.*

@AndroidEntryPoint
class MyIdFragment : DialogFragment() {
  private val daViewModel: DAViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(
      DialogFragment.STYLE_NORMAL,
      R.style.Theme_ArpanDA
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_my_id, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    view.title_text_view.setOnClickListener {
      dismiss()
    }
    val progressDialog = context?.createProgressDialog()
    progressDialog?.show()
    LiveDataUtil.observeOnce(daViewModel.getSelfProfile()) { selfProfile ->
      progressDialog?.dismiss()
      if (selfProfile.error == true) {
        Log.e("ERROR : ", selfProfile.toString())
        context?.showToast("Failed to fetch data", FancyToast.ERROR)
      } else {
        view.daIdTextView.text = "ID: " + selfProfile.daUID.toString()
        view.daNameTextView.text = "Name: " + selfProfile.name.toString()
        val daCategory = if (selfProfile.daCategory.toString() == Constants.DA_REG) {
          "Regular"
        } else {
          "Permanent"
        }
        view.daTypeTextView.text = "Agent Type: $daCategory"
        view.daBkashTextView.text = "bKash: " + selfProfile.bkash
        view.daBloodGroupTextView.text = "Blood Group: " + selfProfile.bloodGroup
        view.daContactTextView.text = "Contact: " + selfProfile.phone

        if (selfProfile.image != null) {
          if (!selfProfile.image.isNullOrEmpty()) {
            Glide.with(requireActivity())
              .load(Constants.SERVER_FILES_BASE_URL+selfProfile.image)
              .diskCacheStrategy(DiskCacheStrategy.ALL)
              .centerCrop()
              .override(300, 300)
              .placeholder(R.drawable.ic_baseline_perm_identity_24)
              .into(view.daImageViewProfile)
          }
        }

        val manager = requireContext().getSystemService(WINDOW_SERVICE) as WindowManager?
        val display = manager!!.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width: Int = point.x
        val height: Int = point.y
        var smallerDimension = if (width < height) width else height
        smallerDimension = smallerDimension * 3 / 4

        val inputValue =
          "Arpan Delivery Agent\nID: " + selfProfile.daUID

        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        val qrgEncoder = QRGEncoder(inputValue, null, QRGContents.Type.TEXT, smallerDimension)
        qrgEncoder.colorBlack = Color.GRAY
        qrgEncoder.colorWhite = Color.WHITE
        try {
          // Getting QR-Code as Bitmap
          var bitmap2 = qrgEncoder.bitmap
          // Setting Bitmap to ImageView
          view.qrCodeImageViewID.setImageBitmap(bitmap2)
        } catch (e: WriterException) {
          Log.e("QR CODE", e.toString())
        }

      }
    }
  }
}
