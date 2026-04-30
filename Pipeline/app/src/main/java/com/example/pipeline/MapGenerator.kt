package com.example.pipeline

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.roundToInt

class MapGenerator {
    val layers = mutableListOf(
        mutableListOf<Node>(),
        mutableListOf<Node>(),
        mutableListOf<Node>(),
    )
    private var screenWidth = 0f
    private var screenHeight = 0f
    private val MAX_NODES_PER_LAYER = 3

    fun updateScreenSize(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
    }

    fun generateInitialMap(historyNodes: List<Node>, historyEdges: List<Edge>, historyLayers: List<List<Node>>): Pair<List<Node>, List<Edge>> {

        val nodes = historyNodes.toMutableList()
        val edges = historyEdges.toMutableList()
        if (nodes.isEmpty()) {
            layers.clear()
            layers.addAll(
                listOf(
                    mutableListOf<Node>(),
                    mutableListOf<Node>(),
                    mutableListOf<Node>()
                )
            )
            startGame(nodes, edges)
        } else {
            layers.clear()
            historyLayers.forEach { layer ->
                layers.add(layer.toMutableList())
            }
        }
        randNextLevel(nodes, edges)
        return Pair(nodes, edges)
    }

    fun randNextLevel(nodes: MutableList<Node>, edges: MutableList<Edge>) {
        when ((0..2).random()) {
            0 -> addPoint(nodes, edges)
            1 -> {if (layers.size < 4) {randNextLevel(nodes, edges)
                    return
                }
                val randomNode = layers[(1 until layers.size - 2).random()]
                    .filter { it.canAddEdge }
                    .randomOrNull()

                if (randomNode != null) {
                    addEdge(edges, randomNode)
                }
            }
            2 -> changeCapacity(edges)
        }
    }

    fun addPoint(nodes: MutableList<Node>, edges: MutableList<Edge>) {

        val newNode = Node(_id = nodes.size)
        nodes.add(newNode)
        val endNode = layers.last().first()

        if (layers[layers.size - 2].size >= MAX_NODES_PER_LAYER){
            layers[layers.size - 2].forEach {currentNode ->
                currentNode._nextNodes.remove(endNode._id)
                edges.removeAll { it._from == currentNode._id && it._to == endNode._id }
            }
            layers.add(layers.size - 1, mutableListOf<Node>())
            layers[layers.size - 2].add(newNode)
            layers[layers.size - 3].forEach { currentNode ->
                addEdge(edges, currentNode, currentNode._id, newNode._id)
            }
            addEdge(edges, newNode, newNode._id, endNode._id)
        }
        else {
            layers[layers.size - 2].add(newNode)

            val fromNode = layers[layers.size - 3]
                .filter { it.canAddEdge }
                .randomOrNull()

            if (fromNode != null) {
                addEdge(edges, fromNode, fromNode._id, newNode._id)
            }

            addEdge(edges, newNode, newNode._id, endNode._id)
        }
        sortAllLayers()
    }

    fun addEdge(edges: MutableList<Edge>, node: Node) {
        if (layers.size < 4) return
        val index = layers.indexOfFirst { layer -> layer.any { it._id == node._id } }
        if (index == -1 || index >= layers.size - 1) return

        val currentNode = node

        val nextLayerNodes = layers[index + 1]

        val sameLayerNearbyNodes = layers[index].filter { targetNode ->
            abs(targetNode._id - currentNode._id) == 1
        }

        val isAvailable = { targetNode: Node ->
            targetNode._id !in currentNode._nextNodes && targetNode._id != currentNode._id
        }

        val to = (nextLayerNodes + sameLayerNearbyNodes)
            .filter(isAvailable)
            .randomOrNull()?._id ?: return
        addEdge(edges,currentNode, currentNode._id, to)
    }

    fun addEdge(edges: MutableList<Edge>, node: Node, from: Int, to: Int) {
        node._nextNodes.add(to)
        edges.add(Edge(
            _id = edges.size,
            _from = from,
            _to = to,
            _capacity = (1..10).random()
        ))
    }

    fun changeCapacity(edges: MutableList<Edge>) {
        val edge = edges.randomOrNull() ?: return

        val newCapacity = (3..10).filter { it != edge._capacity }.randomOrNull() ?: return

        val index = edges.indexOf(edge)
        edges[index] = edge.copy(_capacity = newCapacity)
    }

    fun sortNodes(nodesList: MutableList<Node>, xPos: Float) {
        if (nodesList.isEmpty() || screenHeight <= 0f) return

        val space = screenHeight / (nodesList.size + 1)

        nodesList.forEachIndexed { index, node ->
            val newY = space * (index + 1)
            node._position = Offset(x = xPos, y = newY)
        }
    }

    fun startGame(nodes: MutableList<Node>, edges: MutableList<Edge>) {
        val startNode = Node.createStart()
        val endNode = Node.createEnd()

        nodes.add(startNode)
        layers.first().add(startNode)
        nodes.add(endNode)
        layers.last().add(endNode)

        val newNode1 = Node(_id = nodes.size)
        layers[1].add(newNode1)
        nodes.add(newNode1)

        addEdge(edges, startNode, startNode._id, newNode1._id)
        addEdge(edges, newNode1, newNode1._id, endNode._id)
        sortAllLayers()
    }

    fun sortAllLayers() {
        if (layers.isEmpty() || screenWidth <= 0f) return

        layers.forEachIndexed { index, layer ->
            val xPos = screenWidth * (index + 1) / (layers.size + 1)

            sortNodes(layer, xPos)
        }
    }
}