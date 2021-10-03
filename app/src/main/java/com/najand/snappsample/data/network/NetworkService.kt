package com.najand.snappsample.data.network

import com.najand.snappsample.simulator.WebSocket
import com.najand.snappsample.simulator.WebSocketListener

class NetworkService {

    fun createWebSocket(webSocketListener: WebSocketListener) : WebSocket{
        return WebSocket(webSocketListener);
    }
}