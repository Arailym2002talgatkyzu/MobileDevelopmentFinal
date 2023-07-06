package com.example.mobiledevelopmentfinal

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mobiledevelopmentfinal.databinding.FragmentPollutionBinding
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate


class PollutionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentPollutionBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        _binding = FragmentPollutionBinding.inflate(inflater, container, false)

        val data = arguments
        val pollutants = listOf(
            "co" to "CO",
            "nh3" to "NH3",
            "no" to "NO",
            "no2" to "NO2",
            "o3" to "O3",
            "pm10" to "PM10",
            "pm2_5" to "PM2.5",
            "so2" to "SO2"
        )

        val list = arrayListOf<BarEntry>()
        val txtBuilder = StringBuilder()

        pollutants.forEachIndexed { index, (key,label) ->
            val value = data?.getDouble(key)
            if (value != null){
                list.add(BarEntry((index + 1).toFloat(), value.toFloat()))
            }
            txtBuilder.append("$label: ${value?: "-"}\n")
        }

        binding.textView.text = txtBuilder.toString()

        val barDataSet = BarDataSet(list, "Pollutants")


        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS,255)
        barDataSet.valueTextColor = Color.WHITE
        barDataSet.valueTextSize = 13f
        barDataSet.barBorderColor = Color.BLACK

        barDataSet.barBorderWidth = 1f
        val l: Legend = binding.barChart.getLegend()
        l.textSize = 20f
        l.textColor = Color.WHITE
        val barData = BarData(barDataSet)
        binding.barChart.data = barData

        binding.barChart.apply {
            description.text = "Air Pollutants"
            description.textSize = 15f
            animateY(1000)
        }

        val quarters = arrayOf("", "CO", "NH3", "NO", "NO2", "PM10", "O3", "PM2_5", "so2")
        val formatter : ValueFormatter = object :ValueFormatter(){
            override fun getAxisLabel(value : Float, axis: AxisBase): String {
                return quarters[value.toInt()]
            }
        }
        val xAxis = binding.barChart.xAxis
        val leftAxis = binding.barChart.axisLeft
        val rightAxis = binding.barChart.axisRight
        xAxis.valueFormatter = formatter
        xAxis.textColor = Color.WHITE
        leftAxis.textColor = Color.WHITE
        rightAxis.textColor = Color.WHITE

        leftAxis.textSize = 15f
        rightAxis.textSize = 15f
        xAxis.textSize = 15f

        return binding.root
    }

}