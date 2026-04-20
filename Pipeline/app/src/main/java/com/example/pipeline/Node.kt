package com.example.pipeline

import androidx.compose.ui.geometry.Offset


enum class NodeType {START, END, NORMAL }


class Node(
    var _id: Int = -1,
    var _position: Offset = Offset(50f, 50f),
    var _type: NodeType = NodeType.NORMAL,
    val _nextNodes: MutableList<Int> = mutableListOf<Int>()
) {
    val maxOutDegree get() = when (_type) {
        NodeType.START -> Int.MAX_VALUE
        NodeType.END -> 0
        NodeType.NORMAL -> 3
    }


    val canAddEdge get() = _nextNodes.size < maxOutDegree
    companion object {
        fun createStart() = Node(
            _id = 0, _type = NodeType.START
        )

        fun createEnd() = Node(
            _id = 1, _type = NodeType.END
        )
    }
}