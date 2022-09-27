package da.arpan.delivery.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import core.arpan.delivery.models.Location
import core.arpan.delivery.utils.LiveDataUtil
import da.arpan.delivery.R
import da.arpan.delivery.adapters.LocationItemRecyclerAdapter
import da.arpan.delivery.viewModels.AuthViewModel
import da.arpan.delivery.viewModels.PublicViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_delivery_charge.view.*
import kotlinx.android.synthetic.main.fragment_wallet_dialog.view.title_text_view
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WalletDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class DeliveryChargeChartFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var normalLocationsItemsRecyclerAdapter : LocationItemRecyclerAdapter
    private val publicViewModel: PublicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,
            R.style.Theme_ArpanDA)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delivery_charge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.title_text_view.setOnClickListener{
            dismiss()
        }
        LiveDataUtil.observeOnce(publicViewModel.getAllLocations()){
            if(it.isNotEmpty()){
                view.deliveryChargeRecyclerView.layoutManager = LinearLayoutManager(activity)
                normalLocationsItemsRecyclerAdapter =
                    LocationItemRecyclerAdapter(requireActivity(), it)
                view.deliveryChargeRecyclerView.adapter = normalLocationsItemsRecyclerAdapter
            }
        }
    }
}