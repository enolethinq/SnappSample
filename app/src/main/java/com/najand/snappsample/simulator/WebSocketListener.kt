package com.najand.snappsample.simulator

interface WebSocketListener {

    fun onConnect()
    fun onMessage(data: String)
    fun onDisconnect()
    fun onError(e: String)
}