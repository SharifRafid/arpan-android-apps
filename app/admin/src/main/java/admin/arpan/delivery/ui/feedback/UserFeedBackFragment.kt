package admin.arpan.delivery.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.viewModels.AdminViewModel
import admin.arpan.delivery.viewModels.UserViewModel
import android.content.Context
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_user_feed_back.view.*


@AndroidEntryPoint
class UserFeedBackFragment : DialogFragment() {
    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,R.style.Theme_AdminArpan)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_feed_back, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.title_text_view.setOnClickListener {
            dismiss()
        }
        LiveDataUtil.observeOnce(adminViewModel.getAllFeedbacks()){
            if(it.error == true){
                context?.showToast("Failed to load data", FancyToast.ERROR)
            }else{
                val feedbacksList = ArrayList<String>()
                for(i in it.results!!){
                    feedbacksList.add(i.name + "\n" + i.details)
                }
                view.listViewFeedBacks.adapter = ArrayAdapter(
                    activity as Context,
                    R.layout.textview_item_list_view,
                    R.id.topTitle,
                    feedbacksList
                )
            }
        }
    }

}