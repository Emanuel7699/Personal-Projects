package com.example.pipeline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

class GameManager {

    val nodes = mutableStateListOf<Node>()
    val edges = mutableStateListOf<Edge>()
    val generator = MapGenerator()
    val flowSolver = FlowSolver()
    var maxFlow = mutableStateOf(0)
    var flowPercent = mutableStateOf(0)

    val historyNodes = mutableListOf<Node>()
    val historyEdges = mutableListOf<Edge>()
    var currentLayers: List<List<Node>> = emptyList()


    fun initGame(){
        historyNodes.clear()
        historyEdges.clear()
        historyNodes.addAll(nodes)
        historyEdges.addAll(edges.map { it.copy(_flow = 0) })
        currentLayers = generator.layers.map { it.toList() }
        clean()
        val (newNodes, newEdges) = generator.generateInitialMap(historyNodes, historyEdges, currentLayers)
        nodes.addAll(newNodes)
        edges.addAll(newEdges)

        val currentMaxFlow = flowSolver.edmondsKarp(edges.toList())
        val totalCapacity = edges.filter { it._to == 1 }.sumOf { it._capacity }
        maxFlow.value = currentMaxFlow
        flowPercent.value = 0
    }

    fun clean(){
        nodes.clear()
        edges.clear()
    }
}