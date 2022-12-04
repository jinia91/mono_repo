package com.example.internal_api.tcp

import org.springframework.integration.annotation.MessageEndpoint
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Controller

@MessageEndpoint
@Controller
class TcpHandler(
//    private val sevice : Service
) {

    @ServiceActivator(
        inputChannel = "helloChannel",
        outputChannel = "response",
    )
    fun handle(input : String) : String{
        println("_____active hello hander ${input}______")
        return "inputReturn"
        }

    @ServiceActivator(
            inputChannel = "byeChannel",
            outputChannel = "response"
        )
    fun handle2(input : String) : String{
        println("_____active hello hander ${input}______")
        return input
    }
}



//    @ServiceActivator(
//        inputChannel = "byeInputChannel",
//    )
//    fun handleBye(input : String) {
//        println("_____active bye hander______")
//        return "return " + input
//    }




//@MessagingGateway(name = "responseGateway", defaultRequestChannel = "response", defaultReplyChannel = "outboundChannel")
//interface ResponseGateway