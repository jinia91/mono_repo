package com.example.external_api

import SampleLib
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {

    @GetMapping("")
    fun stub(){
        SampleLib.STUB1
    }
}