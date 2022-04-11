package com.lospollos.kino.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import com.lospollos.cinemahallview.CinemaHallView
import com.lospollos.kino.R

class MainActivity : AppCompatActivity() {

    private lateinit var cinemaHallView: CinemaHallView
    private lateinit var confirmButton: Button
    private lateinit var enableSwitch: SwitchCompat
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cinemaHallView = findViewById(R.id.cinema_hall_view)
        confirmButton = findViewById(R.id.confirm_button)
        enableSwitch = findViewById(R.id.enable_switch)
        refreshButton = findViewById(R.id.refresh_button)
        cinemaHallView.cinemaHallMatrix =
            arrayListOf(
                arrayListOf(1, 0, 0, 1),
                arrayListOf(1, 1, 1, 1),
                arrayListOf(0, 0, 0, 0),
                arrayListOf(1, 1, 1, 1),
            )
        confirmButton.setOnClickListener {
            cinemaHallView.confirm()
        }
        refreshButton.setOnClickListener {
            cinemaHallView.refresh()
        }
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            cinemaHallView.isViewEnabled = isChecked
            confirmButton.isClickable = isChecked
            refreshButton.isClickable = isChecked
        }
    }
}