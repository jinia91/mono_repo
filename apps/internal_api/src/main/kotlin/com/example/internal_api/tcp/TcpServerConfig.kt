package com.example.internal_api.tcp

import StringLengthHeaderSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.ip.dsl.Tcp
import org.springframework.integration.ip.tcp.TcpInboundGateway
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory
import org.springframework.integration.router.HeaderValueRouter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.GenericMessage

@Configuration
class TcpServerConfig{

    @Bean
    fun inbound(gateway: TcpInboundGateway): IntegrationFlow {

        return IntegrationFlows.from(gateway)
            .filter<ByteArray>{
                println("filter   " + Thread.currentThread())
                println(String(it))
                String(it)
                true
            }
            .transform(Message::class.java) { message ->
                val reqMsg = String(message.payload as ByteArray)
                println("____active transformer $reqMsg ${Thread.currentThread()}")
                val substringHeader = reqMsg.substring(0, 4)
                println(substringHeader)
                val map = mutableMapOf<String, Any>()
                map.putAll(message.headers)
                when (substringHeader) {
                    "0001" -> map["type"] = "hello"
                    "0002" -> map["type"] = "bye"
                    else  -> map["type"] = "pubsub"
                }
                GenericMessage(message.payload, MessageHeaders(map)).also { println("뿅") }
            }
            .route(
                HeaderValueRouter("type").apply {
                    setChannelMapping("hello", "helloChannel")
                    setChannelMapping("bye", "byeChannel")
                    setChannelMapping("pubsub", "pubsubChannel")
                }
            )
            .get()
    }

//    요청 1 : g.w -> integration flows -> reply
//    g.w -> deserialize -> 포메팅 -> (라우팅) -> service1, service2 -> reply -> g.w ->


    @Bean
    fun gateway(): TcpInboundGateway {
        val netServer = Tcp.netServer(9191)
        val factory :AbstractServerConnectionFactory = netServer.get().apply {
            isSingleUse = false
            isSoKeepAlive = true
            deserializer = StringLengthHeaderSerializer()
            serializer = StringLengthHeaderSerializer()
            soTimeout = 0
        }
        val inboundGateway = Tcp.inboundGateway(factory)
            .apply {
//                requestChannel(service())
                replyChannel(response())
            }
        return inboundGateway.get()
    }

//    @Bean
//    fun serializer()

    @Bean
    fun pubsubChannel() = PublishSubscribeChannel()

    @Bean
    fun response() = DirectChannel()
}