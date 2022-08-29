package da.arpan.delivery.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.models.Location
import da.arpan.delivery.R
import kotlinx.android.synthetic.main.location_item_recycler_view.view.*

class LocationItemRecyclerAdapter(val context : Context,
                                  val locations : ArrayList<Location>,
                                  val firebaseDatabaseLocation : String)
    : RecyclerView.Adapter<LocationItemRecyclerAdapter.RecyclerViewHolder>(){

    class RecyclerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val locationName = itemView.locationName as TextView
        val regularCharge = itemView.regularCharge as TextView
        val clientCharge = itemView.clientCharge as TextView
        val daShare = itemView.daShare as TextView
        val arpanShareRegular = itemView.arpanShareRegular as TextView
        val arpanShareClient = itemView.arpanShareClient as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.location_item_recycler_view,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.locationName.text = locations[position].locationName
        holder.regularCharge.text = locations[position].deliveryCharge.toString()
        holder.clientCharge.text = locations[position].deliveryChargeClient.toString()
        holder.daShare.text = locations[position].daCharge.toString()
        holder.arpanShareRegular.text = (locations[position].deliveryCharge!!-locations[position].daCharge!!).toString()
        holder.arpanShareClient.text = (locations[position].deliveryChargeClient!!-locations[position].daCharge!!).toString()
    }

    override fun getItemCount(): Int {
        return locations.size
    }


}