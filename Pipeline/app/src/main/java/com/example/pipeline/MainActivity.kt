package com.example.pipeline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.pipeline.ui.theme.PipelineTheme

class MainActivity : ComponentActivity() {
    private val gameManager = GameManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // הסרנו את initGame מכאן, כי עוד אין לנו את גודל המסך

        setContent {
            PipelineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameCanvas(gameManager)
                }
            }
        }
    }
}

@Composable
fun GameCanvas(manager: GameManager) {
    // השתמשנו ב-LaunchedEffect כדי להריץ את הלוגיקה רק פעם אחת כשהמסך עולה
    // או כשהמידות משתנות
    Canvas(modifier = Modifier.fillMaxSize()) {
        // 1. עדכון המידות ב-Generator
        manager.generator.updateScreenSize(size.width, size.height)

        // 2. אתחול המשחק רק אם הוא עדיין לא אותחל (כדי שלא יתאפס בכל פריים)
        if (manager.nodes.isEmpty()) {
            manager.initGame()
        }

        // 3. ציור הקווים (Edges)
        manager.edges.forEach { edge ->
            val startNode = manager.nodes.find { it._id == edge._from }
            val endNode = manager.nodes.find { it._id == edge._to }

            if (startNode != null && endNode != null) {
                drawLine(
                    color = Color.Gray,
                    start = startNode._position,
                    end = endNode._position,
                    strokeWidth = 4f
                )
            }
        }

        // 4. ציור הנקודות (Nodes)
        manager.nodes.forEach { node ->
            drawCircle(
                color = when (node._type) {
                    NodeType.START -> Color.Green
                    NodeType.END -> Color.Red
                    else -> Color.Blue
                },
                radius = 20f,
                center = node._position
            )
        }
    }
}