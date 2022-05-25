package com.example.booking

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.example.booking.adapters.BookingAdapter
import com.example.booking.databinding.ActivityMainBinding

@SuppressLint("NotifyDataSetChanged")
class MainActivity : AppCompatActivity(), BookingAdapter.ClickListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var client: ApolloClient

    private lateinit var arrayList: ArrayList<BookingListQuery.Booking>
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = Apollo().get()

        adapter = BookingAdapter(this)

        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this@MainActivity,
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.rvData.layoutManager = mLayoutManager
        binding.rvData.adapter = adapter

        binding.btnAdd.setOnClickListener {
            val add = Intent(this, BookingActions::class.java)
            add.action = "add"
            activityResult.launch(add)
        }


        getBookingList()

    }

    //get the booking list
    private fun getBookingList() {
        lifecycleScope.launchWhenResumed {

            //Use the apolloClient and the generated BookingListQeury to execute a query
            val response = try {
                client.query(BookingListQuery()).execute()

            } catch (e: ApolloException) {
                binding.tvLoading.text = resources.getString(R.string.protocol_error)
                binding.tvLoading.visibility = View.VISIBLE
                return@launchWhenResumed
            }

            Log.d("MainActivity", "Success ${response.data}")

            val bookings = response?.data?.bookings?.bookings
            if (bookings == null || response.hasErrors()) {
                binding.tvLoading.text = response.errors?.get(0)?.message
                binding.tvLoading.visibility = View.VISIBLE
                return@launchWhenResumed
            } else {
                arrayList = ArrayList(bookings)
                adapter.setBookings(arrayList)
                if (arrayList.isEmpty()) {
                    binding.tvLoading.visibility = View.VISIBLE
                    binding.tvLoading.text = resources.getString(R.string.no_data)
                } else {
                    binding.tvLoading.visibility = View.GONE
                }
            }
        }
    }

    //detect item click from recycler view
    override fun onItemClickListener(position: Int) {
        val update = Intent(this, BookingActions::class.java)
        update.action = "update"
        update.putExtra("id", arrayList[position].id)
        update.putExtra("name",arrayList[position].name)
        activityResult.launch(update)
    }

    //refresh booking list
    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                getBookingList()
            }
        }

}

