package com.example.pipeline

import androidx.compose.ui.geometry.Offset

class MapGenerator {
    private val layers = List(4) { mutableListOf<Node>() }
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var level = 0

    fun updateScreenSize(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
    }

    fun generateInitialMap(): Pair<List<Node>, List<Edge>> {
        layers.forEach { it.clear() }
        val nodes = mutableListOf<Node>()
        val edges = mutableListOf<Edge>()

        startGame(nodes, edges)

        repeat(level) {
            randNextLevel(nodes, edges)
        }

        level++
        return Pair(nodes, edges)
    }

    fun resetLevel() {
        level = 0
    }

    fun randNextLevel(nodes: MutableList<Node>, edges: MutableList<Edge>) {
        when ((0..2).random()) {
            0 -> addPoint(nodes, edges)
            1 -> addEdge(nodes, edges)
            2 -> changeFlow(edges)
        }
    }

    fun addPoint(nodes: MutableList<Node>, edges: MutableList<Edge>) {
        val newNodeId = nodes.size
        if (layers[1].size >= layers[2].size) {
            val newNode = Node(
                _id = newNodeId,
                _subID = layers[2].size,
                _position = Offset(3 * screenWidth / 5, screenHeight / 2)
            )
            layers[2].add(newNode)
            nodes.add(newNode)
            edges.add(Edge(
                _id = edges.size,
                _from = (2 until nodes.size - 1).random(),
                _to = nodes.size,
                _flow = (0..10).random()
            ))
            
            edges.add(Edge(
                _id = edges.size,
                _from = nodes.size,
                _to = (1 until nodes.size - 1).random(),
                _flow = (0..10).random()
            ))
            sortNodes(layers[2], 3 * screenWidth / 5)
        }
        else{
            val newNode = Node(
                _id = nodes.size,
                _subID = layers[1].size,
                _position = Offset(2 * screenWidth / 5, screenHeight / 2)
            )
            layers[1].add(newNode)
            nodes.add(newNode)
            
            edges.add(Edge(
                _id = edges.size,
                _from = (0 until nodes.size - 1).filter { it != 1 }.random(),
                _to = nodes.size,
                _flow = (0..10).random()
            ))
            edges.add(Edge(
                _id = edges.size,
                _from = nodes.size,
                _to = (2 until nodes.size - 1).random(),
                _flow = (0..10).random()
            ))
            sortNodes(layers[1], 2 * screenWidth / 5)
        }
    }

    fun addEdge(nodes: List<Node>, edges: MutableList<Edge>) {
        if (nodes.isEmpty()) return
        while (true) {
            val from = (0 until nodes.size).random()
            val to = (0 until nodes.size).random()
            if (from == to) continue
            val alreadyExists = (edges.any { it._from == from && it._to == to }) ||
                    (layers[1].any { it._id == from} && layers[0].any { it._id == to}) ||
                    (layers[3].any { it._id == from} && layers[2].any { it._id == to})
            if (!alreadyExists) {
                edges.add(Edge(
                    _id = edges.size,
                    _from = from,
                    _to = to,
                    _flow = (0..10).random()
                ))
                break
            }
        }
    }

    fun changeFlow(edges: MutableList<Edge>){
        if (edges.isEmpty()) return
        val pos = (0 until edges.size).random()
        while (true) {
            val newFlow = (1..10).random()
            if (newFlow != edges[pos]._flow) {
                edges[pos] = edges[pos].copy(_flow = newFlow)
                break
            }
        }
    }

    fun sortNodes(nodesList: MutableList<Node>, xPos: Float) {
        if (nodesList.isEmpty()) return

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
        layers[0].add(startNode)
        nodes.add(endNode)
        layers[3].add(endNode)

        val newNode1 = Node(
            _id = nodes.size,
            _subID = layers[1].size,
            _position = Offset(2 * screenWidth / 5, screenHeight / 2)
        )
        layers[1].add(newNode1)
        nodes.add(newNode1)

        val newNode2 = Node(
            _id = nodes.size,
            _subID = layers[2].size,
            _position = Offset(3 * screenWidth / 5, screenHeight / 2)
        )
        layers[2].add(newNode2)
        nodes.add(newNode2)

        for (i in 0..2) {
            edges.add(
                Edge(
                    _id = edges.size,
                    _from = i,
                    _to = i+1,
                    _flow = (0..10).random()
                )
            )
        }
        sortNodes(layers[0], 1 * screenWidth / 5)
        sortNodes(layers[1], 2 * screenWidth / 5)
        sortNodes(layers[2], 3 * screenWidth / 5)
        sortNodes(layers[3], 4 * screenWidth / 5)
    }
}