package com.example.mobiledevelopmentfinal

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevelopmentfinal.adapter.LocationListRvAdapter
import com.example.mobiledevelopmentfinal.data.LocationModel
import com.example.mobiledevelopmentfinal.data.utils.RetrofitInstance
import com.example.mobiledevelopmentfinal.databinding.WeatherForecastBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class Locations : AppCompatActivity() {
    lateinit var locationRV: RecyclerView
    lateinit var locationRVAdapter: LocationListRvAdapter
    private lateinit var binding: WeatherForecastBinding
    lateinit var locationList: ArrayList<LocationModel>

    private lateinit var cities: SharedPreferences
    val api_key: String = "7946d051fd8dcb8736a7e0b3505f7b80"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast)
        binding = WeatherForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationRV = findViewById(R.id.idRVLocations)

        loadData()

        binding.Save.setOnClickListener {
            val city = binding.City.text.toString().lowercase()
            saveCity(this, city)
            binding.City.setText("")
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                val deletedLocation: LocationModel =
                    locationList.get(viewHolder.adapterPosition)

                // below line is to get the position of the item at that position.
                val position = viewHolder.adapterPosition

                // this method is called when item is swiped.
                // below line is to remove item from the array list.
                locationList.removeAt(viewHolder.adapterPosition)
                removeCity(this@Locations, deletedLocation.cityName.lowercase())

                // below line is to notify our item is removed from adapter.
                locationRVAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                // below line is to display our snackbar with action.
                Snackbar.make(locationRV, "Deleted " + deletedLocation.cityName, Snackbar.LENGTH_LONG)
                    .setAction(
                        "Undo",
                        View.OnClickListener {
                            // adding on click listener to our action of snack bar.
                            // below line is to add our item to array list with a position.
                            locationList.add(position, deletedLocation)
                            saveCity(this@Locations,deletedLocation.cityName.lowercase())
                            locationRVAdapter.notifyItemInserted(position)
                        }).show()
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(locationRV)

        locationRVAdapter.setOnClickListener(object: LocationListRvAdapter.OnClickListener{
            override fun onClick(position: Int, model: LocationModel) {
                val intent = Intent(this@Locations, MainActivity::class.java)
                intent.putExtra(LOCATION_DETAILS, model.cityName) // Passing the cityName to the main activity using intent.
                startActivity(intent)
            }
        })



    }

    private fun loadData() {
        val cityList = getCities(this)
        var cityName = ""
        var weatherIcon =""
        var condition=""
        var temperature=""
        locationList = ArrayList()
        locationRVAdapter = LocationListRvAdapter(locationList, this)
        locationRV.adapter = locationRVAdapter
        if (cityList.isEmpty()){
            binding.noCity.visibility=View.VISIBLE
        }
        else {
            binding.noCity.visibility=View.GONE
            for (city in cityList) {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = try {
                        @Suppress("BlockingMethodInNonBlockingContext")
                        RetrofitInstance.api.getCurrentWeather(city, "metric", api_key)
                    } catch (e: IOException) {
                        Toast.makeText(
                            applicationContext,
                            "app error ${e.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@launch
                    } catch (e: HttpException) {
                        Toast.makeText(
                            applicationContext,
                            "http error ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    withContext(Dispatchers.Main) {
                        if (response == null) {
                            Toast.makeText(
                                this@Locations,
                                "Please check your internet connection!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val data = response.body()!!
                            val iconId = data.weather[0].icon
                            cityName = city.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                            weatherIcon = "https://openweathermap.org/img/wn/$iconId@4x.png"
                            condition = data.weather[0].description.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            }
                            temperature = "${data.main.temp.toInt()}°"

                            locationList.add(
                                LocationModel(
                                    weatherIcon,
                                    cityName,
                                    condition,
                                    temperature
                                )
                            )
                            locationRVAdapter.notifyDataSetChanged()

                        }
                    }
                }
            }
        }
    }


    private fun getCities(con: Context): Array<String>{
        cities = con.getSharedPreferences("com.example.mobiledevelopmentfinal", 0)
        return cities.getStringSet("cityList", emptySet())?.toTypedArray() ?: emptyArray();
    }

    private fun removeCity(con: Context, city: String){
        val cityList = getCities(this).toMutableList()
        cityList.remove(city.lowercase())
        println(cityList)
        cities = con.getSharedPreferences("com.example.mobiledevelopmentfinal", 0)
        val editor: SharedPreferences.Editor = cities.edit()
        editor.putStringSet("cityList", cityList.toSet())
        editor.apply()
    }

    private fun saveCity(con:Context, city: String){
        val cityList = getCities(this).toMutableList()
        //check city for existence before adding to list
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                @Suppress("BlockingMethodInNonBlockingContext")
                RetrofitInstance.api.getCurrentWeather(city, "metric", api_key)
            } catch (e: IOException) {
                Toast.makeText(
                    applicationContext,
                    "app error ${e.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(
                    applicationContext,
                    "http error ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            withContext(Dispatchers.Main) {
                if (response == null) {
                    Toast.makeText(
                        this@Locations,
                        "Please check your internet connection!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (response.body()==null){
                        Toast.makeText(
                            this@Locations,
                            "City Not Found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        if(cityList.contains(city.lowercase())){
                            Toast.makeText(
                                this@Locations,
                                "The city already in the list",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else{
                            cityList.add(city.lowercase())
                            cities = con.getSharedPreferences("com.example.mobiledevelopmentfinal", 0)
                            val editor: SharedPreferences.Editor = cities.edit()
                            editor.putStringSet("cityList", cityList.toSet())
                            editor.apply()
                            loadData()
                        }
                    }
                }
            }}
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
      val LOCATION_DETAILS = "place_details"
    }
}

