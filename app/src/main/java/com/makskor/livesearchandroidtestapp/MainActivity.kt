package com.makskor.livesearchandroidtestapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
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
    private var searchResultsRecyclerAdapter: SearchListRecyclerAdapter? = null
    private var searchJobHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initSearchResultsListRecylcerView()
        initInternalEventObservers()
    }


    private fun initSearchResultsListRecylcerView() {
        searchResultsRecyclerAdapter = SearchListRecyclerAdapter()
        binding.rwGifImagesSearchResults.layoutManager = GridLayoutManager(this, 2)
        binding.rwGifImagesSearchResults.adapter = searchResultsRecyclerAdapter
    }

    private fun processSearchResults(searchResult: GifImageSearchResult): List<GifImage> {
        return searchResult.results.map {
            it.sizedImage.originalGifImage
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initInternalEventObservers() {
        viewModel.searchResult.observe(this, {
            searchResultsRecyclerAdapter?.setList(processSearchResults(it))
            searchResultsRecyclerAdapter?.notifyDataSetChanged()
        })

        viewModel.searchTerm.observe(this, {
            if(it.isNotBlank()) {
                searchJobHandler?.removeCallbacks(this)
                launchSearchHandler()
            }
        })
    }

    private fun launchSearchHandler() {
        searchJobHandler = Handler(Looper.getMainLooper())
        searchJobHandler?.postDelayed(this@MainActivity, SEARCH_REQUEST_DELAY)
    }

    override fun run() {
        viewModel.searchTerm.value?.let {

            if (!NetworkUtils.isNetworkAvailable(this@MainActivity)) {
                Toast.makeText(
                    this@MainActivity,
                    "Please connect to WIFI or enable Mobile network to search GIFs",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            if (NetworkUtils.isNetworkAvailable(this@MainActivity) && it.isNotBlank()) {
                val retrofit: Retrofit = ApiClient.getInstance()
                val service: GifImageSearchService =
                    retrofit.create<GifImageSearchService>(GifImageSearchService::class.java)
                val searchApiCall: Call<GifImageSearchResult> = service.getResponse(
                    apiKey = API_KEY,
                    limit = SEARCH_RESULTS_PER_PAGE,
                    searchTerm = it
                )

                searchApiCall.enqueue(object : Callback<GifImageSearchResult> {
                    override fun onResponse(
                        call: Call<GifImageSearchResult>,
                        response: Response<GifImageSearchResult>
                    ) {
                        if (response.isSuccessful) {
                            val dataResponse: GifImageSearchResult? = response.body()

                            if (dataResponse != null) {
                                viewModel.searchResult.value = dataResponse
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
                    }

                    override fun onFailure(call: Call<GifImageSearchResult>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Internal Error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    companion object {
        const val API_KEY: String = ""
        const val SEARCH_RESULTS_PER_PAGE: Int = 20
        const val SEARCH_REQUEST_DELAY: Long = 300
    }
}