package com.najand.snappsample.data.model

import com.google.gson.annotations.SerializedName

data class PlacesResult(var count: Int, @SerializedName("items") var places: List<Places>){}
