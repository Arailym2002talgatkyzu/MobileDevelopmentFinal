package com.example.mobiledevelopmentfinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mobiledevelopmentfinal.data.utils.RetrofitInstance
import com.example.mobiledevelopmentfinal.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

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
                    binding.tvTemp.text
                }
            }
        }
    }

}