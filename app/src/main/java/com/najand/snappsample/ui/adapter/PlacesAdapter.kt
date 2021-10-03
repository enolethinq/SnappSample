package com.najand.snappsample.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.najand.snappsample.R
import com.najand.snappsample.data.model.PlacesResult
import kotlinx.android.synthetic.main.place_item.view.*

class PlacesAdapter(private var list: PlacesResult, private var listener: OnItemClicked) : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        return PlacesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false))
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        holder.itemView.itemTitleTv.text = list.places[position].title
        holder.itemView.itemAddressTv.text = list.places[position].address
        holder.itemView.setOnClickListener(View.OnClickListener {
            listener.setOnItemClickedListener(list.places[position].location.y, list.places[position].location.x)
        })
    }

    override fun getItemCount(): Int {
        return list.count
    }

    class PlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    interface OnItemClicked{
        fun setOnItemClickedListener(lat: Float, lng: Float)
    }

}

//private fun LayoutInflater.inflate(placeItem: Int, b: Boolean): View {
//    return
//}
