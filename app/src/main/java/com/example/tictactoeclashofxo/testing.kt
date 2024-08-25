val gloBoard = CharArray(9) { (it + 1).toChar() }
val loBoards = Array(9) { CharArray(9) { (it + 1).toChar() } }

val comPlayer = 'O'
val humPlayer = 'X'
fun emptyGloIndices(gloBoard: CharArray): List<Int> {
    return gloBoard.indices.filter { gloBoard[it] !in arrayOf('X', 'O', 'D', 'N') }
}
fun emptyLoIndices(openBoards: List<Int>, loBoards: Array<CharArray>): List<List<Int>> {
    return openBoards.map { index ->
        loBoards[index].indices.filter { loBoards[index][it] !in arrayOf('X', 'O') }
    }
}
fun winning(board: CharArray, player: Char): Boolean {
    return (board[0] == player && board[1] == player && board[2] == player ||
            board[3] == player && board[4] == player && board[5] == player ||
            board[6] == player && board[7] == player && board[8] == player ||
            board[0] == player && board[3] == player && board[6] == player ||
            board[1] == player && board[4] == player && board[7] == player ||
            board[2] == player && board[5] == player && board[8] == player ||
            board[0] == player && board[4] == player && board[8] == player ||
            board[2] == player && board[4] == player && board[6] == player)
}
fun allXorO(board: CharArray): Boolean {
    return board.all { it == 'X' || it == 'O' }
}
fun evalBoard(current: Int, los: Array<CharArray>): Int {
    val allWinningCombos = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )
    val positionScores = listOf(0.3, 0.2, 0.3, 0.2, 0.4, 0.2, 0.3, 0.2, 0.3)
    val loBoardWeightings = listOf(1.35, 1.0, 1.35, 1.0, 1.7, 1.0, 1.35, 1.0, 1.35)

    fun rowScore(arr: CharArray): Int {
        val oCount = arr.count { it == 'O' }
        val xCount = arr.count { it == 'X' }
        val numCount = arr.count { it !in arrayOf('O', 'X') }

        return when {
            oCount == 3 -> -12
            oCount == 2 && numCount == 1 -> -6
            xCount == 2 && numCount == 1 -> 6
            xCount == 2 && oCount == 1 -> -9
            xCount == 3 -> 12
            xCount == 1 && oCount == 2 -> 9
            else -> 0
        }
    }

    var score = 0
    val glo = CharArray(9) { i ->
        when {
            winning(loBoards[i], comPlayer) -> {
                score -= (positionScores[i] * 150).toInt()
                'O'
            }
            winning(loBoards[i], humPlayer) -> {
                score += (positionScores[i] * 150).toInt()
                'X'
            }
            allXorO(loBoards[i]) -> 'D'
            else -> i.toChar()
        }
    }

    if (winning(glo, comPlayer)) {
        score -= 50000
    } else if (winning(glo, humPlayer)) {
        score += 50000
    }

    for (i in 0 until 9) {
        for (j in 0 until 9) {
            when (los[i][j]) {
                comPlayer -> {
                    score -= if (i == current) {
                        (positionScores[j] * 1.5 * loBoardWeightings[i]).toInt()
                    } else {
                        (positionScores[j] * loBoardWeightings[i]).toInt()
                    }
                }
                humPlayer -> {
                    score += if (i == current) {
                        (positionScores[j] * 1.5 * loBoardWeightings[i]).toInt()
                    } else {
                        (positionScores[j] * loBoardWeightings[i]).toInt()
                    }
                }
            }
        }

        val rawScores = mutableSetOf<Int>()
        for (combo in allWinningCombos) {
            val loArr = charArrayOf(los[i][combo[0]], los[i][combo[1]], los[i][combo[2]])
            val rowScoreVal = rowScore(loArr)
            if (rowScoreVal !in rawScores) {
                if ((combo[0] == 0 && combo[1] == 4 && combo[2] == 8) || (combo[0] == 2 && combo[1] == 4 && combo[2] == 6)) {
                    if (rowScoreVal == 6 || rowScoreVal == -6) {
                        score += (rowScoreVal * 1.2 * 1.5 * loBoardWeightings[i]).toInt()
                    }
                } else {
                    score += if (i == current) {
                        (rowScoreVal * 1.5 * loBoardWeightings[i]).toInt()
                    } else {
                        (rowScoreVal * loBoardWeightings[i]).toInt()
                    }
                }
                rawScores.add(rowScoreVal)
            }
        }
    }

    val rawScores = mutableSetOf<Int>()
    for (combo in allWinningCombos) {
        val gloArr = charArrayOf(glo[combo[0]], glo[combo[1]], glo[combo[2]])
        val rowScoreVal = rowScore(gloArr)
        if (rowScoreVal !in rawScores) {
            if ((combo[0] == 0 && combo[1] == 4 && combo[2] == 8) || (combo[0] == 2 && combo[1] == 4 && combo[2] == 6)) {
                if (rowScoreVal == 6 || rowScoreVal == -6) {
                    score += (rowScoreVal * 1.2 * 150).toInt()
                }
            } else {
                score += (rowScoreVal * 150).toInt()
            }
            rawScores.add(rowScoreVal)
        }
    }
    return score
}
data class Result(val score: Int, val move: Move?)
data class Move(val gloIndex: Int, val loIndex: Int) {
    var score: Int = 0
}
fun minimax(mo: Move, los: Array<CharArray>, player: Char, depth: Int, alphaVal: Int, betaVal: Int, maxDepth: Int): Result {
    var alpha=alphaVal
    var beta=betaVal
    val score = evalBoard(mo.gloIndex, los)
    if (depth == maxDepth) {
        return Result(score, null)
    }
    val gloBoardMinimax = CharArray(9) { ' ' }
    for (i in 0 until 9) {
        gloBoardMinimax[i] = when {
            winning(los[i], comPlayer) -> 'O'
            winning(los[i], humPlayer) -> 'X'
            allXorO(los[i]) -> 'D'
            else -> i.toChar()
        }
    }

    if (winning(gloBoardMinimax, comPlayer)) {
        return Result(score + depth, null)
    } else if (winning(gloBoardMinimax, humPlayer)) {
        return Result(score - depth, null)
    }

    if (gloBoardMinimax[mo.loIndex] in '0'..'8') {
        for (j in 0 until 9) {
            if (gloBoardMinimax[j] in '0'..'8') {
                gloBoardMinimax[j] = 'N'
            }
        }
    }

    if (gloBoardMinimax[mo.loIndex] == 'N') {
        gloBoardMinimax[mo.loIndex] = mo.loIndex.toChar()
    }

    val openBoardsMinimax = emptyGloIndices(gloBoardMinimax)
    if (openBoardsMinimax.isEmpty()) {
        return Result(score, null)
    }

    val emptySpotsInLoBoards = emptyLoIndices(openBoardsMinimax, los)

    return if (player == humPlayer) {
        var maxVal = Int.MIN_VALUE
        var bestMove: Move? = null
        for (o in openBoardsMinimax.indices) {
            for (i in emptySpotsInLoBoards[o].indices) {
                val move = Move(openBoardsMinimax[o], emptySpotsInLoBoards[o][i])
                los[move.gloIndex][move.loIndex] = 'X'
                val result = minimax(move, los, comPlayer, depth + 1, alpha, beta, maxDepth)
                los[move.gloIndex][move.loIndex] = move.loIndex.toChar()
                if (result.move != null && result.score > maxVal) {
                    maxVal = result.score
                    bestMove = move
                }
                alpha = maxOf(alpha, maxVal)
                if (beta <= alpha) {
                    break
                }
            }
        }
        Result(maxVal, bestMove)
    } else {
        var minVal = Int.MAX_VALUE
        var bestMove: Move? = null
        for (o in openBoardsMinimax.indices) {
            for (i in emptySpotsInLoBoards[o].indices) {
                val move = Move(openBoardsMinimax[o], emptySpotsInLoBoards[o][i])
                los[move.gloIndex][move.loIndex] = 'O'
                val result = minimax(move, los, humPlayer, depth + 1, alpha, beta, maxDepth)
                los[move.gloIndex][move.loIndex] = move.loIndex.toChar()
                if (result.move != null && result.score < minVal) {
                    minVal = result.score
                    bestMove = move
                }
                beta = minOf(beta, minVal)
                if (beta <= alpha) {
                    break
                }
            }
        }
        Result(minVal, bestMove)
    }
}
var turn=0
fun gloBoardIndex(nextBoardIndex: Int) {
    for (i in 0 until 9) {
        gloBoard[i] = when {
            winning(loBoards[i], comPlayer) -> 'O'
            winning(loBoards[i], humPlayer) -> 'X'
            allXorO(loBoards[i]) -> 'D'
            gloBoard[i]!='O' || gloBoard[i]!='X' || gloBoard[i]!='D' -> 'N'
            else -> gloBoard[i]
        }
    }
    if (gloBoard[nextBoardIndex] != 'N') {
        for (i in 0 until 9) {
            if (gloBoard[i] == 'N') {
                gloBoard[i] = i.toChar()
            }
        }
    } else {
        gloBoard[nextBoardIndex] = nextBoardIndex.toChar()
    }
}
val openBoards: List<Int> = List(9) { it }
fun aiPlayer() {
    println("AI Player is running...")
        val emptySpotsInLoBoards = emptyLoIndices(openBoards, loBoards)
        var minimumScore = Int.MAX_VALUE
        var bestMove: Move? = null
        for (o in openBoards.indices) {
            for (i in emptySpotsInLoBoards[o].indices) {
                val move = Move(openBoards[o], emptySpotsInLoBoards[o][i])
                val originalValue = loBoards[move.gloIndex][move.loIndex]
                loBoards[move.gloIndex][move.loIndex] = 'O'
                val result =minimax(move, loBoards, humPlayer, 0, Int.MIN_VALUE, Int.MAX_VALUE, 2)
                loBoards[move.gloIndex][move.loIndex] = originalValue
                if (result.score < minimumScore) {
                    minimumScore = result.score
                    bestMove = move.apply { score = result.score }
                }
            }
        }
        bestMove?.let {
            println("Global board index: ${it.gloIndex}")
            println("Local board index: ${it.loIndex}")
        }
}



fun main(){
    loBoards[4][2] = 'X'
    //printBoards()
    aiPlayer()
    println(loBoards[0][0])
    loBoards[0][4]='X'
    aiPlayer()
}
fun printBoards() {
    println("Global Board:")
    println(gloBoard.joinToString(" "))
    for (i in loBoards.indices) {
        println("Local Board $i:")
        println(loBoards[i].joinToString(" "))
    }
}