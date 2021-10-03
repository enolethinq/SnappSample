package com.najand.snappsample.data.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SearchApiClient{
    const val BASE_URL: String = "https://api.neshan.org/v1/"
    val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
    }
    val searchService: SearchService by lazy {
        retrofitBuilder
                .build()
                .create(SearchService::class.java)
    }
}