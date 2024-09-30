package com.example.cloudnotify.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudnotify.R
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.databinding.ViewholderBookMarkedBinding



class BookMarkedItemAdapter(
    private val onRemoveClickListener: OnRemoveClickListener,
    private val onCardClickListener: OnCardClickListener
) : RecyclerView.Adapter<BookMarkedItemAdapter.BookMarkItemViewHolder>() {

    private var bookMarkedList = listOf<BookmarkLocation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookMarkItemViewHolder {
        val binding: ViewholderBookMarkedBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.viewholder_book_marked,
            parent,
            false
        )
        return BookMarkItemViewHolder(binding)
    }

    override fun getItemCount() = bookMarkedList.size

    override fun onBindViewHolder(holder: BookMarkItemViewHolder, position: Int) {
        holder.bind(bookMarkedList[position], onRemoveClickListener, onCardClickListener)
    }

    class BookMarkItemViewHolder(val binding: ViewholderBookMarkedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            bookmarkLocation: BookmarkLocation,
            onRemoveClickListener: OnRemoveClickListener,
            onCardClickListener: OnCardClickListener
        ) {
            binding.bookmarkLocation = bookmarkLocation

            // Set click listener for the card
            binding.root.setOnClickListener {
                onCardClickListener.onCardClick(bookmarkLocation)
            }

            // Set click listener for the remove icon
            binding.icoRemove.setOnClickListener {
                onRemoveClickListener.onRemoveClick(bookmarkLocation)
            }

            binding.executePendingBindings()
        }
    }

    fun updateData(newList: List<BookmarkLocation>) {
        bookMarkedList = newList
        notifyDataSetChanged()
    }
}

interface OnRemoveClickListener {
    fun onRemoveClick(bookmarkLocation: BookmarkLocation)
}

interface OnCardClickListener {
    fun onCardClick(bookmarkLocation: BookmarkLocation)
}