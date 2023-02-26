package com.makskor.livesearchandroidtestapp

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat

import org.junit.After
import org.junit.Before
import org.junit.Test

class NetworkUtilsTest {
    private var appContext: Context? = null

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @After
    fun tearDown() {
        appContext = null
    }

    @Test
    fun isNetworkAvailable_networkEnabled_returnsTrue() {
        val networkIsAvailable = NetworkUtils.isNetworkAvailable(appContext!!)
        assertThat(networkIsAvailable).isTrue()
    }
}