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
//Responsible for displaying the data from the model into a row in the recycler view
class BookingAdapter(listener: ClickListener, onLongClickListener: OnLongClickListener) : RecyclerView.Adapter<BookingAdapter.BookingHolder>() {

    private lateinit var context: Context
    private var selectedItemPosition: Int

    //BookingListQuery.Booking is a typesafe generated model from BookingList.graphql query
    private var bookings: ArrayList<BookingListQuery.Booking>

    private val listener: ClickListener

    private val onLongClickListener: OnLongClickListener

    init {
        selectedItemPosition = -1
        this.bookings = ArrayList()
        this.listener = listener
        this.onLongClickListener = onLongClickListener
    }

    //set the list of bookings
    fun setBookings(bookings: ArrayList<BookingListQuery.Booking>){
        this.bookings = ArrayList(bookings)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingHolder {
        // Use layout inflator to inflate a view
        val binding = ItemDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        this.context = parent.context

        //wrap it inside a ViewHolder and return it
        return BookingHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    //responsible for binding data to a particular view holder
    override fun onBindViewHolder(holder: BookingHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener {
            selectedItemPosition = holder.adapterPosition
            notifyDataSetChanged()
            listener.onItemClickListener(position)
        }

        holder.itemView.setOnLongClickListener() {
            selectedItemPosition = holder.adapterPosition
            notifyDataSetChanged()

            //Notify the listener which position was long pressed.
            onLongClickListener.onLongItemClickListener(position)
            true
        }
    }

    //Tells the RV how many items are in the list
    override fun getItemCount() = bookings.size

    //Container to provide easy access to views that represent each row of the list
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

    //detect long click
    interface OnLongClickListener {
        fun onLongItemClickListener(position: Int)
    }

}
