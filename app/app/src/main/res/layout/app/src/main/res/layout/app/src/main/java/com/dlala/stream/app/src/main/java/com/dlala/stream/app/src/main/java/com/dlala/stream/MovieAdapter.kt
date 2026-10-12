package com.dlala.stream

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MovieAdapter(private val onClick: (Movie) -> Unit) :
    RecyclerView.Adapter<MovieAdapter.VH>() {

    private var items: List<Movie> = emptyList()

    fun submit(list: List<Movie>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false) as TextView)

    override fun onBindViewHolder(holder: VH, position: Int) {
        val movie = items[position]
        holder.tv.text = movie.title
        holder.tv.setOnClickListener { onClick(movie) }
    }

    override fun getItemCount() = items.size
}
