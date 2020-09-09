package com.goldenmelon.youtv.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.goldenmelon.youtv.ui.fragment.ContentListFragment.OnListFragmentInteractionListener
import com.goldenmelon.youtv.databinding.FragmentContentListItemBinding
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.utils.loadImage
import kotlinx.android.synthetic.main.fragment_content_list_item.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class ContentItemRecyclerViewAdapter(
    private val values: List<Content>,
    private val listener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<ContentItemRecyclerViewAdapter.ViewHolder>() {

    //item click listener
    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val item = v.tag as Content
        // Notify the active callbacks interface (the activity, if the fragment is attached to
        // one) that an item has been selected.
        listener?.onItemClick(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //create ViewHolder & return ViewHoler
        val binding =
            FragmentContentListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        //set holder
        holder.binding.item = item

        //set View'Tag & listener(추가 Study: let, run, with, apply, and also)
        with(holder.binding.root) {
            //set data in View'tag
            tag = item
            setOnClickListener(onClickListener)
        }

        //load thumbnail & display
        holder.thumbnail.loadImage(item.thumbnail!!, RequestOptions().centerCrop())

        holder.channelThumbnail.visibility = View.GONE
        item.channelThumbnail?.let {
            holder.channelThumbnail.visibility = View.VISIBLE
            holder.channelThumbnail.loadImage(
                it,
                RequestOptions().circleCrop()
            )
            //channel
            holder.channelThumbnail.setOnClickListener {
                listener?.onChannelInItemClick(item)
            }
        }

        //check live
        if (item.lengthText.isNullOrBlank() || item.lengthText == "null") {
            holder.live.visibility = View.VISIBLE
        } else {
            holder.live.visibility = View.INVISIBLE
        }

        //즉시 업데이트
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(val binding: FragmentContentListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val thumbnail: ImageView = binding.itemThumbnail
        val channelThumbnail: ImageView = binding.itemChannelThumbnail
        val live: TextView = binding.itemLive
    }
}
