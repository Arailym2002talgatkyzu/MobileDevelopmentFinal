package com.example.mobiledevelopmentfinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevelopmentfinal.adapter.RvAdapter
import com.example.mobiledevelopmentfinal.data.forecastModels.ForecastData
import com.example.mobiledevelopmentfinal.data.utils.RetrofitInstance
import com.example.mobiledevelopmentfinal.databinding.ActivityMainBinding
import com.example.mobiledevelopmentfinal.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val api_key: String = "7946d051fd8dcb8736a7e0b3505f7b80"
    private var city: String = "genoa"
    private lateinit var sheetLayoutBinding: BottomSheetLayoutBinding
    private lateinit var dialog: BottomSheetDialog
    lateinit var pollutionFragment: PollutionFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMainBinding.inflate(layoutInflater)
        sheetLayoutBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        dialog.setContentView(sheetLayoutBinding.root)
        setContentView(binding.root)

        //Getting intent from Locations activity(open the weather of specific city)
        if(intent.hasExtra(Locations.LOCATION_DETAILS)){
            // get the city name
            city =
                intent.getStringExtra(Locations.LOCATION_DETAILS) as String
        }

        binding.Locations.setOnClickListener{
            val intent = Intent(this, Locations::class.java)
            startActivity(intent)
            this?.finish()
        }

        pollutionFragment = PollutionFragment()


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


        getCurrentWeather(city)

        binding.tvForecast.setOnClickListener{
            openDialog()
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

                    val sunrise = timeFormatter(data.sys.sunrise, data.timezone)
                    val sunset = timeFormatter(data.sys.sunset, data.timezone)
                    binding.tvSunrise.text=sunrise
                    binding.tvSunset.text=sunset

                    binding.apply {
                        tvStatus.text=capitalizeFirstChar(data.weather[0].description)
                        tvWind.text="${data.wind.speed.toString()} KM/H"
                        tvLocation.text = "${data.name}, ${data.sys.country}"
                        tvTemp.text = "${data.main.temp.toInt()}°C"
                        tvFeelsLike.text = "Feels like: ${data.main.feels_like.toInt()}°C"
                        tvMinTemp.text = "Min temp: ${data.main.temp_min.toInt()}°C"
                        tvMaxTemp.text = "Max temp: ${data.main.temp_max.toInt()}°C"
                        tvHumidity.text = "${data.main.humidity} %"
                        tvPressure.text = "${data.main.pressure} hPa"
                        val updTime = getCurrentLocalDateTime()
                        tvUpdateTime.text = "Updated: "+updTime

                        getPollution(data.coord.lat, data.coord.lon)
                    }
                }
            }
        }
    }

    //function to Format the time from API according to timezone
    private fun timeFormatter(tmstmp: Int, tmz: Int): CharSequence?{
        val zoneId = ZoneId.ofOffset("UTC", java.time.ZoneOffset.ofTotalSeconds(tmz))
        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(tmstmp.toLong()), zoneId)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        return zonedDateTime.format(formatter)
    }

    fun getCurrentLocalDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
        return current.format(formatter)
    }

    fun capitalizeFirstChar(input: String): String {
        if (input.isEmpty()) return input
        return input.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
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