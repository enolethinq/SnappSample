package com.najand.snappsample.data.network

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.najand.snappsample.data.model.PlacesResult
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

object Repository{
//    private var job: CompletableJob? = null
//    suspend fun getSearchResult(currentLatLng: LatLng, place: String): PlacesResult {
//        var value: PlacesResult
//        job?.let {
//            CoroutineScope(IO + it).launch {
//                val result = SearchApiClient.searchService.fetchSearchResult("service.DiScDu8gelClrjE1xDMrgFvXMpfaiVtA4COaTnpe",
//                        place, currentLatLng.latitude, currentLatLng.longitude)
//                withContext(Main) {
//                    value = result
//                    it.complete()
//                }
//            }
//        }
//
//    }
//
//    fun cancelJob(){
//        job?.cancel()
//    }
}