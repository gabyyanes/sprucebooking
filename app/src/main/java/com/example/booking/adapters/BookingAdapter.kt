package com.example.booking.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.booking.BookingListQuery
import com.example.booking.R
import com.example.booking.databinding.ItemDetailsBinding

@SuppressLint("NotifyDataSetChanged")
class BookingAdapter(listener: ClickListener) : RecyclerView.Adapter<BookingAdapter.BookingHolder>() {

    private lateinit var context: Context
    private var selectedItemPosition: Int

    //BookingListQuery.Booking is a typesafe generated model from BookingList.graphql query
    private var bookings: ArrayList<BookingListQuery.Booking>

    private val listener: ClickListener

    init {
        selectedItemPosition = -1
        this.bookings = ArrayList()
        this.listener = listener
    }

    //set the list of bookings
    fun setBookings(bookings: ArrayList<BookingListQuery.Booking>){
        this.bookings = ArrayList(bookings)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingHolder {
        val binding = ItemDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        this.context = parent.context
        return BookingHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: BookingHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener {
            selectedItemPosition = holder.adapterPosition
            notifyDataSetChanged()
            listener.onItemClickListener(position)
        }
    }

    override fun getItemCount() = bookings.size

    inner class BookingHolder(private val binding: ItemDetailsBinding): RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(position: Int) {

            //use the bookings property to bind items to the adapter
            val booking = bookings[position]

            binding.tvName.text = booking.name

            binding.tvAddress.text = booking.address

            binding.tvName.setTextColor(context.getColor(
                if (selectedItemPosition == position) R.color.black else android.R.color.darker_gray
            ))

        }
    }

    //detect item click
    interface ClickListener {
        fun onItemClickListener(position: Int)
    }

}