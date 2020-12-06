package com.goldenmelon.youtv.ui.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.application.App
import com.goldenmelon.youtv.ui.adapter.ContentItemRecyclerViewAdapter
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.preference.Prefs
import com.goldenmelon.youtv.viewmodel.ChannelViewModel
import com.goldenmelon.youtv.viewmodel.ContentViewModel
import com.goldenmelon.youtv.viewmodel.SearchContentViewModel
import kotlinx.android.synthetic.main.fragment_content_list.*
import kotlinx.android.synthetic.main.fragment_content_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ContentListFragment.OnListFragmentInteractionListener] interface.
 */

sealed class ContentListType {
    object Main : ContentListType()
    object Search : ContentListType()
    object Channel : ContentListType()
}

class ContentListFragment : Fragment() {
    internal lateinit var type: ContentListType

    private val items = mutableListOf<Content>()
    private var listener: OnListFragmentInteractionListener? = null

    private val viewModel: AndroidViewModel by lazy {
        when (type) {
            is ContentListType.Main -> {
                ViewModelProviders.of(activity!!).get(ContentViewModel::class.java)
            }
            is ContentListType.Search -> {
                ViewModelProviders.of(activity!!).get(SearchContentViewModel::class.java)
            }
            is ContentListType.Channel -> {
                ViewModelProviders.of(activity!!).get(ChannelViewModel::class.java)
            }
        }
    }

    //preference
    private val prefs: Prefs by lazy {
        App.prefs!!
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // set Listener
        if (context is OnListFragmentInteractionListener) {
            listener = context
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate recyclerview
        val view =
            inflater.inflate(R.layout.fragment_content_list, container, false) as SwipeRefreshLayout

        view.setOnRefreshListener {
            when (type) {
                is ContentListType.Main -> {
                    (viewModel as ContentViewModel).let {
                        it.clearContents()
                        it.loadContents()
                    }
                }
                is ContentListType.Search -> {
                    (viewModel as SearchContentViewModel).let {
                        it.clearContents()
                        it.loadContents(prefs.getLatestSearchWord())
                    }
                }
                is ContentListType.Channel -> {
                    channelWebpage?.let {
                        (viewModel as ChannelViewModel).clearContents()
                        (viewModel as ChannelViewModel).loadContents(it)
                    }
                }
            }

            view.isRefreshing = false
        }


        // set the adapter
        with(view.list) {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ContentItemRecyclerViewAdapter(
                    items,
                    listener
                )

            addOnScrollListener(
                object : OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        if (!canScrollVertically(1)) {
                            listener?.onReachBottom()
                        }
                    }
                }
            )
        }

        return view
    }

    public var channelWebpage: String? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val contents = when (type) {
            is ContentListType.Main -> {
                (viewModel as ContentViewModel).getContents()
            }
            is ContentListType.Search -> {
                (viewModel as SearchContentViewModel).getContents(prefs.getLatestSearchWord())
            }
            is ContentListType.Channel -> {
                channelWebpage?.let {
                    (viewModel as ChannelViewModel).getContents(it)
                }
            }
        }

        contents?.observe(this,
            Observer<List<Content>> {
                if (it.isEmpty()) {
                    items.clear()
                } else {
                    for (content in it) {
                        if (!items.contains(content)) {
                            items.add(content)
                        }
                    }
                }

                (/*view as RecyclerView*/list).adapter?.notifyDataSetChanged()
                listener?.onUpdated()
            })
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        fun onItemClick(item: Content)
        fun onChannelInItemClick(item: Content)
        fun onReachBottom()
        fun onUpdated()
        fun onMenuInItemClick(v: View, item: Content)
    }

    companion object {
        const val TAG = "ItemFragment"
    }
}
