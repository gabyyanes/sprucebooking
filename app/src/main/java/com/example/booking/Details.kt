package com.example.booking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.booking.databinding.ActivityDetailsBinding

class Details : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var name: String
    private lateinit var address: String
    private lateinit var email: String
    private lateinit var date: String
    private lateinit var time: String
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

       val bundle = intent.extras

        if (bundle != null) {
            name = bundle.getString("name")!!
            address = bundle.getString("address")!!
            email = bundle.getString("email")!!
            type = bundle.getString("type")!!
            date = bundle.getString("date")!!
            time = bundle.getString("time")!!
        }

        binding.customerdet.text.toString()
        binding.servicedet.text.toString()
        binding.name.setText(name)
        binding.email.setText(email)
        binding.address.setText(address)
        binding.type.setText(type)
        binding.date.setText(date)
        binding.time.setText(time)

    }
}