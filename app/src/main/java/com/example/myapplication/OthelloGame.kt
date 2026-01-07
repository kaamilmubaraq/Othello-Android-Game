package com.example.myapplication

import androidx.compose.ui.graphics.Color

object Disc {
    const val EMPTY = 0
    const val BLACK = 1
    const val WHITE = 2
}

enum class Player(val disc: Int, val label: String) {
    BLACK(Disc.BLACK, "Black"),
    WHITE(Disc.WHITE, "White");

    fun other(): Player = if (this == BLACK) WHITE else BLACK

    val hintColor: Color
        get() = if (this == BLACK) Color(0xFF101010) else Color(0xFFFFFFFF)
}

class GameState(
    val board: IntArray,
    val current: Player,
    val validMoves: BooleanArray,
    val blackCount: Int,
    val whiteCount: Int,
    val gameOver: Boolean,
    val infoText: String,
    val resultText: String
)

private val DIRS = arrayOf(
    intArrayOf(-1, -1), intArrayOf(0, -1), intArrayOf(1, -1),
    intArrayOf(-1, 0),                 intArrayOf(1, 0),
    intArrayOf(-1, 1),  intArrayOf(0, 1),  intArrayOf(1, 1)
)

fun newGame(): GameState {
    val board = IntArray(64) // default = 0, which matches Disc.EMPTY

    // Standard Othello start:
    // (3,3)=W, (4,4)=W, (3,4)=B, (4,3)=B
    board[3 * 8 + 3] = Disc.WHITE
    board[4 * 8 + 4] = Disc.WHITE
    board[3 * 8 + 4] = Disc.BLACK
    board[4 * 8 + 3] = Disc.BLACK

    val current = Player.BLACK
    val moves = computeValidMoves(board, current)
    val (b, w) = countDiscs(board)

    return GameState(
        board = board,
        current = current,
        validMoves = moves,
        blackCount = b,
        whiteCount = w,
        gameOver = false,
        infoText = "",
        resultText = ""
    )
}

fun playMove(state: GameState, index: Int): GameState {
    if (state.gameOver) return state
    if (index !in 0..63) return state
    if (!state.validMoves[index]) return state

    val current = state.current
    val newBoard = applyMove(state.board, index, current)

    val (b, w) = countDiscs(newBoard)

    // Decide next turn (handle pass)
    val other = current.other()
    val otherMoves = computeValidMoves(newBoard, other)
    val otherHasMoves = otherMoves.any { it }

    return if (otherHasMoves) {
        GameState(
            board = newBoard,
            current = other,
            validMoves = otherMoves,
            blackCount = b,
            whiteCount = w,
            gameOver = false,
            infoText = "",
            resultText = ""
        )
    } else {
        val sameMoves = computeValidMoves(newBoard, current)
        val sameHasMoves = sameMoves.any { it }

        if (sameHasMoves) {
            // Opponent passes, current plays again
            GameState(
                board = newBoard,
                current = current,
                validMoves = sameMoves,
                blackCount = b,
                whiteCount = w,
                gameOver = false,
                infoText = "${other.label} has no valid moves. Pass!",
                resultText = ""
            )
        } else {
            // No one can move => game over
            val result = when {
                b > w -> "Black wins! ($b - $w)"
                w > b -> "White wins! ($w - $b)"
                else -> "Draw! ($b - $w)"
            }
            GameState(
                board = newBoard,
                current = current,
                validMoves = BooleanArray(64) { false },
                blackCount = b,
                whiteCount = w,
                gameOver = true,
                infoText = "",
                resultText = result
            )
        }
    }
}

private fun applyMove(board: IntArray, index: Int, player: Player): IntArray {
    val newBoard = board.copyOf()
    val me = player.disc
    val opp = if (me == Disc.BLACK) Disc.WHITE else Disc.BLACK

    newBoard[index] = me

    val row = index / 8
    val col = index % 8

    for ((dx, dy) in DIRS) {
        var x = col + dx
        var y = row + dy
        val toFlip = ArrayList<Int>(8)

        while (x in 0..7 && y in 0..7) {
            val idx = y * 8 + x
            val v = newBoard[idx]
            when (v) {
                opp -> toFlip.add(idx)
                me -> {
                    if (toFlip.isNotEmpty()) {
                        for (p in toFlip) newBoard[p] = me
                    }
                    break
                }
                else -> break
            }
            x += dx
            y += dy
        }
    }

    return newBoard
}

private fun computeValidMoves(board: IntArray, player: Player): BooleanArray {
    val me = player.disc
    val opp = if (me == Disc.BLACK) Disc.WHITE else Disc.BLACK

    val moves = BooleanArray(64) { false }

    for (i in 0 until 64) {
        if (board[i] != Disc.EMPTY) continue

        val r = i / 8
        val c = i % 8

        var valid = false
        for ((dx, dy) in DIRS) {
            var x = c + dx
            var y = r + dy
            var seenOpp = false

            while (x in 0..7 && y in 0..7) {
                val v = board[y * 8 + x]
                if (v == opp) {
                    seenOpp = true
                } else if (v == me) {
                    if (seenOpp) valid = true
                    break
                } else {
                    break
                }
                x += dx
                y += dy
            }
            if (valid) break
        }

        moves[i] = valid
    }

    return moves
}

private fun countDiscs(board: IntArray): Pair<Int, Int> {
    var b = 0
    var w = 0
    for (v in board) {
        if (v == Disc.BLACK) b++
        else if (v == Disc.WHITE) w++
    }
    return b to w
}
