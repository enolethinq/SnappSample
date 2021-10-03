package com.najand.snappsample.ui.maps

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.datatransport.runtime.Destination
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.najand.snappsample.R
import com.najand.snappsample.data.model.PlacesResult
import com.najand.snappsample.data.network.NetworkService
import com.najand.snappsample.data.network.SearchApiClient
import com.najand.snappsample.ui.adapter.PlacesAdapter
import com.najand.snappsample.utils.MapUtils
import com.najand.snappsample.utils.PermissionUtils
import com.najand.snappsample.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.places_bottom_sheet.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MapsActivity: AppCompatActivity(), MapsView, OnMapReadyCallback, PlacesAdapter.OnItemClicked {

    companion object{
        private const val TAG = "MapsActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 909
        private const val PICKUP_REQUEST_CODE = 11
        private const val DROP_REQUEST_CODE = 10
    }

    private lateinit var dialog: Dialog
    private lateinit var presenter: MapsPresenter
    private lateinit var googleMap: GoogleMap
    private var fusedLocationProviderClient: FusedLocationProviderClient?=null
    private lateinit var locationCallback: LocationCallback
    private val nearbyCabMarkerList = arrayListOf<Marker>()

    private var currentLatLng: LatLng? =null
    private var pickUpLatLng: LatLng? = null
    private var dropUpLatLng: LatLng? = null

    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null
    
    private var isAllowed: Boolean = false
    private var isDestination: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        ViewUtils.enableTransparentStatusBar(window)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        presenter = MapsPresenter(NetworkService())
        presenter.onAttach(this)

        setupClickListener()
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
    }

    private fun setupClickListener(){
        var pickupLocationName = ""
        var dropLocationName = ""
        pickUpTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                pickupLocationName = pickUpTextView.text.toString()
            }
        })
        dropTextView.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                dropLocationName = dropTextView.text.toString()
            }
        })

        pickUpTextView.setOnClickListener {
//            launchLocationAutoCompleteActivity(PICKUP_REQUEST_CODE)
            isDestination = false

        }
        dropTextView.setOnClickListener {
            isDestination = true
//            launchLocationAutoCompleteActivity(DROP_REQUEST_CODE)
        }
        pickupSearchBtn.setOnClickListener{
            if (pickUpTextView.text.length > 2){
                GlobalScope.launch(IO) {
                    val response = SearchApiClient.searchService.fetchSearchResult(getString(R.string.api_key),
                            pickupLocationName, currentLatLng!!.latitude, currentLatLng!!.longitude)
                    if (response.isSuccessful){
                        for (body in response.body()!!.places) {
                            Log.i(TAG, "onLocationResult: nacm--> " + body.title)
                        }
                        withContext(Main){
                            showBottomSheet(response.body())
                        }
                    }
                }
            }
        }
        dropSearchBtn.setOnClickListener{
            if (pickUpTextView.text.length > 2){
                GlobalScope.launch(IO) {
                    val response = SearchApiClient.searchService.fetchSearchResult(getString(R.string.api_key),
                            dropLocationName, currentLatLng!!.latitude, currentLatLng!!.longitude)
                    if (response.isSuccessful){
                        for (body in response.body()!!.places) {
                            Log.i(TAG, "onLocationResult: nacm--> " + body.title)
                        }
                        withContext(Main){
                            showBottomSheet(response.body())
                        }
                    }
                }
            }
        }
    }

    private fun showBottomSheet(response: PlacesResult?) {
//        val adapter = response?.let { PlacesAdapter(it, listener = PlacesAdapter.OnItemClicked) }
        val adapter = PlacesAdapter(response!!, this)
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.places_bottom_sheet)
        dialog.places_rv.layoutManager = LinearLayoutManager(this)
        dialog.places_rv.adapter = adapter

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun launchLocationAutoCompleteActivity(requestCode: Int){
        if (!Places.isInitialized()){
            Places.initialize(this, getString(R.string.google_maps_key))
        }
        val fields: List<Place.Field> = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        //request code for detecting we are from which tv
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
        startActivityForResult(intent, requestCode)
    }

    override fun onStart() {
        super.onStart()
        when{
            PermissionUtils.isAccessFineLocationGranted(this) ->{
                when{
                    PermissionUtils.isLocationEnable(this)->{
                        //fetch the location
                        setupLocationListener()
                    } else ->{
                        PermissionUtils.showGPSNotEnableDialog(this)
                    }
                }
            }
            else ->{
                PermissionUtils.requestAccessFineLocationPermission(this , LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    when{
                        PermissionUtils.isLocationEnable(this)->{
                            //fetch the location
                            setupLocationListener()
                        } else ->{
                        PermissionUtils.showGPSNotEnableDialog(this)
                    }
                    }
                }else{
                    Toast.makeText(this, "Location Permission Not Granted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKUP_REQUEST_CODE || requestCode == DROP_REQUEST_CODE){
            Log.i(TAG, "onActivityResult: $resultCode")
            when (resultCode){
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    Log.i(TAG, "onActivityResult: result place: " + place.latLng)
                    when (requestCode) {
                        PICKUP_REQUEST_CODE -> {
//                            pickUpTextView.text = place.name
                            pickUpLatLng = place.latLng
                        }
                        DROP_REQUEST_CODE -> {
//                            dropTextView.text = place.name
                            dropUpLatLng = place.latLng
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Log.d(TAG, "onActivityResult: error message " + status.statusMessage!!)
                }
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "onActivityResult: canceled")
                }
            }
        }
    }
    private fun animateCamera(latLng: LatLng?){
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }



    private fun enableMyLocation(){
        Log.i(TAG, "enableMyLocation: called")
        googleMap.setPadding(0, ViewUtils.dpToPx(48f), 0, 0)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }
    }

    private fun setCurrentLocationAsPickUp(){
        pickUpLatLng = currentLatLng
//        pickUpTextView.text = getString(R.string.current_location)
    }

    private fun moveCamera(latLng: LatLng?){
        Log.i(TAG, "moveCamera: called")
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun addCarMarker(latLng: LatLng): Marker?{
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this))
        return googleMap.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))
    }

    private fun addOriginOrDestinationMarker(latLng: LatLng): Marker?{
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getLocationBitmap(this))
        return if (isDestination)  {
            destinationMarker = googleMap.addMarker(MarkerOptions().position(latLng)
                    .title("مقصد")
                    .flat(true))
            destinationMarker
        }else{
            originMarker =  googleMap.addMarker(MarkerOptions().position(latLng)
                    .title("مبدا")
                    .flat(true)
                    .icon(bitmapDescriptor))
            originMarker
        }
    }

    private fun setupLocationListener(){
        Log.i(TAG, "setupLocationListener: called")
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        //for getting the current location update, update after every two seconds
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                runOnUiThread {
                    if (currentLatLng == null) {
                        for (location in locationResult.locations) {
                            if (currentLatLng == null) {
                                currentLatLng = LatLng(location.latitude, location.longitude)
                                setCurrentLocationAsPickUp()
                                enableMyLocation()
                                moveCamera(currentLatLng)
                                animateCamera(currentLatLng)
                                presenter.requestNearbyCabs(currentLatLng!!)
                            }
                        }

                    }

                }

            }

            //update the location of the user on server


        }

        fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
        )

    }


    override fun showNearbyCabs(latLngList: List<LatLng>) {
        nearbyCabMarkerList.clear()
        for (latLng in latLngList){
            val nearbyCabsMarker = addCarMarker(latLng)
            nearbyCabMarkerList.add(nearbyCabsMarker!!)
        }
    }

    override fun onDestroy() {
        presenter.onDetach()
        super.onDestroy()
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    override fun setOnItemClickedListener(lat: Float, lng: Float) {
        dialog.dismiss()
        val latLng = LatLng(lat.toDouble(),lng.toDouble())
        if (isDestination)
            originMarker?.remove()
        else    destinationMarker?.remove()
        addOriginOrDestinationMarker(latLng)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(20f), 1000, null)
    }
}