package com.example.pipeline

import androidx.annotation.FractionRes
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

        repeat(50) {
            randNextLevel(nodes, edges)
        }

        level++
        return Pair(nodes, edges)
    }

    fun resetLevel() {
        level = 0
    }

    fun randNextLevel(nodes: MutableList<Node>, edges: MutableList<Edge>) {
        when ((0..4).random()) {
            0 -> addPoint(nodes, edges)
            1 -> try{addEdge(nodes, edges, nodes[0], 4)}
            catch(e: Exception){
                addPoint(nodes, edges)
            }
            2 -> try {addEdge(nodes, edges, layers[1].random(), 2)}
                catch (e: Exception) {
                    addPoint(nodes, edges)
            }
            3 -> try{addEdge(nodes, edges, layers[2].random(), 0)}
                catch(e: Exception){
                    addPoint(nodes, edges)
                }
            4 -> changeFlow(edges)
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
            addEdge(nodes, edges, newNode,0)
            addEdge(nodes, edges, newNode,1)
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
            addEdge(nodes, edges, newNode,2)
            addEdge(nodes, edges, newNode,3)
            sortNodes(layers[1], 2 * screenWidth / 5)
        }
    }

    fun addEdge(nodes: List<Node>, edges: MutableList<Edge>, node: Node, case: Int) {
        if (nodes.isEmpty()) return
        var from = node._id
        var to = node._id
        val isAvailable = { id: Int -> id !in node.nextNodes && id != node._id }

        when (case) {
            0 -> {
                from = node._id
                to = (1 until nodes.size - 1).filter { isAvailable(it) }.random()
                }
            1 -> {
                from = (2 until nodes.size - 1).filter { isAvailable(it) }.random()
                to = node._id
                }
            2 -> {
                from = node._id
                to = (2 until nodes.size - 1).filter { isAvailable(it) }.random()
            }
            3 -> {
                from = (0 until nodes.size - 1).filter {  isAvailable(it) && it != 1}.random()
                to = node._id
            }
            4 -> {
                from = node._id
                to = layers[1].random()._id
            }
        }
        addEdge(edges,node, from, to)
    }

    fun addEdge(edges: MutableList<Edge>, node: Node, from: Int, to: Int) {
        node.nextNodes.add(to)
        edges.add(Edge(
            _id = edges.size,
            _from = from,
            _to = to,
            _flow = (0..10).random()
        ))
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

        addEdge(edges,startNode, 0, 2)
        addEdge(edges,newNode1, 2, 3)
        addEdge(edges,newNode2, 3, 1)
        sortNodes(layers[0], 1 * screenWidth / 5)
        sortNodes(layers[1], 2 * screenWidth / 5)
        sortNodes(layers[2], 3 * screenWidth / 5)
        sortNodes(layers[3], 4 * screenWidth / 5)
    }
}