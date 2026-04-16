package com.example.pipeline

import androidx.compose.ui.geometry.Offset


enum class NodeType {START, END, NORMAL }


class Node(
    var _id: Int = -1,
    var _subID: Int = -1,
    var _position: Offset = Offset(50f, 50f),
    var _color: String = "red",
    var _type: NodeType = NodeType.NORMAL
) {
    companion object {
        fun createStart() = Node(
            _id = 0, _subID = 0,
            _color = "red", _type = NodeType.START
        )

        fun createEnd() = Node(
            _id = 1, _subID = 1,
            _color = "red", _type = NodeType.END
        )
    }
}