package com.dlala.stream
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class MovieAdapter(private val onClick: (Movie) -> Unit) :
    RecyclerView.Adapter<MovieAdapter.VH>() {

    private var items: List<Movie> = emptyList()

    fun submit(list: List<Movie>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val poster: ImageView = view.findViewById(R.id.movie_poster)
        val title: TextView = view.findViewById(R.id.movie_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val movie = items[position]
        holder.title.text = movie.title

        holder.poster.setImageResource(R.drawable.ic_logo)

        if (!movie.posterUrl.isNullOrBlank()) {
            holder.poster.load(movie.posterUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_logo)
                error(R.drawable.ic_logo)
            }
        }

        holder.itemView.setOnClickListener {
            onClick(movie)
        }
    }

    override fun getItemCount(): Int = items.size
}
