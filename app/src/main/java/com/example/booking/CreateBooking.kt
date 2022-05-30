package com.example.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.example.booking.databinding.ActivityCreateBookingBinding
import com.example.booking.type.BookingType
import com.example.booking.type.CreateBookingInput
import java.util.*

class CreateBooking : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    private lateinit var binding: ActivityCreateBookingBinding

    private lateinit var client: ApolloClient

    private lateinit var activityAction: String

    lateinit var option: Spinner
    lateinit var result: TextView

    lateinit var date: EditText

    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    var savedHour = 0
    var savedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = Apollo().get()

        binding.etEmail.addTextChangedListener(createWatcher)
        binding.etName.addTextChangedListener(createWatcher)
        binding.etAddress.addTextChangedListener(createWatcher)
        binding.etDate.addTextChangedListener(createWatcher)

        date = binding.etDate

        option = binding.spOptions
        result = binding.tvResult

        val options = arrayOf(BookingType.Housekeeping.toString(),BookingType.DogWalk.toString())

        option.adapter = ArrayAdapter<String>(this@CreateBooking, android.R.layout.simple_list_item_1,
            options)

        option.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                result.text = options[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                result.text = "Please select an option"
            }
        }

        pickDate()

        //insert the booking based on the action
            binding.btnCreate.setOnClickListener{
                when(activityAction){
                    "add" -> createBooking()
                }
           }

        //get intent data
          val data = intent
          activityAction = data.action!!
    }

    private fun getDateTimeCalendar() {
        val cal:Calendar = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        binding.etDate.setOnClickListener {
            getDateTimeCalendar()

            DatePickerDialog(this,this,year,month,day).show()
        }

    }
    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year

        getDateTimeCalendar()

        TimePickerDialog(this,this, hour, minute,true).show()
    }

    override fun onTimeSet(p0: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        date.setText("$savedDay-$savedMonth-$savedYear.T$savedHour:$savedMinute")
    }

    private fun createBooking(){

        lifecycleScope.launchWhenResumed {
            val email = binding.etEmail.text.toString()
            val name = binding.etName.text.toString()
            val address = binding.etAddress.text.toString()
            val date = date.text.toString()

            val result = try {
                client.mutation(CreateBookingMutation(
                    input = CreateBookingInput(address = address, email = email, name = name
                        , serviceDate = date, type = BookingType.safeValueOf(result.text.toString())))
                ).execute()
            }catch (e: ApolloException){
                Toast.makeText(this@CreateBooking, R.string.protocol_error, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }

            //handle errors if needed
            if (result.hasErrors()){
                val message = result.errors?.get(0)?.message
                Toast.makeText(this@CreateBooking, message, Toast.LENGTH_LONG).show()
                return@launchWhenResumed
            }else {
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name) &&
                    !TextUtils.isEmpty(address) && !TextUtils.isEmpty(date)){
                    setResult(RESULT_OK)
                    Toast.makeText(this@CreateBooking, "success", Toast.LENGTH_LONG).show()
                    finish()
                }else {
                    Toast.makeText(this@CreateBooking, R.string.create_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private val createWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val inputEmail = binding.etEmail.text.toString()
            val inputName = binding.etName.text.toString()
            val inputAddress = binding.etAddress.text.toString()
            val inputDate = date.text.toString()

            binding.btnCreate.isEnabled = !inputEmail.isEmpty() && !inputName.isEmpty() &&
                    !inputAddress.isEmpty()  && !inputDate.isEmpty()
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

}