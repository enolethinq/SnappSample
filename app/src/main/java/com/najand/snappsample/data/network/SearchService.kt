package com.najand.snappsample.data.network

import com.najand.snappsample.data.model.PlacesResult
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SearchService {
    @GET("search?")
    suspend fun fetchSearchResult(
            @Header("Api-Key") key: String,
            @Query("term") term: String,
            @Query("lat") lat: Double,
            @Query("lng") lng: Double
    ): Response<PlacesResult>
}