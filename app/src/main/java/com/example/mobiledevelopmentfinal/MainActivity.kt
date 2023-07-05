package com.example.mobiledevelopmentfinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mobiledevelopmentfinal.data.utils.RetrofitInstance
import com.example.mobiledevelopmentfinal.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val api_key: String = "7946d051fd8dcb8736a7e0b3505f7b80"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCurrentWeather()
    }

    private fun getCurrentWeather() {
        GlobalScope.launch(Dispatchers.IO){
          val response =  try{
            RetrofitInstance.api.getCurrentWeather("Genoa", "metric", api_key)
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
                    }
                }
            }
        }
    }

}