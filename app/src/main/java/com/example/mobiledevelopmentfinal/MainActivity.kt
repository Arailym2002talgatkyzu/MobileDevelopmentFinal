package com.example.mobiledevelopmentfinal

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevelopmentfinal.adapter.RvAdapter
import com.example.mobiledevelopmentfinal.data.forecastModels.ForecastData
import com.example.mobiledevelopmentfinal.data.utils.RetrofitInstance
import com.example.mobiledevelopmentfinal.databinding.ActivityMainBinding
import com.example.mobiledevelopmentfinal.databinding.BottomSheetLayoutBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val api_key: String = "7946d051fd8dcb8736a7e0b3505f7b80"
    private var city: String = "genoa"
    private lateinit var sheetLayoutBinding: BottomSheetLayoutBinding
    private lateinit var dialog: BottomSheetDialog
    lateinit var pollutionFragment: PollutionFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMainBinding.inflate(layoutInflater)
        sheetLayoutBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        dialog.setContentView(sheetLayoutBinding.root)
        setContentView(binding.root)

        pollutionFragment = PollutionFragment()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query!= null){
                    city = query
                }
                getCurrentWeather(city)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        fetchLocation()
        getCurrentWeather(city)

        binding.tvForecast.setOnClickListener{
            openDialog()
        }

        binding.tvLocation.setOnClickListener {
            fetchLocation()
        }

    }

    //Works only on physical device
    private fun fetchLocation() {
        val task: Task<Location> = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        task.addOnSuccessListener {
            val geoCoder = Geocoder(this, Locale.getDefault())
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                geoCoder.getFromLocation(it.latitude, it.longitude, 1, object: Geocoder.GeocodeListener{
                    override fun onGeocode(locations: MutableList<Address>) {
                        city = locations[0].locality
                    }

                })
            }
            else{
                val location = geoCoder.getFromLocation(it.latitude, it.longitude, 1) as List<Address>
                city = location[0].locality
            }
            getCurrentWeather(city)
        }

    }

    private fun openDialog() {

        Log.d("OpenDialog", "Open Dialog called")
        getForecast()
        sheetLayoutBinding.rvForecast.apply {
            setHasFixedSize(true)
            layoutManager=GridLayoutManager(this@MainActivity, 1, RecyclerView.HORIZONTAL, false)
        }
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
    }

    private fun getForecast() {
        Log.d("getForecast", "Get Forecast called")
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.api.getForecast(city, "metric", api_key)
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(applicationContext, "http error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val data = response.body()!!
                    var forecastArray= arrayListOf<ForecastData>()
                    forecastArray=data.list as ArrayList<ForecastData>
                    val adapter = RvAdapter(forecastArray)
                    sheetLayoutBinding.rvForecast.adapter = adapter
                    sheetLayoutBinding.tvSheet.text= "Five days forecast in ${data.city.name}"

                }
            }
        }
    }

    private fun getCurrentWeather(city: String) {
        GlobalScope.launch(Dispatchers.IO){
          val response =  try{
            RetrofitInstance.api.getCurrentWeather(city, "metric", api_key)
            }catch (e: IOException){
              Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT).show()
              return@launch
            }
            catch (e: HttpException){
                Toast.makeText(applicationContext, "http error ${e.message}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if(response.isSuccessful && response.body()!=null){
                withContext(Dispatchers.Main){
                    val data =response.body()!!
                    val iconId = data.weather[0].icon

                    val imgurl ="https://openweathermap.org/img/wn/$iconId@4x.png"
                    Picasso.get().load(imgurl).into(binding.imgWeather)
                    binding.tvSunrise.text=
                        SimpleDateFormat(
                            "hh:mm a",
                            Locale.ENGLISH
                        ).format(data.sys.sunrise*1000)

                    binding.tvSunset.text=
                        SimpleDateFormat(
                            "hh:mm a",
                            Locale.ENGLISH
                        ).format(data.sys.sunset*1000)
                    binding.apply {
                        tvStatus.text=data.weather[0].description
                        tvWind.text="${data.wind.speed.toString()} KM/H"
                        tvLocation.text = "${data.name}\n${data.sys.country}"
                        tvTemp.text = "${data.main.temp.toInt()}°C"
                        tvFeelsLike.text = "Feels like: ${data.main.feels_like.toInt()}°C"
                        tvMinTemp.text = "Min temp: ${data.main.temp_min.toInt()}°C"
                        tvMaxTemp.text = "Max temp: ${data.main.temp_max.toInt()}°C"
                        tvHumidity.text = "${data.main.humidity} %"
                        tvPressure.text = "${data.main.pressure} hPa"
                        tvUpdateTime.text = "Last updated: ${
                            SimpleDateFormat(
                                "hh:mm a",
                                Locale.ENGLISH
                            ).format(data.dt*1000)
                        }"

                        getPollution(data.coord.lat, data.coord.lon)
                    }
                }
            }
        }
    }

    private fun getPollution(lat: Double, lon: Double) {
        GlobalScope.launch(Dispatchers.IO){
            val response =  try{
                RetrofitInstance.api.getPollution(
                    lat,
                    lon,
                    "metric",
                    api_key)
            }catch (e: IOException){
                Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT).show()
                return@launch
            }
            catch (e: HttpException){
                Toast.makeText(applicationContext, "http error ${e.message}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if(response.isSuccessful && response.body()!=null){
                withContext(Dispatchers.Main){
                    val data =response.body()!!

                    val num = data.list[0].main.aqi

                    binding.tvAirQual.text = when(num){
                        1 -> getString(R.string.good)
                        2 -> getString(R.string.fair)
                        3 -> getString(R.string.moderate)
                        4 -> getString(R.string.poor)
                        5 -> getString(R.string.very_poor)
                        else -> "no data"
                    }
                    binding.layoutPollution.setOnClickListener{
                        val bundle = Bundle()
                        bundle.putDouble("co", data.list[0].components.co)
                        bundle.putDouble("nh3", data.list[0].components.nh3)
                        bundle.putDouble("no", data.list[0].components.no)
                        bundle.putDouble("no2", data.list[0].components.no2)
                        bundle.putDouble("o3", data.list[0].components.o3)
                        bundle.putDouble("pm10", data.list[0].components.pm10)
                        bundle.putDouble("pm2_5", data.list[0].components.pm2_5)
                        bundle.putDouble("so2", data.list[0].components.so2)

                        pollutionFragment.arguments = bundle

                        supportFragmentManager.beginTransaction().apply {
                            replace(R.id.frameLayout, pollutionFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
            }
        }

    }

}