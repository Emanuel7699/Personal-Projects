package com.example.pipeline

data class Edge (
    var _id: Int = -1,
    var _from: Int = -1,
    var _to: Int = -1,
    var _color: String = "red",
    var _flow: Int = 0,
    var _capacity: Int = 0
)