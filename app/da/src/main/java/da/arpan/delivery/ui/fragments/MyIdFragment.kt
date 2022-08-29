package da.arpan.delivery.ui.fragments

import android.R.attr.bitmap
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.WriterException
import da.arpan.delivery.R
import kotlinx.android.synthetic.main.fragment_my_id.view.*
import kotlinx.android.synthetic.main.fragment_my_id.view.title_text_view
import kotlinx.android.synthetic.main.fragment_wallet_dialog.view.*


class MyIdFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,
            R.style.Theme_ArpanDA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_id, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.title_text_view.setOnClickListener{
            dismiss()
        }
        val sharedPreferences = view.context.getSharedPreferences("user_details",
            AppCompatActivity.MODE_PRIVATE
        )
        view.daIdTextView.text = "ID: "+ sharedPreferences.getString("da_uid","").toString()
        view.daNameTextView.text = "Name: "+ sharedPreferences.getString("da_name","").toString()
        val daCategory = if(sharedPreferences.getString("da_category","").toString()=="রেগুলার"){
            "Regular"
        }else{
            "Permanent"
        }
        view.daTypeTextView.text = "Agent Type: $daCategory"
        view.daBkashTextView.text = "bKash: "+ sharedPreferences.getString("da_bkash","").toString()
        view.daBloodGroupTextView.text = "Blood Group: "+ sharedPreferences.getString("da_blood_group","").toString()
        view.daContactTextView.text = "Contact: "+ sharedPreferences.getString("da_mobile","").toString()

        val image = sharedPreferences.getString("da_image","").toString()

        if(image.isNotEmpty()){
            val storageReference = FirebaseStorage.getInstance()
                .getReference("da_storage_image_location")
                .child(image)

            Glide.with(requireActivity())
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(300,300)
                .placeholder(R.drawable.ic_baseline_perm_identity_24)
                .into(view.daImageViewProfile)
        }

        val manager = requireContext().getSystemService(WINDOW_SERVICE) as WindowManager?
        val display = manager!!.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width: Int = point.x
        val height: Int = point.y
        var smallerDimension = if (width < height) width else height
        smallerDimension = smallerDimension * 3 / 4

        val inputValue = "Arpan Delivery Agent\nID: "+ sharedPreferences.getString("da_uid","").toString()

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

    fun mergeBitmaps(logo: Bitmap?, qrcode: Bitmap): Bitmap? {
        val combined = Bitmap.createBitmap(qrcode.width, qrcode.height, qrcode.config)
        val canvas = Canvas(combined)
        val canvasWidth: Int = canvas.getWidth()
        val canvasHeight: Int = canvas.getHeight()
        canvas.drawBitmap(qrcode, Matrix(), null)
        val resizeLogo = Bitmap.createScaledBitmap(logo!!, canvasWidth / 5, canvasHeight / 5, true)
        val centreX = (canvasWidth - resizeLogo.width) / 2
        val centreY = (canvasHeight - resizeLogo.height) / 2
        canvas.drawBitmap(resizeLogo, centreX.toFloat(), centreY.toFloat(), null)
        return combined
    }
}
