package com.example.internal_api.tcp

import org.springframework.integration.annotation.MessageEndpoint
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Controller

@MessageEndpoint
@Controller
class Subscribers(
) {

    @ServiceActivator(
        inputChannel = "pubsubChannel",
        outputChannel = "response"
    )
    fun subscribe(input : String) : String{
        println("_____active hello hander1 ${input}______${Thread.currentThread()}")
        return "return $input sub1"
    }

    @ServiceActivator(
        inputChannel = "pubsubChannel",
        outputChannel = "response"
    )
    fun subscribe2(input : String) : String{
        println("_____active hello hander2 ${input}______${Thread.currentThread()}")
        return "return $input sub2"
    }
    @ServiceActivator(
        inputChannel = "pubsubChannel",
        outputChannel = "response"
    )
    fun subscribe3(input : String) : String{
        println("_____active hello hander3 ${input}______${Thread.currentThread()}")
        return "return $input sub3"
    }
}