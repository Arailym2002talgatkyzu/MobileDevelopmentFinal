package com.example.mobiledevelopmentfinal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevelopmentfinal.R
import com.example.mobiledevelopmentfinal.data.LocationModel
import com.squareup.picasso.Picasso

class LocationListRvAdapter (private val locationList: ArrayList<LocationModel>,
private val context: Context
): RecyclerView.Adapter<LocationListRvAdapter.LocationViewHolder>(){
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationListRvAdapter.LocationViewHolder {
        // this method is use to inflate the layout file
        // which we have created for our recycler view.
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.locations_list_view,
            parent, false
        )
        // at last we are returning our view holder
        return LocationViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return locationList.size
    }


    override fun onBindViewHolder(holder: LocationListRvAdapter.LocationViewHolder, position: Int) {
        val model = locationList[position]
        holder.cityNameTV.text = model.cityName
        holder.conditionTV.text = model.condition
        holder.tempTV.text = model.temp
        Picasso.get().load(model.weatherIcon).into(holder.weatherIV)

        holder.itemView.setOnClickListener {

            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: LocationModel)
    }
    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // on below line we are initializing location data
        val cityNameTV: TextView = itemView.findViewById(R.id.tvCity)
        val weatherIV: ImageView = itemView.findViewById(R.id.tvWeatherIcon)
        val conditionTV: TextView = itemView.findViewById(R.id.tvCondition)
        val tempTV: TextView = itemView.findViewById(R.id.tvTemp)
    }

}