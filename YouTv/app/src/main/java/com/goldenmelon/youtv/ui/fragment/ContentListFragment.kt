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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.application.App
import com.goldenmelon.youtv.ui.adapter.ContentItemRecyclerViewAdapter
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.preference.Prefs
import com.goldenmelon.youtv.viewmodel.*
import kotlinx.android.synthetic.main.fragment_content_list.*
import kotlinx.android.synthetic.main.fragment_content_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ContentListFragment.OnListFragmentInteractionListener] interface.
 */
class ContentListFragment : Fragment() {
    private val items = mutableListOf<Content>()

    lateinit var viewModel: ContentListViewModel

    private var listener: OnListFragmentInteractionListener? = null

    //preference
    //LatestSearchWord
    private val prefs: Prefs by lazy {
        App.prefs!!
    }

    //채널 페이지 URL
    var channelWebpage: String? = null

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
            (inflater.inflate(
                R.layout.fragment_content_list,
                container,
                false
            ) as SwipeRefreshLayout).apply {
                setOnRefreshListener {
                    refreshData()
                    isRefreshing = false
                }
            }

        // set the adapter
        with(view.list) {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ContentItemRecyclerViewAdapter(
                    items,
                    listener
                )

            //하단 진입 리스너
            addOnScrollListener(
                object : OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        if (!canScrollVertically(1)) {
                            moreData()
                            listener?.onReachBottom()
                        }
                    }
                }
            )
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        loadData()
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun observeData() {
        viewModel.contents.observe(this,
            Observer<List<Content>> {
                if (it.isEmpty()) {
                    items.clear()
                } else {
                    it.filterNot {
                        items.contains(it)
                    }.forEach {
                        items.add(it)
                    }
                }

                list.adapter?.notifyDataSetChanged()
                listener?.onUpdated()
            })
    }

    private fun loadData() {
        when (viewModel) {
            is MainListViewModel -> {
                viewModel.loadContents()
            }
            is SearchListViewModel -> {
                viewModel.loadContents(prefs.getLatestSearchWord())
            }
            is ChannelListViewModel -> {
                channelWebpage?.let {
                    viewModel.loadContents(it)
                }
            }
        }
    }

    fun refreshData() {
        when (viewModel) {
            is MainListViewModel -> {
                if(viewModel.contents.value.isNullOrEmpty()) {
                    viewModel.loadContents()
                }
            }
            is SearchListViewModel -> {
                viewModel.refresh(prefs.getLatestSearchWord())

            }
            is ChannelListViewModel -> {
                channelWebpage?.let {
                    viewModel.refresh(it)
                }
            }
        }
    }

    fun moreData() {
        when (viewModel) {
            is MainListViewModel -> {
                viewModel.loadContents()
            }
        }
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
