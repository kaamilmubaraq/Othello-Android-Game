package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    OthelloApp()
                }
            }
        }
    }
}

@Composable
private fun OthelloApp() {
    var state by remember { mutableStateOf(newGame()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header row: scores + current player + reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Black: ${state.blackCount}   White: ${state.whiteCount}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (!state.gameOver) {
                        "Turn: ${state.current.label}"
                    } else {
                        "Game Over"
                    }
                )
            }

            Button(onClick = { state = newGame() }) {
                Text("Reset")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.infoText.isNotBlank()) {
            Text(
                text = state.infoText,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
        }

        // Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, Color(0xFF0B3D0B))
                .background(Color(0xFF1B5E20)) // board green
        ) {
            Column(Modifier.fillMaxSize()) {
                for (r in 0 until 8) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until 8) {
                            val idx = r * 8 + c
                            val cell = state.board[idx]
                            val isValid = !state.gameOver && state.validMoves[idx]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(1.dp, Color(0xFF0B3D0B))
                                    .clickable(enabled = isValid) {
                                        state = playMove(state, idx)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when (cell) {
                                    Disc.BLACK -> DiscView(Color(0xFF111111))
                                    Disc.WHITE -> DiscView(Color(0xFFF5F5F5))
                                    else -> {
                                        // hint dot for valid moves
                                        if (isValid) {
                                            HintDot(color = state.current.hintColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Result line (only when game ends)
        if (state.gameOver) {
            Text(
                text = state.resultText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DiscView(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color(0x55000000), CircleShape)
    )
}

@Composable
private fun HintDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.65f))
    )
}
