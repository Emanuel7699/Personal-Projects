package com.example.pipeline

import androidx.compose.runtime.mutableStateListOf

class GameManager {

    val nodes = mutableStateListOf<Node>()
    val edges = mutableStateListOf<Edge>()
    val generator = MapGenerator()


    fun initGame(){
        clean()
        val (newNodes, newEdges) = generator.generateInitialMap()
        nodes.addAll(newNodes)
        edges.addAll(newEdges)
    }

    fun clean(){
        nodes.clear()
        edges.clear()
        generator.resetLevel()
    }
}