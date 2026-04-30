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
import androidx.compose.runtime.key
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
    Column(modifier = Modifier.fillMaxSize()) {

        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
                    drawLine(
                        color = Color.Gray,
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

//                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()


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

        Button(
            onClick = {
                buttonText = "Next Level"
                manager.initGame()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(400.dp, 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = buttonText)
        }
    }
}