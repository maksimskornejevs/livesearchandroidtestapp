package com.makskor.livesearchandroidtestapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.makskor.livesearchandroidtestapp.models.GifImageSearchResult

class MainActivityViewModel: ViewModel() {
    val searchResult = MutableLiveData<LiveDataEvent<GifImageSearchResult>>()
    val searchTermEvent = MutableLiveData<LiveDataEvent<String>>()
    val searchTerm = MutableLiveData<String>()
    val searchResultsRecyclerAdapter = MutableLiveData<SearchListRecyclerAdapter>()
}