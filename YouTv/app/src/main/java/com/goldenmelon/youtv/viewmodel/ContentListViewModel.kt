package com.goldenmelon.youtv.viewmodel

import androidx.lifecycle.MutableLiveData
import com.goldenmelon.youtv.datas.Content

interface ContentListViewModel {
    val contents: MutableLiveData<MutableList<Content>>

    fun loadContents(param: String? = null)
    fun clearContents() {
        contents.let {
            it.value = mutableListOf<Content>()
        }
    }
    fun refresh(param: String? = null)
}
