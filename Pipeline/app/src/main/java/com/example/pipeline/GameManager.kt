package com.example.pipeline

import androidx.compose.runtime.mutableStateListOf

class GameManager {

    val nodes = mutableStateListOf<Node>()
    val edges = mutableStateListOf<Edge>()
    val generator = MapGenerator()

    val historyNodes = mutableListOf<Node>()
    val historyEdges = mutableListOf<Edge>()
    var currentLayers: List<List<Node>> = emptyList()


    fun initGame(){
        historyNodes.clear()
        historyEdges.clear()
        historyNodes.addAll(nodes)
        historyEdges.addAll(edges)
        currentLayers = generator.layers.map { it.toList() }
        clean()
        val (newNodes, newEdges) = generator.generateInitialMap(historyNodes, historyEdges, currentLayers)
        nodes.addAll(newNodes)
        edges.addAll(newEdges)
    }

    fun clean(){
        nodes.clear()
        edges.clear()
    }
}