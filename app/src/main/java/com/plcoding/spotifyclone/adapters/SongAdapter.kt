package com.plcoding.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.plcoding.spotifyclone.R
import com.plcoding.spotifyclone.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>(){

    private val diffCallback = object : DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)




    class SongViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder( LayoutInflater.from(parent.context).inflate(
            R.layout.list_item,
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.author
            glide.load(song.imageUrl).into(ivItemImage)

            setOnItemClickListener {
                onItemClickListener?.let{
                    click ->
                    click(song)
                }
            }

        }
    }


    private var onItemClickListener: ((Song) -> Unit)? = null

    fun setOnItemClickListener(listener: (Song) -> Unit){
        onItemClickListener = listener
    }


    override fun getItemCount(): Int {
        return songs.size
    }
}



