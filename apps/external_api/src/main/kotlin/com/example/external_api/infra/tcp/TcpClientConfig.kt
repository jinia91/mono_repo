package com.example.external_api.infra.tcp

import StringLengthHeaderSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Transformers
import org.springframework.integration.ip.dsl.Tcp
import org.springframework.integration.ip.tcp.TcpOutboundGateway
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory

@Configuration
class TcpConfig {

    @Bean
    fun outBound(outBoundGateway : TcpOutboundGateway): IntegrationFlow {
        return IntegrationFlows.from("input")
            .handle(outBoundGateway)
            .transform(Transformers.objectToString())
            .get()
    }

    @Bean
    fun outBoundGateway(): TcpOutboundGateway {
        val netClient = Tcp.netClient("localhost", 9191)
        val factory: AbstractClientConnectionFactory = netClient.get()
            .apply {
                isSingleUse = false
                isSoKeepAlive = true
                soTimeout = 0
                deserializer = StringLengthHeaderSerializer()
                serializer = StringLengthHeaderSerializer()
            }
        val outboundGateway = Tcp.outboundGateway(factory)
        return outboundGateway.get()
    }
}