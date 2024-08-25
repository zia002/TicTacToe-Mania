import kotlin.math.max
import kotlin.math.min

val MAX = Int.MAX_VALUE
val MIN = Int.MIN_VALUE
val patterns = mapOf("11111" to 30000000, "22222" to -30000000, "011110" to 20000000, "022220" to -20000000, "011112" to 50000, "211110" to 50000,
    "022221" to -50000, "122220" to -50000, "01110" to 30000, "02220" to -30000, "011010" to 15000, "010110" to 15000, "022020" to -15000, "020220" to -15000, "001112" to 2000,
    "211100" to 2000, "002221" to -2000, "122200" to -2000, "211010" to 2000, "210110" to 2000, "010112" to 2000, "011012" to 2000,
    "122020" to -2000, "120220" to -2000, "020221" to -2000, "022021" to -2000, "01100" to 500, "00110" to 500, "02200" to -500, "00220" to -500
)

fun getCoordsAround(boardSize: Int, board: Array<IntArray>): Pair<List<Int>, List<Int>> {
    val potentialValsCoord = mutableMapOf<Pair<Int, Int>, Int>()

    for (i in board.indices) {
        for (j in board[i].indices) {
            if (board[i][j] != 0) {
                val x = j
                val y = i

                if (y > 0) {
                    potentialValsCoord[(x to y - 1)] = 1
                    if (x > 0) {
                        potentialValsCoord[(x - 1 to y - 1)] = 1
                    }
                    if (x < boardSize - 1) {
                        potentialValsCoord[(x + 1 to y - 1)] = 1
                    }
                }
                if (x > 0) {
                    potentialValsCoord[(x - 1 to y)] = 1
                    if (y < boardSize - 1) {
                        potentialValsCoord[(x - 1 to y + 1)] = 1
                    }
                }
                if (y < boardSize - 1) {
                    potentialValsCoord[(x to y + 1)] = 1
                    if (x < boardSize - 1) {
                        potentialValsCoord[(x + 1 to y + 1)] = 1
                    }
                    if (x > 0) {
                        potentialValsCoord[(x - 1 to y + 1)] = 1
                    }
                }
                if (x < boardSize - 1) {
                    potentialValsCoord[(x + 1 to y)] = 1
                    if (y > 0) {
                        potentialValsCoord[(x + 1 to y - 1)] = 1
                    }
                }
            }
        }
    }

    val finalValsX = mutableListOf<Int>()
    val finalValsY = mutableListOf<Int>()

    for (key in potentialValsCoord.keys) {
        finalValsX.add(key.first)
        finalValsY.add(key.second)
    }

    return Pair(finalValsX, finalValsY)
}

fun convertArrToMove(row: Int, col: Int): String {
    val colVal = ('a' + col)
    val rowVal = (row + 1).toString()
    return "$colVal$rowVal"
}

fun convertMoveToArr(col: Char, row: String): Pair<Int, Int> {
    val colVal = col - 'a'
    val rowVal = row.toInt() - 1
    return colVal to rowVal
}

fun getRandomMove(board: Array<IntArray>, boardSize: Int): Pair<Int, Int> {
    var ctr = 0
    val idx = boardSize / 2
    while (ctr < (idx / 2)) {
        listOf(
            idx + ctr to idx + ctr,
            idx + ctr to idx - ctr,
            idx + ctr to idx,
            idx to idx + ctr,
            idx to idx - ctr,
            idx - ctr to idx,
            idx - ctr to idx - ctr,
            idx - ctr to idx + ctr
        ).forEach { (x, y) ->
            if (x in 0 until boardSize && y in 0 until boardSize && board[y][x] == 0) {
                return x to y
            }
        }
        ctr++
    }
    for (i in board.indices) {
        for (j in board[i].indices) {
            if (board[i][j] == 0) {
                return j to i
            }
        }
    }
    return -1 to -1 // should not reach here
}

fun btsConvert(board: Array<IntArray>, player: Int): List<String> {
    val cList = mutableListOf<String>()
    val rList = mutableListOf<String>()
    val dList = mutableListOf<String>()

    val boardSize = board.size

    // Diagonals from top-left to bottom-right
    for (i in -boardSize + 5 until boardSize - 4) {
        val bdiagVals = StringBuilder()
        for (j in 0 until boardSize) {
            val x = j
            val y = i + j
            if (x in 0 until boardSize && y in 0 until boardSize) {
                bdiagVals.append(
                    when (board[y][x]) {
                        0 -> "0"
                        player -> "1"
                        else -> "2"
                    }
                )
            }
        }
        if (bdiagVals.isNotEmpty()) dList.add(bdiagVals.toString())
    }

    // Diagonals from bottom-left to top-right
    for (i in -boardSize + 5 until boardSize - 4) {
        val fdiagVals = StringBuilder()
        for (j in 0 until boardSize) {
            val x = j
            val y = i + j
            if (x in 0 until boardSize && y in 0 until boardSize) {
                fdiagVals.append(
                    when (board[boardSize - 1 - y][x]) {
                        0 -> "0"
                        player -> "1"
                        else -> "2"
                    }
                )
            }
        }
        if (fdiagVals.isNotEmpty()) dList.add(fdiagVals.toString())
    }

    // Columns
    for (col in board.indices) {
        val colVals = StringBuilder()
        for (row in board.indices) {
            colVals.append(
                when (board[row][col]) {
                    0 -> "0"
                    player -> "1"
                    else -> "2"
                }
            )
        }
        cList.add(colVals.toString())
    }

    for (row in board.indices) {
        val rowVals = StringBuilder()
        for (col in board[row].indices) {
            rowVals.append(
                when (board[row][col]) {
                    0 -> "0"
                    player -> "1"
                    else -> "2"
                }
            )
        }
        rList.add(rowVals.toString())
    }

    return dList + cList + rList
}

fun points(board: Array<IntArray>, player: Int): Int {
    var value = 0
    val playerStrArr = btsConvert(board, player)

    for (str in playerStrArr) {
        val len1 = str.length
        for (j in 0 until len1) {
            val n5 = j + 5
            if (n5 <= len1) {
                val st5 = str.substring(j, n5)
                if (patterns.containsKey(st5)) {
                    value += patterns[st5] ?: 0
                }
            }
        }
        for (j in 0 until len1) {
            val n6 = j + 6
            if (n6 <= len1) {
                val st6 = str.substring(j, n6)
                if (patterns.containsKey(st6)) {
                    value += patterns[st6] ?: 0
                }
            }
        }
    }
    return value
}

fun otherPlayerStone(player: Int): Int {
    return if (player == 1) 2 else 1
}

fun minimax(board: Array<IntArray>, isMaximizer: Boolean, depth: Int, alpha: Int, beta: Int, player: Int): Int {
    val point = points(board, player)
    if (depth == 4 || point >= 20000000 || point <= -20000000) {
        return point
    }
    var alpha = alpha
    var beta = beta
    var best = if (isMaximizer) MIN else MAX
    val potentialVals = getCoordsAround(board.size, board)

    for (i in potentialVals.first.indices) {
        val x = potentialVals.first[i]
        val y = potentialVals.second[i]
        if (board[y][x] == 0) {
            val newBoard = Array(board.size) { board[it].copyOf() }
            newBoard[y][x] = if (isMaximizer) player else otherPlayerStone(player)
            val score = minimax(newBoard, !isMaximizer, depth + 1, alpha, beta, player)
            if (isMaximizer) {
                if (score > best) best = score
                alpha = max(alpha, best)
                if (beta <= alpha) break
            } else {
                if (score < best) best = score
                beta = min(beta, best)
                if (beta <= alpha) break
            }
        }
    }
    return best
}

fun computer(boardSize: Int, board: Array<IntArray>, isComputerFirst: Boolean): Pair<String, Array<IntArray>> {
    var mostPoints = MIN
    var alpha = MIN
    var beta = MAX
    val mark = if (isComputerFirst) 1 else 2
    var bestMoveRow = -1
    var bestMoveCol = -1

    val potentialVals = getCoordsAround(boardSize, board)
    for (i in potentialVals.first.indices) {
        val x = potentialVals.first[i]
        val y = potentialVals.second[i]
        if (board[y][x] == 0) {
            val newBoard = Array(board.size) { board[it].copyOf() }
            newBoard[y][x] = mark
            val movePoints = max(mostPoints, minimax(newBoard, false, 1, alpha, beta, mark))
            alpha = max(alpha, movePoints)
            if (movePoints > mostPoints) {
                bestMoveRow = y
                bestMoveCol = x
                mostPoints = movePoints
                if (movePoints >= 20000000) break
            }
        }
    }

    if (bestMoveRow == -1 || bestMoveCol == -1) {
        val (x, y) = getRandomMove(board, boardSize)
        return convertArrToMove(y, x) to board.apply { this[y][x] = mark }
    }

    val updatedBoard = Array(board.size) { board[it].copyOf() }
    updatedBoard[bestMoveRow][bestMoveCol] = mark
    return convertArrToMove(bestMoveRow, bestMoveCol) to updatedBoard
}

fun playGame(humanFirst: Boolean, boardSize: Int, board: Array<IntArray>) {
    var playing = true
    var moveNum = 0
    while (playing) {
        if (humanFirst) {
            while (true) {
                print("move: ")
                val input = readLine()?.trim() ?: continue
                val (col, row) = input[0] to input.substring(1)
                if (col.isLetter() && row.all { it.isDigit() }) {
                    val (colVal, rowVal) = convertMoveToArr(col, row)
                    if (colVal in 0 until boardSize && rowVal in 0 until boardSize && board[rowVal][colVal] == 0) {
                        board[rowVal][colVal] = 1
                        break
                    } else {
                        println("Error invalid move: please try again")
                    }
                } else {
                    println("Error invalid move: please try again")
                }
            }
            moveNum++
            val (move, updatedBoard) = computer(boardSize, board, false)
            println("Move played: $move")
            for (i in board.indices) {
                for (j in board[i].indices) {
                    board[i][j] = updatedBoard[i][j]
                }
            }
        }
    }
}

fun main() {
    val humanFirst = true
    val size = 9
    val board = Array(size) { IntArray(size) }
    playGame(humanFirst, size, board)
}
