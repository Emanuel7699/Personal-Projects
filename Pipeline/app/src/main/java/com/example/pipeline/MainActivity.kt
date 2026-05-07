package com.example.pipeline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pipeline.ui.theme.PipelineTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val gameManager = GameManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PipelineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(gameManager)
                }
            }
        }
    }
}

@Composable
fun MainScreen(manager: GameManager) {
    val textMeasurer = rememberTextMeasurer()
    var buttonText by remember { mutableStateOf("Start Play") }
    val maxFlow by manager.maxFlow
    val flowPercent by manager.flowPercent
    var currentPath by remember { mutableStateOf<List<Int>>(emptyList()) }
    var lastPosition by remember { mutableStateOf<Offset?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)

        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val startNode = manager.nodes.find { node ->
                                (node._type == NodeType.START || node._type == NodeType.END) &&
                                        (offset - node._position).getDistance() < 60f
                            }
                            if (startNode != null) {
                                currentPath = listOf(startNode._id)
                                lastPosition = offset
                            } else {
                                currentPath = emptyList()
                                lastPosition = null
                            }
                        },
                        onDrag = { change, _ ->
                            if (currentPath.isNotEmpty()) {
                                val offset = change.position
                                val touchedNode = manager.nodes.find { node ->
                                    (offset - node._position).getDistance() < 60f
                                }
                                if (touchedNode != null && touchedNode._id !in currentPath) {
                                    val lastNodeId = currentPath.last()
                                    val edgeExists = manager.edges.any {
                                        (it._from == lastNodeId && it._to == touchedNode._id) ||
                                                (it._from == touchedNode._id && it._to == lastNodeId)
                                    }
                                    if (edgeExists) {
                                        currentPath = currentPath + touchedNode._id
                                    } else {
                                        currentPath = emptyList()
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            val firstNode = manager.nodes.find { it._id == currentPath.firstOrNull() }
                            val lastNode = manager.nodes.find { it._id == currentPath.lastOrNull() }
                            val isForward = firstNode?._type == NodeType.START && lastNode?._type == NodeType.END
                            val isBackward = firstNode?._type == NodeType.END && lastNode?._type == NodeType.START
                            if (isForward || isBackward) {
                                val pathEdges = (0 until currentPath.size - 1).mapNotNull { i ->
                                    manager.edges.find {
                                        (it._from == currentPath[i] && it._to == currentPath[i + 1]) ||
                                                (it._from == currentPath[i + 1] && it._to == currentPath[i])
                                    }
                                }
                                if (isForward) {
                                    val minResidual = pathEdges.minOfOrNull { it._capacity - it._flow } ?: 0
                                    if (minResidual > 0) {
                                        pathEdges.forEach { edge ->
                                            val index = manager.edges.indexOf(edge)
                                            manager.edges[index] = edge.copy(_flow = edge._flow + 1)
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Path is at maximum capacity!")
                                        }
                                    }
                                } else if (isBackward) {
                                    val minFlow = pathEdges.minOfOrNull { it._flow } ?: 0
                                    if (minFlow > 0) {
                                        pathEdges.forEach { edge ->
                                            val index = manager.edges.indexOf(edge)
                                            manager.edges[index] = edge.copy(_flow = edge._flow - 1)
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("No flow to reduce on this path!")
                                        }
                                    }
                                }
                                val totalFlow = manager.edges.filter { it._to == 1 }.sumOf { it._flow }
                                manager.flowPercent.value = manager.flowSolver.calculatePercent(totalFlow, manager.maxFlow.value)
                            }
                            currentPath = emptyList()
                            lastPosition = null
                        }
                    )
                }
        ) {
            manager.generator.updateScreenSize(size.width, size.height)
            manager.edges.forEach { edge ->
                val startNode = manager.nodes.find { it._id == edge._from }
                val endNode = manager.nodes.find { it._id == edge._to }

                if (startNode != null && endNode != null) {
                    val startPos = startNode._position
                    val endPos = endNode._position

                    val dx = endPos.x - startPos.x
                    val dy = endPos.y - startPos.y
                    val angleRad = atan2(dy.toDouble(), dx.toDouble())
                    var angleDeg = Math.toDegrees(angleRad).toFloat()


                    val nodeRadius = 35f
                    val targetX = endPos.x - cos(angleRad).toFloat() * nodeRadius
                    val targetY = endPos.y - sin(angleRad).toFloat() * nodeRadius
                    val targetPos = Offset(targetX, targetY)
                    val isInCurrentPath = currentPath.zipWithNext().any { (a, b) ->
                        (edge._from == a && edge._to == b) || (edge._from == b && edge._to == a)
                    }
                    drawLine(
                        color = if (isInCurrentPath) Color.Cyan else Color.Gray,
                        start = startNode._position,
                        end = targetPos,
                        strokeWidth = 8f
                    )

                    val arrowSize = 30f
                    val arrowAngle = Math.toRadians(30.0)

                    val arrowPt1 = Offset(
                        x = targetPos.x - arrowSize * cos(angleRad - arrowAngle).toFloat(),
                        y = targetPos.y - arrowSize * sin(angleRad - arrowAngle).toFloat()
                    )
                    val arrowPt2 = Offset(
                        x = targetPos.x - arrowSize * cos(angleRad + arrowAngle).toFloat(),
                        y = targetPos.y - arrowSize * sin(angleRad + arrowAngle).toFloat()
                    )

                    val arrowPath = Path().apply {
                        moveTo(targetPos.x, targetPos.y)
                        lineTo(arrowPt1.x, arrowPt1.y)
                        lineTo(arrowPt2.x, arrowPt2.y)
                        close()
                    }
                    drawPath(path = arrowPath, color = Color.Gray)


                    if (angleDeg > 90f || angleDeg < -90f) {
                        angleDeg += 180f
                    }

                    val midX = (startNode._position.x + endNode._position.x) / 2
                    val midY = (startNode._position.y + endNode._position.y) / 2

                    val textString = "${edge._flow}/${edge._capacity}"
                    val textLayout = textMeasurer.measure(
                        text = textString,
                        style = TextStyle(color = Color.Black, fontSize = 18.sp)
                    )

                    rotate(degrees = angleDeg, pivot = Offset(midX, midY)) {
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(
                                x = midX - (textLayout.size.width / 2f),
                                y = midY - textLayout.size.height - 5f
                            )
                        )
                    }
                }

            }

            manager.nodes.forEach { node ->
                drawCircle(
                    color = when (node._type) {
                        NodeType.START -> Color.Green
                        NodeType.END -> Color.Red
                        else -> Color.Blue
                    },
                    radius = 30f,
                    center = node._position
                )
            }

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Max Flow: $maxFlow")
            Text(text = "Flow Percent: $flowPercent%")
        }

        Button(
            onClick = {
                buttonText = "Next Level"
                manager.initGame()
            },
            enabled = buttonText == "Start Play" || flowPercent == 100,
            modifier = Modifier
                .padding(32.dp, 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = buttonText)
        }
    }
}