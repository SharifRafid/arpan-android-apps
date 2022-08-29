package da.arpan.delivery.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import core.arpan.delivery.models.OrderItemMain
import core.arpan.delivery.utils.CalculationLogics
import core.arpan.delivery.utils.Constants
import da.arpan.delivery.R
import kotlinx.android.synthetic.main.fragment_wallet_dialog.*
import kotlinx.android.synthetic.main.fragment_wallet_dialog.view.*
import java.text.SimpleDateFormat
import java.util.*
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
class WalletDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_wallet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.title_text_view.setOnClickListener{
            dismiss()
        }
        val date = getDate(System.currentTimeMillis(), "MM-yyyy").toString()
        //(date.split("-")[0].toInt()+1)

        val dt = Date()
        val c = Calendar.getInstance()
        c.time = dt
        c.add(Calendar.MONTH, -1)
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.DAY_OF_MONTH] = 1

        val c2 = Calendar.getInstance() // this takes current date
        c2.time = dt
        c2.add(Calendar.MONTH, -1)
        c2[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH) //Date set to end month
        c2[Calendar.HOUR_OF_DAY] = 24

        val prevDate = getDate(c.timeInMillis,  "MM-yyyy").toString()

        myIncomeTextView.text = (activity as HomeActivity).thisMonthsMyIncome.toString()
        totalOrderThisMonthTextView.text = (activity as HomeActivity).thisMonthsTotalOrder.toString()

        arpanBokeyaTextView.text = (activity as HomeActivity).thisMonthsArpanBokeya.toString()

        if((activity as HomeActivity).firestoreDatabaseSnapshot!=null){
            val it = (activity as HomeActivity).firestoreDatabaseSnapshot
            val documentsArray = ArrayList<OrderItemMain>()
            for(document in it!!.documents){
                val o = document.toObject(OrderItemMain::class.java)!!
                o.id = document.id
                documentsArray.add(o)
            }
            val calculationResult = CalculationLogics().calculateArpansStatsForArpan(documentsArray)
            if((activity as HomeActivity).daAgent.daCategory==Constants.DA_PERM){
                myIncomeLastMonthTextView.text = calculationResult.agentsIncomePermanent.toString()
                arpanBokeyaLastMonthTextView.text = calculationResult.agentsDueToArpanPermanent.toString()
            }else{
                myIncomeLastMonthTextView.text = calculationResult.agentsIncome.toString()
                arpanBokeyaLastMonthTextView.text = calculationResult.agentsDueToArpan.toString()
            }
            totalOrderLastMonthTextView.text = calculationResult.totalOrders.toString()
        }else{
            FirebaseFirestore.getInstance()
                .collectionGroup("users_order_collection")
                .whereEqualTo("daID",requireContext().getSharedPreferences("user_details",
                    AppCompatActivity.MODE_PRIVATE
                ).getString("key","").toString())
                .whereEqualTo("orderStatus", "COMPLETED")
                .whereGreaterThanOrEqualTo("orderPlacingTimeStamp", c.timeInMillis)
                .whereLessThanOrEqualTo("orderPlacingTimeStamp", c2.timeInMillis)
                .orderBy("orderPlacingTimeStamp")
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        (activity as HomeActivity).firestoreDatabaseSnapshot = it.result
                        val documentsArray = ArrayList<OrderItemMain>()
                        for(document in it.result!!.documents){
                            val o = document.toObject(OrderItemMain::class.java)!!
                            o.id = document.id
                            documentsArray.add(o)
                        }
                        val calculationResult = CalculationLogics().calculateArpansStatsForArpan(documentsArray)
                        if((activity as HomeActivity).daAgent.daCategory==Constants.DA_PERM){
                            myIncomeLastMonthTextView.text = calculationResult.agentsIncomePermanent.toString()
                            arpanBokeyaLastMonthTextView.text = calculationResult.agentsDueToArpanPermanent.toString()
                        }else{
                            myIncomeLastMonthTextView.text = calculationResult.agentsIncome.toString()
                            arpanBokeyaLastMonthTextView.text = calculationResult.agentsDueToArpan.toString()
                        }
                        totalOrderLastMonthTextView.text = calculationResult.totalOrders.toString()
                    }else{
                        it.exception!!.printStackTrace()
                    }
                }
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WalletDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WalletDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}