package com.example.external_api.application

import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.stereotype.Component

@MessagingGateway(defaultRequestChannel = "input")
@Component
interface TcpClient {
    @Gateway
    fun sendData(input: String): String
}