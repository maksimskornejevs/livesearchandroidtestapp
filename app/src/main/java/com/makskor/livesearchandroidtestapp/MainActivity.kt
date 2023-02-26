package com.makskor.livesearchandroidtestapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.makskor.livesearchandroidtestapp.api.ApiClient
import com.makskor.livesearchandroidtestapp.api.GifImageSearchService
import com.makskor.livesearchandroidtestapp.databinding.ActivityMainBinding
import com.makskor.livesearchandroidtestapp.models.GifImage
import com.makskor.livesearchandroidtestapp.models.GifImageSearchResult
import kotlinx.coroutines.Runnable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : AppCompatActivity(), Runnable {
    private lateinit var binding: ActivityMainBinding
    val viewModel: MainActivityViewModel by viewModels()
    private var searchJobHandler: Handler? = null
    private var isLoadInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initSearchResultsListRecylcerView()
        initInternalEventObservers()
        initUserActionObservers()
    }

    private fun initUserActionObservers() {
        binding.tvSearchBarInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val currentSearchTerm: String? = viewModel.searchTerm.value
                val editedSearchTerm: String = s.toString()
                if (editedSearchTerm.isNotEmpty() && currentSearchTerm != editedSearchTerm) {
                    viewModel.searchTermEvent.value = LiveDataEvent(s.toString())
                }
            }
        })
    }


    private fun initSearchResultsListRecylcerView() {

        if(viewModel.searchResultsRecyclerAdapter.value == null) {
            viewModel.searchResultsRecyclerAdapter.value = SearchListRecyclerAdapter()
            viewModel.searchResultsRecyclerAdapter.value!!.stateRestorationPolicy =
                StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        binding.rwGifImagesSearchResults.layoutManager =
            GridLayoutManager(this, RECYCLER_VIEW_COLUMNS)
        binding.rwGifImagesSearchResults.adapter = viewModel.searchResultsRecyclerAdapter.value

        binding.rwGifImagesSearchResults.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = (recyclerView.layoutManager as GridLayoutManager)
                var searchTerm: String? = null
                var searchResult: GifImageSearchResult? = null


                viewModel.searchTermEvent.value?.let {
                    it.peekContent().let {
                        searchTerm = it
                    }
                }

                viewModel.searchResult.value?.let {
                    it.peekContent().let {
                        searchResult = it
                    }
                }


                if (!isLoadInProgress &&
                    searchTerm != null &&
                    searchTerm!!.isNotBlank() &&
                    searchResult != null &&
                    searchResult!!.pagination.totalCount > searchResult!!.pagination.offset &&
                    layoutManager.findLastCompletelyVisibleItemPosition() >= viewModel.searchResultsRecyclerAdapter.value?.itemCount!! - LOAD_MORE_ITEMS_OFFSET
                ) {
                    val itemsCountInAdapter: Int =
                        viewModel.searchResultsRecyclerAdapter.value?.itemCount!!
                    val leftToLoadGifs =
                        searchResult!!.pagination.totalCount - itemsCountInAdapter


                    when {
                        leftToLoadGifs >= SEARCH_RESULTS_PER_PAGE -> {
                            loadSearchResults(
                                searchTerm!!,
                                itemsCountInAdapter + SEARCH_RESULTS_PER_PAGE
                            )
                        }
                        else -> {
                            loadSearchResults(
                                searchTerm!!,
                                itemsCountInAdapter + leftToLoadGifs - 1
                            )
                        }
                    }
                }
            }
        })
    }

    private fun processSearchResults(searchResult: GifImageSearchResult): List<GifImage> {
        return searchResult.results.map {
            it.sizedImage.originalGifImage
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initInternalEventObservers() {
        viewModel.searchResult.observe(this) {
            it.getContentIfNotHandled()?.let {
                val itemsCountInAdapter: Int =
                    viewModel.searchResultsRecyclerAdapter.value?.itemCount!!
                val gifImageList: List<GifImage> = processSearchResults(it)

                if (it.pagination.offset != 0) {
                    viewModel.searchResultsRecyclerAdapter.value?.appendList(gifImageList)
                    viewModel.searchResultsRecyclerAdapter.value?.notifyItemRangeInserted(
                        itemsCountInAdapter,
                        itemsCountInAdapter + gifImageList.size - 1
                    )
                } else {
                    viewModel.searchResultsRecyclerAdapter.value?.setList(gifImageList)
                    viewModel.searchResultsRecyclerAdapter.value?.notifyDataSetChanged()
                }
            }
        }

        viewModel.searchTermEvent.observe(this) {
            it.getContentIfNotHandled()?.let {
                viewModel.searchTerm.value = it
                searchJobHandler?.removeCallbacks(this)
                launchSearchHandler()
            }
        }
    }

    private fun launchSearchHandler() {
        searchJobHandler = Handler(Looper.getMainLooper())
        searchJobHandler?.postDelayed(this@MainActivity, SEARCH_REQUEST_DELAY)
    }

    override fun run() {
        viewModel.searchTermEvent.value?.let {
            it.peekContent().let {
                loadSearchResults(it, 0)
            }
        }
    }

    private fun loadSearchResults(searchTerm: String, offset: Int) {
        if (!NetworkUtils.isNetworkAvailable(this@MainActivity)) {
            Toast.makeText(
                this@MainActivity,
                "Please connect to WIFI or enable Mobile network to search GIFs",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (NetworkUtils.isNetworkAvailable(this@MainActivity) && searchTerm.isNotBlank()) {
            isLoadInProgress = true
            val retrofit: Retrofit = ApiClient.getInstance()
            val service: GifImageSearchService =
                retrofit.create<GifImageSearchService>(GifImageSearchService::class.java)
            val searchApiCall: Call<GifImageSearchResult> = service.getResponse(
                apiKey = API_KEY,
                limit = SEARCH_RESULTS_PER_PAGE,
                searchTerm = searchTerm,
                offset = offset
            )

            searchApiCall.enqueue(object : Callback<GifImageSearchResult> {
                override fun onResponse(
                    call: Call<GifImageSearchResult>,
                    response: Response<GifImageSearchResult>
                ) {
                    if (response.isSuccessful) {
                        val dataResponse: GifImageSearchResult? = response.body()

                        if (dataResponse != null) {
                            viewModel.searchResult.value = LiveDataEvent(dataResponse)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "There is no results for you search term. Please try to search for something else",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    } else {
                        Toast.makeText(this@MainActivity, "Internal Error", Toast.LENGTH_SHORT)
                            .show()
                    }

                    isLoadInProgress = false
                }

                override fun onFailure(call: Call<GifImageSearchResult>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Internal Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    companion object {
        const val API_KEY: String = ""
        const val SEARCH_RESULTS_PER_PAGE: Int = 50
        const val SEARCH_REQUEST_DELAY: Long = 300
        const val LOAD_MORE_ITEMS_OFFSET: Int = 13
        const val RECYCLER_VIEW_COLUMNS: Int = 2
    }
}