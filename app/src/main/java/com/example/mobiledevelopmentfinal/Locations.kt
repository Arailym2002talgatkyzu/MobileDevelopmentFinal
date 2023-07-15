package com.example.mobiledevelopmentfinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobiledevelopmentfinal.databinding.WeatherForecastBinding

class Locations : AppCompatActivity() {
    private lateinit var binding: WeatherForecastBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = WeatherForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}