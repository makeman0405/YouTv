package com.goldenmelon.youtv.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.goldenmelon.youtv.databinding.FragmentContentListItemBinding
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.ui.fragment.ContentListFragment.OnListFragmentInteractionListener
import com.goldenmelon.youtv.utils.loadImage

class ContentItemRecyclerViewAdapter(
    private val values: List<Content>,
    private val listener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<ContentItemRecyclerViewAdapter.ViewHolder>() {

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
        holder.binding.run {
            viewModel = ContentViewModel(values[position], listener)
            executePendingBindings()
        }
    }

    override fun getItemCount(): Int = values.size

    class ViewHolder(val binding: FragmentContentListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    class ContentViewModel(
        val item: Content,
        private val listener: OnListFragmentInteractionListener?
    ) {
        fun onItemClick(view: View) {
            listener?.onItemClick(item)
        }

        fun onChannelInItemClick(view: View) {
            listener?.onChannelInItemClick(item)
        }

        fun onMenuInItemClick(view: View) {
            listener?.onMenuInItemClick(view, item)
        }

        var liveVislble = View.VISIBLE
        var channelVisible  = View.VISIBLE

        init {
            liveVislble = if (item.lengthText.equals("null")) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

            channelVisible = if (item.channelThumbnail.isNullOrBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}

object BindingAdapter {
    @JvmStatic
    @BindingAdapter(value = ["imageUrl", "supportCircle"], requireAll = false)
    fun loadImage(
        view: ImageView,
        url: String?, supportCircle: Boolean = false
    ) {
        if(view.visibility == View.VISIBLE) {
            val requestOptions =
                if (!supportCircle) RequestOptions().centerInside() else RequestOptions().circleCrop()
            view.loadImage(url, requestOptions)
        }
    }
}




