package com.goldenmelon.youtv.viewmodel

import androidx.lifecycle.LiveData
import com.goldenmelon.youtv.datas.Content

interface ContentListViewModel {
    fun getContents(param: String? = null): LiveData<MutableList<Content>>?
    fun loadContents(param: String? = null)
    fun clearContents()
}
