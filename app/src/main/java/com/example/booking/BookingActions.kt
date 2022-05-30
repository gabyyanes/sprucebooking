package com.example.booking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.booking.databinding.ActivityBookingActionsBinding
import com.example.booking.type.BookingType
import com.example.booking.type.UpdateBookingInput

class BookingActions : AppCompatActivity() {

    private lateinit var binding: ActivityBookingActionsBinding

    private lateinit var client: ApolloClient

    private lateinit var activityAction: String

    private lateinit var id: String
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var address: String
    private lateinit var serviceDate: String
    private lateinit var date: String
    private lateinit var time: String
    private lateinit var type: String

    private lateinit var booking: BookingListQuery.Booking

    //spinner for booking type
    lateinit var option: Spinner
    lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = Apollo().get()

        option = binding.spOptions
        result = binding.tvResult

        val options = arrayOf(BookingType.Housekeeping.toString(),BookingType.DogWalk.toString())

        option.adapter = ArrayAdapter<String>(this@BookingActions, android.R.layout.simple_list_item_1,
            options)

        option.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                result.text = options[position]
                option.setSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                result.text = "Please select an option"
            }
        }

        //update the booking based on the action
        binding.btnSave.setOnClickListener{
            when(activityAction){
                "update" -> updateBooking()
            }
        }

        binding.btnDelete.setOnClickListener {
            val deleteDialog = AlertDialog.Builder(this)
            deleteDialog.setTitle(R.string.app_name)
            deleteDialog.setMessage("cancel booking?")
            deleteDialog.setNegativeButton("NO"){ _, _ ->
                deleteDialog.setCancelable(true)
            }
            deleteDialog.setPositiveButton("YES"){ _, _ ->
                cancelBooking()
            }
            deleteDialog.show()
        }

        //get intent data for the update screen
        val data = intent
        activityAction = data.action!!
        if (activityAction == "update"){
            id = data.getStringExtra("id")!!
            name = data.getStringExtra("name")!!
            email = data.getStringExtra("email")!!
            address = data.getStringExtra("address")!!
            type = data.getStringExtra("type")!!
            serviceDate = data.getStringExtra("serviceDate")!!
            date = data.getStringExtra("date")!!
            time = data.getStringExtra("time")!!

            binding.etName.setText(name)
            binding.etEmail.setText(email)
            binding.etAddress.setText(address)
            binding.tvResult.setText(type)
            binding.etServiceDate.setText(serviceDate)

            Log.d("UpdateBookingId", "Success ${id}")
            Log.d("UpdateBookingName", "Success ${name}")
            Log.d("UpdateBookingType", "Success ${type}")
        }
    }


    private fun updateBooking(){
        lifecycleScope.launchWhenResumed {

            val email = binding.etEmail.text.toString()
            val name = binding.etName.text.toString()
            val address = binding.etAddress.text.toString()
            val serviceDate = binding.etServiceDate.text.toString()

            val result = try {
                client.mutation(UpdateBookingMutation(
                    id,
                    input = UpdateBookingInput(
                        type = Optional.presentIfNotNull(BookingType.safeValueOf(result.text.toString())), email = Optional.presentIfNotNull(email),
                        name = Optional.presentIfNotNull(name), address = Optional.presentIfNotNull(address)
                        ,serviceDate = Optional.presentIfNotNull(serviceDate))
                )
                ).execute()
            }catch (e: ApolloException){
                Toast.makeText(this@BookingActions, R.string.protocol_error, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }

            Log.d("BookingActions", "Success ${result.data}")

            val inputType = result.data?.updateBooking?.type
            val inputEmail = result.data?.updateBooking?.email
            val inputName = result.data?.updateBooking?.name
            val inputAddress = result.data?.updateBooking?.address
            val inputDate = result.data?.updateBooking?.serviceDate

            val anyElementNull = listOf(inputType,inputEmail,inputName,inputAddress,inputDate)

            //handle errors if needed
            if (result.hasErrors()){
                val message = result.errors?.get(0)?.message
                Toast.makeText(this@BookingActions, message, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }else {
                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(name) ||
                    !TextUtils.isEmpty(address) || !TextUtils.isEmpty(date)){
                    setResult(RESULT_OK)
                    Toast.makeText(this@BookingActions, "success", Toast.LENGTH_LONG).show()
                    finish()
                }else {
                    Toast.makeText(this@BookingActions, R.string.create_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cancelBooking(){
        lifecycleScope.launchWhenResumed {

            val result = try {
                client.mutation(CancelBookingMutation(id)).execute()
            }catch (e: ApolloException){
                Toast.makeText(this@BookingActions, R.string.protocol_error, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }

            val inputId = result.data?.cancelBooking?.id

            //handle errors if needed
            if (result.hasErrors()){
                val message = result.errors?.get(0)?.message
                Toast.makeText(this@BookingActions, message, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }else {
                if (inputId != null && inputId.isNotEmpty()){
                    setResult(RESULT_OK)
                    Toast.makeText(this@BookingActions, "success", Toast.LENGTH_LONG).show()
                    finish()
                }else {
                    Toast.makeText(this@BookingActions, R.string.create_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}


