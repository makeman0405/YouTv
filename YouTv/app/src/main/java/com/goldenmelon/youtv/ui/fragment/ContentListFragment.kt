package com.goldenmelon.youtv.ui.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.goldenmelon.youtv.application.App
import com.goldenmelon.youtv.databinding.FragmentContentListBinding
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.preference.Prefs
import com.goldenmelon.youtv.ui.adapter.ContentItemRecyclerViewAdapter
import com.goldenmelon.youtv.viewmodel.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ContentListFragment.OnListFragmentInteractionListener] interface.
 */
class ContentListFragment : Fragment() {

    private var _binding: FragmentContentListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
    ): View {
        _binding = FragmentContentListBinding.inflate(inflater, container, false)

        //refresh event
        binding.root.run {
            setOnRefreshListener {
                refreshData()
                isRefreshing = false
            }
        }

        // set the adapter
        with(binding.list) {
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
                        }
                    }
                }
            )
        }

        return binding.root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun moreData() {
        when (viewModel) {
            is MainListViewModel -> {
                viewModel.loadContents()
                listener?.onReachBottom()
            }
        }
    }

    private fun observeData() {
        viewModel.contents.observe(this, { list ->
            if (list.isEmpty()) {
                items.clear()
            } else {
                for (item in list) {
                    if (!items.contains(item)) {
                        items.add(item)
                    }
                }
            }

            _binding?.list?.adapter?.notifyDataSetChanged()
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
                if (viewModel.contents.value.isNullOrEmpty()) {
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
