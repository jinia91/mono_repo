package com.example.internal_api.tcp

import java.io.Serializable

data class Mock(
    val body : String = "default"
) : Serializable