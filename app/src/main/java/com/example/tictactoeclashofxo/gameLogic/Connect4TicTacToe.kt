package com.example.tictactoeclashofxo.gameLogic

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.WinnerActivity
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.Task
import com.example.tictactoeclashofxo.databinding.ActivityConnect4TicTacToeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Stack
import kotlin.random.Random
import kotlin.random.nextInt

class Connect4TicTacToe : AppCompatActivity() {
    private lateinit var binding:ActivityConnect4TicTacToeBinding
    private var board = Array(6) {CharArray(7) {'-'} }
    private lateinit var buttonList:Array<ImageView>
    private lateinit var buttonList2:Array<ImageView>
    private lateinit var player1Timer: Chronometer
    private lateinit var player2Timer: Chronometer
    private var player1Offset:Long=0
    private var player2Offset:Long=0
    private var player1Item:Int=0
    private var player2Item:Int=0
    private var player1Image:Int=0
    private var player2Image:Int=0
    private var player1Name:String?=""
    private var player2Name:String?=""
    private var type:Int=0
    private var whoseTurn:Int=0
    private var background:Int=0
    private var turn=0
    private var putSound: MediaPlayer? =null
    private var winSound:MediaPlayer?=null
    private var loseSound:MediaPlayer?=null
    private var soundOn="0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityConnect4TicTacToeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        background=intent.getIntExtra("BACKGROUND",0)
        type=intent.getIntExtra("TYPE",0)
        val myPref= SessionManager(this)
        turn=intent.getIntExtra("TURN",0)
        player1Name= myPref.getDetails("PLAYER1")
        player2Name= myPref.getDetails("PLAYER2")
        buttonList = arrayOf(
            binding.b00, binding.b01, binding.b02, binding.b03, binding.b04, binding.b05, binding.b06,
            binding.b10, binding.b11, binding.b12, binding.b13, binding.b14, binding.b15,binding.b16,
            binding.b20, binding.b21, binding.b22, binding.b23, binding.b24, binding.b25, binding.b26,
            binding.b30, binding.b31, binding.b32, binding.b33, binding.b34, binding.b35, binding.b36,
            binding.b40, binding.b41, binding.b42, binding.b43, binding.b44, binding.b45, binding.b46,
            binding.b50, binding.b51, binding.b52, binding.b53, binding.b54, binding.b55, binding.b56
        )
        buttonList2= arrayOf(binding.b00, binding.b01, binding.b02, binding.b03, binding.b04, binding.b05, binding.b06)
        player1Timer=binding.player1Timer;player1Timer.stop()
        player2Timer=binding.player2Timer;player2Timer.stop()

        putSound=MediaPlayer.create(this,R.raw.put_item)
        winSound=MediaPlayer.create(this,R.raw.win_game)
        loseSound=MediaPlayer.create(this,R.raw.lose_game)
        soundOn=myPref.getDetails("SOUND").toString()
        //----------- here we show the initial dialog about the match,who's turn first and who will have circle or cross ------------//
        val dialog= Dialog(this)
        val tempParent = FrameLayout(this)
        val customDialog=layoutInflater.inflate(R.layout.game_start,tempParent,false)
        dialog.setContentView(customDialog)
        val player1img=dialog.findViewById<ImageView>(R.id.player1Img)
        val player2img=dialog.findViewById<ImageView>(R.id.player2Img)
        val player1name=dialog.findViewById<TextView>(R.id.player1Name)
        val player2name=dialog.findViewById<TextView>(R.id.player2Name)
        val whoFirst=dialog.findViewById<TextView>(R.id.whoFirst)
        val player1item=dialog.findViewById<ImageView>(R.id.player1Item)
        val player2item=dialog.findViewById<ImageView>(R.id.player2Item)
        //------------ random number task here -----------//
        val randomTurn= Random.nextInt(0,10) //---> if even player1 turn first
        val randomItem= Random.nextInt(0,10) //---> if even player1 have cross
        //------------ select value will be 1 for bot play and 0 for trainer play ---------//
        binding.main.setBackgroundResource(background)
        player2Image=myPref.getDP()
        player2img.setImageResource(player2Image)
        //------------ start the game mode,first show the dialog then after start the game --------------//
        if(turn==0) {
            turn = if(randomTurn%2!=0) 0
            else 1
        }
        whoseTurn=turn
        when(type){
            1->{
                //======= this the trainer mode where bot and random play will be executed ========//
                //------- initial task of set the value in their placeholder -------//
                player1name.text=getString(R.string.me)
                player2name.text=myPref.getName()
                player1Name=getString(R.string.me)
                player2Name=myPref.getName()
                if(turn%2!=0) whoFirst.text=getString(R.string.tf,player1Name)
                else whoFirst.text=getString(R.string.tf,player2Name)
                binding.player1Name.text=player1Name
                binding.player2Name.text=player2Name
                binding.player1Image.setImageResource(R.drawable.person)
                binding.player2Image.setImageResource(R.drawable.person)
                player1img.setImageResource(player1Image)
                if(randomItem%2==0){
                    player1Item= Task.getRandomCross()
                    player1item.setImageResource(player1Item)
                    player2Item=myPref.getCircle()
                    player2item.setImageResource(player2Item)
                }else{
                    player1Item= Task.getRandomCircle()
                    player1item.setImageResource(player1Item)
                    player2Item=myPref.getCross()
                    player2item.setImageResource(player2Item)
                }
                Task.showDialogPerfect(dialog)
                //------- start here the function call ---------//
                //------- start here the function call ---------//
                job1=lifecycleScope.launch {
                    delay(3000)
                    dialog.dismiss()
                    play(turn)
                }
            }
            2->{
                //======= this the  local play mode  ========//
                //------- initial task of set the value in their placeholder -------//
                if(randomItem%2==0){
                    player1Item=myPref.getCross()
                    player1item.setImageResource(player1Item)
                    player2Item=myPref.getCircle()
                    player2item.setImageResource(player2Item)
                }else{
                    player1Item=myPref.getCircle()
                    player1item.setImageResource(player1Item)
                    player2Item=myPref.getCross()
                    player2item.setImageResource(player2Item)
                }
                player1name.text=player1Name
                player2name.text=player2Name
                if(turn%2!=0) whoFirst.text=getString(R.string.tf,player1Name)
                else whoFirst.text=getString(R.string.tf,player2Name)
                binding.player1Name.text=player1Name
                binding.player2Name.text=player2Name
                binding.player1Image.setImageResource(R.drawable.person)
                binding.player2Image.setImageResource(R.drawable.person)
                player1Image=myPref.getDP()
                player1img.setImageResource(player1Image)
                Task.showDialogPerfect(dialog)
                //------- start here the function call ---------//
                //------- start here the function call ---------//
                job1=lifecycleScope.launch {
                    delay(3000)
                    dialog.dismiss()
                    play(turn)
                }
            }
            3->{
                //======= this the  bot mode where always optimal play will be executed ========//
                //------- initial task of set the value in their placeholder -------//
                player1name.text=getString(R.string.bot)
                player2name.text=myPref.getName()
                player1Name="Bot"
                player2Name=myPref.getName()
                if(turn%2!=0) whoFirst.text=getString(R.string.tf,player1Name)
                else whoFirst.text=getString(R.string.tf,player2Name)
                binding.player1Name.text=player1Name
                binding.player2Name.text=player2Name
                binding.player1Image.setImageResource(R.drawable.bot_dp)
                binding.player2Image.setImageResource(R.drawable.person)
                player1Image=R.drawable.bot_dp
                player1img.setImageResource(player1Image)
                if(randomItem%2==0){
                    player1Item= Task.getRandomCross()
                    player1item.setImageResource(player1Item)
                    player2Item=myPref.getCircle()
                    player2item.setImageResource(player2Item)

                }else{
                    player1Item= Task.getRandomCircle()
                    player1item.setImageResource(player1Item)
                    player2Item=myPref.getCross()
                    player2item.setImageResource(player2Item)
                }
                Task.showDialogPerfect(dialog)
                //------- start here the function call ---------//
                //------- start here the function call ---------//
                job1=lifecycleScope.launch {
                    delay(3000)
                    dialog.dismiss()
                    play(turn)
                }
            }
        }
    }
    //=========== some global variable which are need to run the game ============//
    private var player1First=0
    private var player2First=0
    private var job1: Job? = null
    private var putValueJob: Job?=null
    private fun play(turn:Int){
        job1?.cancel()
        putValueJob?.cancel()
        val result=checkWinner(board)
        if(result.first!=-1){
            cancelClick()
            val time1=(SystemClock.elapsedRealtime()-player1Timer.base).toString()
            val time2=(SystemClock.elapsedRealtime()-player2Timer.base).toString()
            val winner: String
            val loser: String
            val winnerTime: String
            val loserTime: String

            if(result.first==1){
                winner=player1Name.toString()
                loser=player2Name.toString()
                winnerTime=time1
                loserTime=time2
                winMove(result.second)
                if (soundOn=="1") loseSound?.start()
            }
            else if(result.first==2){
                winner=player2Name.toString()
                loser=player1Name.toString()
                winnerTime=time2
                loserTime=time1
                winMove(result.second)
                if (soundOn=="1") winSound?.start()
            }
            else{
                if(time1>=time2){
                    winner=player2Name.toString()
                    loser=player1Name.toString()
                    winnerTime=time2
                    loserTime=time1
                    if (soundOn=="1") winSound?.start()
                }
                else{
                    winner=player1Name.toString()
                    loser=player2Name.toString()
                    winnerTime=time1
                    loserTime=time2
                    if (soundOn=="1") loseSound?.start()
                }
            }
            var winCoin=-1
            var loseCoin=-1
            if(type==1){//-----Trainer------//
                if(result.first==0) { //----draw----//
                    winCoin = Random.nextInt(5..10)
                    loseCoin=Random.nextInt(2..5)
                }else{   //----win----//
                    winCoin=Random.nextInt(100..200)
                    loseCoin=Random.nextInt(2..5)
                }
            }
            else if(type==3){//-----Bot------//
                if(result.first==0) {//----draw----//
                    winCoin = Random.nextInt(15..30)
                    loseCoin=Random.nextInt(5..10)
                }else{  //----win----//
                    winCoin=Random.nextInt(150..300)
                    loseCoin=Random.nextInt(5..10)
                }
            }
            val intent= Intent(this, WinnerActivity::class.java)
            intent.putExtra("WinnerName",winner)
            intent.putExtra("LoserName",loser)
            intent.putExtra("WinTime",winnerTime)
            intent.putExtra("LoseTime",loserTime)
            intent.putExtra("WinCoin",winCoin)
            intent.putExtra("LoseCoin",loseCoin)

            intent.putExtra("BACKGROUND",background)
            intent.putExtra("TYPE",type)
            intent.putExtra("TURN",whoseTurn)
            intent.putExtra("MODE",5)
            job1=lifecycleScope.launch {
                delay(3000)
                startActivity(intent)
                finish()
            }
        }
        else if(turn%2!=0){
            cancelItemClick(1)
            binding.player1Bg.setBackgroundResource(R.drawable.select_bg)
            binding.player2Bg.background=null
            //======= here control the player1 turns , type=1> trained mode,type=2>local mode,type=3>bot mode ========//
            if(player1First==0) {
                player1Timer.base= SystemClock.elapsedRealtime()
                player1Offset=0
                player1First=1
            }
            player1Timer.start()
            player1Timer.base = SystemClock.elapsedRealtime()-player1Offset
            player2Timer.stop()
            player2Offset= SystemClock.elapsedRealtime()-player2Timer.base
            //---------------------to control the action------------------//
            if(type==2) {
                //----------------- it is only for the local play mode ---------------//
                var player1InTime = 0
                job1 = lifecycleScope.launch {
                    putValueJob = launch {
                        put(1) {
                            player1InTime = 1
                            cancelItemClick(2)
                            changeTurn(0)
                            play(turn + 1)
                        }
                    }
//                    delay(5010)
//                    if (player1InTime == 0) {
//                        job1?.cancel()
//                        putValueJob!!.cancel()
//                        changeTurn(2)
//                        cancelItemClick(0)
//                        play(turn + 1)
//                    }
                }
            }
            else{
                cancelItemClick(0)
                //----it is for the bot and trained mode,return the row and column where to put the item ---------//
                if(type==1){
                    //------------ trained mode control here ---------------//
                    val delayTime=Task.getRandomTime()
                    val res=trainer()
                    job1=lifecycleScope.launch {
                        delay(delayTime)
                        board[res.first][res.second]='1'
                        buttonList[(res.first*7)+res.second].setImageResource(player1Item)
                        if(soundOn=="1") putSound?.start()
                        changeTurn(2)
                        play(turn + 1)
                    }
                }else{
                    //------------ bot mode control here ---------------//
//                    val delayTime=Task.getRandomTime()
//                    val b=board
//                    job1=lifecycleScope.launch {
//                        val res=bot(b,5)
//                        delay(delayTime)
//                        board[res.first][res.second]='1'
//                        buttonList[(res.first*7)+res.second].setImageResource(player1Item)
//                        if(soundOn=="1") putSound?.start()
//                        changeTurn(2)
//                        play(turn + 1)
//                    }
                    CoroutineScope(Dispatchers.Default).launch {
                        val b=board
                        val res=bot(b,8)
                        board[res.first][res.second]='1'
                        buttonList[(res.first*7)+res.second].setImageResource(player1Item)
                        if(soundOn=="1") putSound?.start()
                        changeTurn(2)
                        play(turn + 1)
                    }
                }
            }
        }
        else{
            binding.player2Bg.setBackgroundResource(R.drawable.select_bg)
            binding.player1Bg.background=null
            //======= here control the player2 turns ========//
            if(player2First==0) {
                player2Timer.base= SystemClock.elapsedRealtime()
                player2Offset=0
                player2First=1
            }
            player1Timer.stop()
            player1Offset= SystemClock.elapsedRealtime()-player1Timer.base
            player2Timer.start()
            player2Timer.base = SystemClock.elapsedRealtime()-player2Offset
            var player2InTime=0
            job1=lifecycleScope.launch {
                putValueJob=launch{
                    put(2) {
                        player2InTime=1
                        changeTurn(1)
                        play(turn +1)
                    }
                }
//                delay(5010)
//                if(player2InTime==0){
//                    job1?.cancel()
//                    putValueJob!!.cancel()
//                    changeTurn(1)
//                    play(turn+1)
//                }
            }
        }
    }
    private fun winMove(list: List<Int>?) {
        for(i in 0..<list?.size!!){
            buttonList[list[i]].setBackgroundResource(R.drawable.win_bg)
        }
    }
    private fun put(turn: Int, callback: () -> Unit) {
        for ((index, imageView) in buttonList.withIndex()) {
            val column = index % 7
            imageView.setOnClickListener {
                if(board[0][column]=='-'){
                    for(i in 5 downTo 0){
                        if(board[i][column]=='-'){
                            val id= i*7 + column
                            buttonList[id].apply {
                                if(turn==1) {
                                    setImageResource(player1Item)
                                    if(soundOn=="1") putSound?.start()
                                    board[i][column]='1'
                                }
                                else {
                                    setImageResource(player2Item)
                                    if(soundOn=="1") putSound?.start()
                                    board[i][column]='2'
                                }
                            }
                            break
                        }
                    }
                    callback()
                }
            }
        }
    }
    private fun changeTurn(turn: Int){
        if(turn==1){
            player1Timer.start()
            player2Timer.stop()
        }
        else{
            player2Timer.start()
            player1Timer.stop()
        }
    }
    private fun trainer():Pair<Int,Int>{
        val randDepth= listOf(5,5,4,1,5,1,1,5,4,5,4,5,4,1,5,1,5,5,1,5,5,4,5,5,5,4)
        val depth=randDepth.random()
        // return bot(board,depth)
        return Pair(1,2)
    }
    private suspend fun bot(b: Array<CharArray>,depth: Int): Pair<Int, Int> {
        val res=putCheck()
        if(res.first!=-1){
            return Pair(res.second,res.third)
        }
        val aiMove = miniMaxAlphaBeta(b, depth, '1')
        for(i in 5 downTo 0){
            if(b[i][aiMove]=='-'){
                return Pair(i,aiMove)
            }
        }
        return Pair(0,0)
    }
    private fun cancelClick(){
        binding.player1Bg.background=null
        binding.player2Bg.background=null
        player1Timer.stop()
        player2Timer.stop()
        for(imageView in buttonList) imageView.isClickable=false
    }
    private fun checkWinner(board: Array<CharArray>): Pair<Int, List<Int>?> {
        fun toPosition(row: Int, col: Int): Int = row * 7 + col
        for (r in 0..5) {
            for (c in 0..3) {
                val current = board[r][c]
                if (current != '-' && current == board[r][c + 1] && current == board[r][c + 2] && current == board[r][c + 3]) {
                    return Pair(current.digitToInt(), listOf(toPosition(r, c), toPosition(r, c + 1), toPosition(r, c + 2), toPosition(r, c + 3)))
                }
            }
        }
        for (c in 0..6) {
            for (r in 0..2) {
                val current = board[r][c]
                if (current != '-' && current == board[r + 1][c] && current == board[r + 2][c] && current == board[r + 3][c]) {
                    return Pair(current.digitToInt(), listOf(toPosition(r, c), toPosition(r + 1, c), toPosition(r + 2, c), toPosition(r + 3, c)))
                }
            }
        }
        for (r in 0..2) {
            for (c in 0..3) {
                val current = board[r][c]
                if (current != '-' && current == board[r + 1][c + 1] && current == board[r + 2][c + 2] && current == board[r + 3][c + 3]) {
                    return Pair(current.digitToInt(), listOf(toPosition(r, c), toPosition(r + 1, c + 1), toPosition(r + 2, c + 2), toPosition(r + 3, c + 3)))
                }
            }
        }
        for (r in 3..5) {
            for (c in 0..3) {
                val current = board[r][c]
                if (current != '-' && current == board[r - 1][c + 1] && current == board[r - 2][c + 2] && current == board[r - 3][c + 3]) {
                    return Pair(current.digitToInt(), listOf(toPosition(r, c), toPosition(r - 1, c + 1), toPosition(r - 2, c + 2), toPosition(r - 3, c + 3)))
                }
            }
        }
        if (board.all { row -> '-' !in row }) {
            return Pair(0, null)
        }
        return Pair(-1, null)
    }
    private fun putCheck(): Triple<Int, Int, Int> {
        for(c in 0..6){
            if(board[0][c]=='-'){
                for(i in 5 downTo 0) {
                    if (board[i][c] == '-') {
                        //=== check for bot ====//
                        board[i][c]='1'
                        var res=checkWinner(board)
                        board[i][c]='-'
                        if (res.first==1) return Triple(1,i,c)
                        //=== check for user ===//
                        board[i][c] = '2'
                        res=checkWinner(board)
                        board[i][c]='-'
                        if(res.first==2) return Triple(2, i, c)
                        break
                    }
                }
            }
        }
        return Triple(-1,-1,-1)
    }
    private fun cancelItemClick(on:Int){
            for (i in buttonList) {
                 if(on==1) i.isClickable = true
                 else i.isClickable=false
            }
    }
    override fun onDestroy() {
        super.onDestroy()
        job1?.cancel()
        putValueJob?.cancel()
        putSound?.release()
        winSound?.release()
        loseSound?.release()
        winSound=null
        loseSound=null
        putSound=null
    }
    //====== here we implement the minimax and bot play e mode here ========//
    private fun miniMaxAlphaBeta(board: Array<CharArray>, depth: Int, player: Char): Int {
        println(Thread.currentThread())
        val validMoves = getValidMoves(board).toMutableList()
        validMoves.shuffle()
        var bestMove = validMoves[0]
        var bestScore = Float.NEGATIVE_INFINITY
        val alpha = Float.NEGATIVE_INFINITY
        val beta = Float.POSITIVE_INFINITY
        val opponent = if (player == '1') '2' else '1'
        for (move in validMoves) {
            val tempBoard = makeMove(board, move, player).first
            val boardScore = minimizeBeta(tempBoard, depth - 1, alpha, beta, player, opponent)
            if (boardScore > bestScore) {
                bestScore = boardScore
                bestMove = move
            }
        }
        return bestMove
    }
    private fun minimizeBeta(board: Array<CharArray>, depth: Int, alpha: Float, beta: Float, player: Char, opponent: Char): Float {
        val validMoves = mutableListOf<Int>()
        for (col in 0 ..6) {
            if (isValidMove(col, board)) {
                val temp = makeMove(board, col, player).second
                validMoves.add(temp)
            }
        }
        if (depth == 0 || validMoves.isEmpty() || gameIsOver(board)) {
            return utilityValue(board, player)
        }
        var betaValue = beta
        for (move in getValidMoves(board)) {
            var boardScore = Float.POSITIVE_INFINITY
            if (alpha < betaValue) {
                val tempBoard = makeMove(board, move, opponent).first
                boardScore = maximizeAlpha(tempBoard, depth - 1, alpha, betaValue, player, opponent)
            }
            if (boardScore < betaValue) betaValue = boardScore
        }
        return betaValue
    }
    private fun maximizeAlpha(board: Array<CharArray>, depth: Int, alpha: Float, beta: Float, player: Char, opponent: Char): Float {
        val validMoves = mutableListOf<Int>()
        for (col in 0 ..6) {
                val temp = makeMove(board, col, player).second
                validMoves.add(temp)
        }
        if (depth == 0 || validMoves.isEmpty() || gameIsOver(board)) {
            return utilityValue(board, player)
        }
        var alphaValue = alpha
        for (move in validMoves) {
            var boardScore = Float.NEGATIVE_INFINITY
            if (alphaValue < beta) {
                val moveRes=makeMove(board,move,player)
                val tempBoard = moveRes.first
                val nextGo=moveRes.second
                if(nextGo==-1) break
                boardScore = minimizeBeta(tempBoard, depth - 1, alphaValue, beta, player, opponent)
            }
            if (boardScore > alphaValue) {
                alphaValue = boardScore
            }
        }
        return alphaValue
    }
    private fun getValidMoves(board: Array<CharArray>): List<Int> {
        val columns = mutableListOf<Int>()
        for (col in 0 ..6) {
            if (isColumnValid(board, col)) {
                columns.add(col)
            }
        }
        return columns
    }
    private fun isColumnValid(b: Array<CharArray>, col: Int): Boolean {
        return b[0][col] == '-'
    }
    private fun makeMove(board: Array<CharArray>, col: Int, player: Char): Triple<Array<CharArray>, Int, Int> {
        val tempBoard = board.map { it.copyOf() }.toTypedArray()
        if(col<=-1) {
            return Triple(tempBoard,-1,-1)
        }
        for (row in 5 downTo 0) {
            if (tempBoard[row][col] == '-') {
                tempBoard[row][col] = player
                return Triple(tempBoard, row, col)
            }
        }
        return Triple(tempBoard,-1,-1)
    }
    private fun isValidMove(col: Int, board: Array<CharArray>): Boolean {
        for (row in 0 ..5) {
            if (board[row][col] == '-') {
                return true
            }
        }
        return false
    }
    private fun utilityValue(board: Array<CharArray>, player: Char): Float {
        val opponent = if (player == '2') '1' else '2'
        val playerFours = countSequence(board, player, 4)
        val playerThrees = countSequence(board, player, 3)
        val playerTwos = countSequence(board, player, 2)
        val playerScore = playerFours * 99999 + playerThrees * 999 + playerTwos * 99
        val opponentFours = countSequence(board, opponent, 4)
        val opponentThrees = countSequence(board, opponent, 3)
        val opponentTwos = countSequence(board, opponent, 2)
        val opponentScore = opponentFours * 99999 + opponentThrees * 999 + opponentTwos * 99
        return if (opponentFours > 0) {
            Float.NEGATIVE_INFINITY
        } else {
            (playerScore - opponentScore).toFloat()
        }
    }
    private fun countSequence(board: Array<CharArray>, player: Char, length: Int): Int {
        fun verticalSeq(row: Int, col: Int): Int {
            var count = 0
            for (rowIndex in row ..5) {
                if (board[rowIndex][col] == board[row][col]) {
                    count++
                } else {
                    break
                }
            }
            return if (count >= length) 1 else 0
        }
        fun horizontalSeq(row: Int, col: Int): Int {
            var count = 0
            for (colIndex in col ..6) {
                if (board[row][colIndex] == board[row][col]) {
                    count++
                } else {
                    break
                }
            }
            return if (count >= length) 1 else 0
        }
        fun negDiagonalSeq(row: Int, col: Int): Int {
            var count = 0
            var colIndex = col
            for (rowIndex in row downTo 0) {
                if (colIndex >= 7) break
                if (board[rowIndex][colIndex] == board[row][col]) {
                    count++
                } else {
                    break
                }
                colIndex++
            }
            return if (count >= length) 1 else 0
        }
        fun posDiagonalSeq(row: Int, col: Int): Int {
            var count = 0
            var colIndex = col
            for (rowIndex in row ..5) {
                if (colIndex >= 7) break
                if (board[rowIndex][colIndex] == board[row][col]) {
                    count++
                } else {
                    break
                }
                colIndex++
            }
            return if (count >= length) 1 else 0
        }
        var totalCount = 0
        for (row in 0 ..5) {
            for (col in 0 ..6) {
                if (board[row][col] == player) {
                    totalCount += verticalSeq(row, col)
                    totalCount += horizontalSeq(row, col)
                    totalCount += posDiagonalSeq(row, col) + negDiagonalSeq(row, col)
                }
            }
        }
        return totalCount
    }
    private fun gameIsOver(board: Array<CharArray>): Boolean {
        return countSequence(board, '2', 4) >= 1 || countSequence(board, '1', 4) >= 1
    }
    //===== complete task of bot play in the minimax algorithm =========//
}