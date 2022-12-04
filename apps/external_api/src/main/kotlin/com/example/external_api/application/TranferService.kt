package com.example.external_api.application

import org.springframework.stereotype.Service

@Service
class TranferService(
    private val client: TcpClient
) {
    fun foo(input : String) {
        client.sendData(input)
    }
}