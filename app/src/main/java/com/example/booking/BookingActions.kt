package com.example.booking

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
import com.example.booking.type.CreateBookingInput
import com.example.booking.type.UpdateBookingInput

class BookingActions : AppCompatActivity() {

    private lateinit var binding: ActivityBookingActionsBinding

    private lateinit var client: ApolloClient

    private lateinit var activityAction: String
    private lateinit var id: String
    private lateinit var name: String

    private lateinit var booking: BookingListQuery.Booking

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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                result.text = "Please select an option"
            }
        }

        //insert or update the booking based on the action
        binding.btnSave.setOnClickListener{
            when(activityAction){
                "add" -> createBooking()
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

        //get intent data
        val data = intent
        activityAction = data.action!!
        if (activityAction == "update"){
            id = data.getStringExtra("id")!!
            name = data.getStringExtra("name")!!
            updateBooking()
            Log.d("UpdateBookingId", "Success ${id}")
            Log.d("UpdateBookingName", "Success ${name}")
        }
    }

        private fun createBooking(){
            lifecycleScope.launchWhenResumed {
                val email = binding.etEmail.text.toString()
                val name = binding.etName.text.toString()
                val address = binding.etAddress.text.toString()
                val date = binding.etDate.text.toString()

                val result = try {
                   // val type = binding.etType
                    client.mutation(CreateBookingMutation(
                        input = CreateBookingInput(type = BookingType.safeValueOf(result.text.toString()), email = email, name = name, address = address
                            , serviceDate = date)
                    )
                    ).execute()
                }catch (e: ApolloException){
                    Toast.makeText(this@BookingActions, R.string.protocol_error, Toast.LENGTH_LONG).show()
                    return@launchWhenResumed
                }

                val inputType = result.data?.createBooking?.type
                val inputEmail = result.data?.createBooking?.email
                val inputName = result.data?.createBooking?.name
                val inputAddress = result.data?.createBooking?.address
                val inputDate = result.data?.createBooking?.serviceDate

                val anyElementNull = listOf(inputType,inputEmail,inputName,inputAddress,inputDate)

                //handle errors if needed
                if (result.hasErrors()){
                    val message = result.errors?.get(0)?.message
                    Toast.makeText(this@BookingActions, message, Toast.LENGTH_LONG).show()
                    return@launchWhenResumed
                }else {
                    if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name) &&
                                !TextUtils.isEmpty(address) && !TextUtils.isEmpty(date)){
                        setResult(RESULT_OK)
                        Toast.makeText(this@BookingActions, "success", Toast.LENGTH_LONG).show()
                        finish()
                    }else {
                        Toast.makeText(this@BookingActions, R.string.create_error, Toast.LENGTH_LONG).show()
                    }
                }
            }

        }

    private fun updateBooking(){
        lifecycleScope.launchWhenResumed {
            //val type = binding.etType.BookingType
            val email = binding.etEmail.text.toString()
            val name = binding.etName.text.toString()
            val address = binding.etAddress.text.toString()
            val date = binding.etDate.text.toString()

            val result = try {
                client.mutation(UpdateBookingMutation(
                    id,
                    input = UpdateBookingInput(
                        type = Optional.presentIfNotNull(BookingType.Housekeeping), email = Optional.presentIfNotNull(email),
                        name = Optional.presentIfNotNull(name), address = Optional.presentIfNotNull(address)
                        ,serviceDate = Optional.presentIfNotNull(date))
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
                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(name) ||
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
            val email = binding.etEmail.text.toString()
            val name = binding.etName.text.toString()
            val address = binding.etAddress.text.toString()
            val date = binding.etDate.text.toString()

            val result = try {
                client.mutation(CancelBookingMutation(id)).execute()
            }catch (e: ApolloException){
                Toast.makeText(this@BookingActions, R.string.protocol_error, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }

            val inputType = result.data?.cancelBooking?.type
            val inputEmail = result.data?.cancelBooking?.email
            val inputName = result.data?.cancelBooking?.name
            val inputAddress = result.data?.cancelBooking?.address
            val inputDate = result.data?.cancelBooking?.serviceDate

            val anyElementNull = listOf(inputType,inputEmail,inputName,inputAddress,inputDate)

            //handle errors if needed
            if (result.hasErrors()){
                val message = result.errors?.get(0)?.message
                Toast.makeText(this@BookingActions, message, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }else {
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name) &&
                            !TextUtils.isEmpty(address) && !TextUtils.isEmpty(date)){
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


