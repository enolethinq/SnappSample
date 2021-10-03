
package com.najand.snappsample.ui.maps

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.najand.snappsample.data.model.PlacesResult
import com.najand.snappsample.data.network.NetworkService
import com.najand.snappsample.data.network.Repository
import com.najand.snappsample.data.network.SearchApiClient
import com.najand.snappsample.data.network.SearchService
import com.najand.snappsample.simulator.WebSocket
import com.najand.snappsample.simulator.WebSocketListener
import com.najand.snappsample.utils.Constants
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.*
import org.json.JSONObject

class MapsPresenter(private val networkService: NetworkService):WebSocketListener {

    companion object{
        private const val TAG = "MapsPresenter"
    }

    private val compositeDisposable = CompositeDisposable();
    private var view: MapsView? = null
    private lateinit var searchService: SearchService
    //webSocket cannot be null because it will be creating as soon as presenter get attached to views
    private lateinit var webSocket: WebSocket

    fun onAttach(view: MapsView){
        this.view = view
        webSocket = networkService.createWebSocket(this)
        webSocket.connect()
    }

    fun onDetach(){
        webSocket.disconnect()
        view = null
    }

    override fun onConnect() {
        Log.i(TAG, "onConnect: ")
    }

    override fun onMessage(data: String) {
        Log.i(TAG, "onMessage: $data")
        val jsonObject = JSONObject(data)
        Log.i(TAG, "onMessage: "+jsonObject.getString(Constants.TYPE))
        when(jsonObject.getString(Constants.TYPE)){
            Constants.NEAR_BY_CABS->{
                handleOnMessageNearbyCabs(jsonObject)
            }
        }

    }

    private fun handleOnMessageNearbyCabs(jsonObject: JSONObject) {
        Log.i(TAG, "handleOnMessageNearbyCabs: called")
        val nearbyCabsLocations = arrayListOf<LatLng>()
        val jsonArray = jsonObject.getJSONArray(Constants.LOCATIONS)
        Log.i(TAG, "handleOnMessageNearbyCabs: "+jsonArray.get(0))
        for (i in 0 until jsonArray.length()){
            val lat = (jsonArray.get(i) as JSONObject).getDouble(Constants.LAT)
            val lng = (jsonArray.get(i) as JSONObject).getDouble(Constants.LNG)
            val latLng = LatLng(lat, lng)
            nearbyCabsLocations.add(latLng)
        }
        view?.showNearbyCabs(nearbyCabsLocations)
    }

    override fun onDisconnect() {
        Log.i(TAG, "onDisconnect: ")
    }

    override fun onError(e: String) {
        Log.i(TAG, "onError: $e")
    }
    //holds model and view also


    fun requestNearbyCabs(latLng: LatLng){
        val jsonObject = JSONObject()
        jsonObject.put(Constants.TYPE, Constants.NEAR_BY_CABS)
        jsonObject.put(Constants.LAT, latLng.latitude)
        jsonObject.put(Constants.LNG, latLng.longitude)
        webSocket.sendMessage(jsonObject.toString())
    }
    fun requestCab(pickUpLatLng: LatLng, dropLatLng: LatLng){
        val jsonObject = JSONObject()
        jsonObject.put(Constants.TYPE, Constants.REQUEST_CAB)
        jsonObject.put("pickUpLat", pickUpLatLng.latitude)
        jsonObject.put("pickUpLng", pickUpLatLng.longitude)
        jsonObject.put("dropLat", dropLatLng.latitude)
        jsonObject.put("dropLng", dropLatLng.longitude)

        webSocket.sendMessage(jsonObject.toString())
    }

//    suspend fun searchByName(currentLatLng: LatLng, place: String): PlacesResult?{
//        Log.i(TAG, "searchByName: called")
//        val job: CompletableJob = Job()
//        var value: PlacesResult? = null
//        job.let {
//            CoroutineScope(Dispatchers.IO + it).launch {
//                Log.i(TAG, "searchByName: launched")
//                val result = SearchApiClient.searchService.fetchSearchResult("service.DiScDu8gelClrjE1xDMrgFvXMpfaiVtA4COaTnpe",
//                        place, currentLatLng.latitude, currentLatLng.longitude)
//                withContext(Dispatchers.Main) {
//                    value = result
//                    it.complete()
//                }
//            }
//        }
//        return value
//    }

}