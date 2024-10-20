package com.example.tictactoeclashofxo.gameLogic

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.databinding.ActivityConnect5TictactoeBinding
import com.example.tictactoeclashofxo.WinnerActivity
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.random.nextInt

class Connect5TicTacToe : AppCompatActivity() {
    private lateinit var binding:ActivityConnect5TictactoeBinding
    private var board = Array(9) {IntArray(9) {0} }
    private lateinit var buttonList:Array<ImageView>
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
    private var background:Int=0
    private var whoseTurn:Int=0
    private val max = Int.MAX_VALUE
    private val min = Int.MIN_VALUE
    private var turn=0
    private var rand=0
    private var putSound: MediaPlayer? =null
    private var winSound: MediaPlayer?=null
    private var loseSound: MediaPlayer?=null
    private var soundOn="0"
    private val patterns = mapOf("11111" to 30000000, "22222" to -30000000, "011110" to 20000000, "022220" to -20000000, "011112" to 50000, "211110" to 50000,
        "022221" to -50000, "122220" to -50000, "01110" to 30000, "02220" to -30000, "011010" to 15000, "010110" to 15000, "022020" to -15000, "020220" to -15000, "001112" to 2000,
        "211100" to 2000, "002221" to -2000, "122200" to -2000, "211010" to 2000, "210110" to 2000, "010112" to 2000, "011012" to 2000,
        "122020" to -2000, "120220" to -2000, "020221" to -2000, "022021" to -2000, "01100" to 500, "00110" to 500, "02200" to -500, "00220" to -500
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnect5TictactoeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //----------- here we collect all the value which we need ---------------//
        background=intent.getIntExtra("BACKGROUND",0)
        type=intent.getIntExtra("TYPE",0)
        val myPref= SessionManager(this)
        turn=intent.getIntExtra("TURN",0)
        player1Name= myPref.getDetails("PLAYER1")
        player2Name= myPref.getDetails("PLAYER2")
        buttonList= arrayOf(
            binding.b00,binding.b01,binding.b02,binding.b03,binding.b04,binding.b05,binding.b06,binding.b07,binding.b08,
            binding.b10, binding.b11,binding.b12,binding.b13,binding.b14,binding.b15,binding.b16,binding.b17,binding.b18,
            binding.b20, binding.b21,binding.b22,binding.b23,binding.b24,binding.b25,binding.b26,binding.b27,binding.b28,
            binding.b30, binding.b31,binding.b32,binding.b33,binding.b34,binding.b35,binding.b36,binding.b37,binding.b38,
            binding.b40, binding.b41,binding.b42,binding.b43,binding.b44,binding.b45,binding.b46,binding.b47,binding.b48,
            binding.b50, binding.b51,binding.b52,binding.b53,binding.b54,binding.b55,binding.b56,binding.b57,binding.b58,
            binding.b60, binding.b61,binding.b62,binding.b63,binding.b64,binding.b65,binding.b66,binding.b67,binding.b68,
            binding.b70, binding.b71,binding.b72,binding.b73,binding.b74,binding.b75,binding.b76,binding.b77,binding.b78,
            binding.b80, binding.b81,binding.b82,binding.b83,binding.b84,binding.b85,binding.b86,binding.b87,binding.b88
        )
        player1Timer=binding.player1Timer;player1Timer.stop()
        player2Timer=binding.player2Timer;player2Timer.stop()
        putSound=MediaPlayer.create(this,R.raw.put_item)
        winSound=MediaPlayer.create(this,R.raw.win_game)
        loseSound=MediaPlayer.create(this,R.raw.lose_game)
        soundOn=myPref.getDetails("SOUND").toString()
        val list= listOf(9,11,13,15,4,10,8)
        rand=list.random()
        //----------- here we show the initial dialog about the match,who's turn first and who will have circle or cross ------------//
        val dialog= Dialog(this)
        val customDialog=layoutInflater.inflate(R.layout.game_start,null)
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
            var winner="Draw"
            var loser="Draw"
            var winnerTime="0"
            var loserTime="0"
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
            intent.putExtra("Type",type)
            intent.putExtra("WinCoin",winCoin)
            intent.putExtra("LoseCoin",loseCoin)
            intent.putExtra("BACKGROUND",background)
            intent.putExtra("TYPE",type)
            intent.putExtra("TURN",whoseTurn)
            intent.putExtra("MODE",7)
            job1=lifecycleScope.launch {
                delay(3000)
                startActivity(intent)
                finish()
            }
        }
        else if(turn%2!=0){
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
                            changeTurn(2)
                            play(turn + 1)
                        }
                    }
//                    delay(15010)
//                    if (player1InTime == 0) {
//                        job1?.cancel()
//                        putValueJob!!.cancel()
//                        changeTurn(2)
//                        play(turn + 1)
//                    }
                }
            }
            else{
                cancelItemClick(0)
                //----it is for the bot and trained mode,return the row and column where to put the item ---------//
                if(type==1){
                    //------------ trained mode control here ---------------//
                    val time=Task.getRandomTime()
                    job1=lifecycleScope.launch {
                        val id=trainer()
                        delay(time)
                        buttonList[id].setImageResource(player1Item)
                        if (soundOn=="1") putSound?.start()
                        changeTurn(2)
                        play(turn + 1)
                    }
                }else{
                    //------------ bot mode control here ---------------//
                    val time=Task.getRandomTime()
                    var mustStop=false
                    var id=0
                    // Storing the Job reference for later cancellation
                    val test = CoroutineScope(Dispatchers.Default).launch {
                        val bestMove = async {
                            for (i in 0..8) {
                                for (j in 0..8) {
                                    mustStop = isItWinUser(i, j)
                                    if (mustStop) {
                                        id=i*9 + j
                                        break
                                    }
                                }
                                if (mustStop) {
                                    break
                                }
                            }
                        }
                    }
                    test.cancel()
                    job1=lifecycleScope.launch {
                        if(mustStop) {
                            delay(time)
                            if (soundOn == "1") putSound?.start()
                            buttonList[id].setImageResource(player1Item)
                            changeTurn(2)
                            play(turn + 1)
                        }
                        else {
                            val id2 = bot()
                            delay(time)
                            if (soundOn == "1") putSound?.start()
                            buttonList[id2].setImageResource(player1Item)
                            changeTurn(2)
                            play(turn + 1)
                        }
                    }
                }
            }
        }
        else{
            cancelItemClick(1)
            binding.player2Bg.setBackgroundResource(R.drawable.select_bg)
            binding.player1Bg.background=null
            //======= here control the player2 turns ========//
            if(player2First==0) {
                player2Timer.base=SystemClock.elapsedRealtime()
                player2Offset=0
                player2First=1
            }
            player1Timer.stop()
            player1Offset=SystemClock.elapsedRealtime()-player1Timer.base
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
//                delay(15010)
//                if(player2InTime==0){
//                    job1?.cancel()
//                    putValueJob!!.cancel()
//                    changeTurn(1)
//                    play(turn+1)
//                }
            }
        }
    }
    private fun winMove(list: List<Int>) {
        for(element in list){
            buttonList[element].setBackgroundResource(R.drawable.win_bg)
        }
    }
    //==== here player-1 and bot put <2> and player-2 and user put <1> =====//
    private fun put(turn: Int, callback: () -> Unit) {
        for ((index, imageView) in buttonList.withIndex()) {
            val row = index / 9
            val column = index % 9
            imageView.setOnClickListener {
                if (board[row][column]==0) {
                    if (turn == 1) {
                        imageView.setImageResource(player1Item)
                        if (soundOn=="1") putSound?.start()
                        board[row][column]=2
                        callback()
                    } else if (turn == 2 && board[row][column]==0) {
                        imageView.setImageResource(player2Item)
                        if (soundOn=="1") putSound?.start()
                        board[row][column]=1
                        callback()
                    }
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
    private fun trainer():Int{
        var id=0
        if (turn%rand==0 || (turn+1)%rand==0){
            id=findAndChooseRandomZero(board)
        }
        else id=bot()
        return id
    }
    private fun findAndChooseRandomZero(board: Array<IntArray>): Int {
        val zeroPositions = mutableListOf<Int>()
        for (row in board.indices) {
            for (col in board[row].indices) {
                if (board[row][col] == 0) {
                    zeroPositions.add(row * 9 + col)
                }
            }
        }
        return zeroPositions.random()
    }
    private fun cancelClick(){
        player1Timer.stop()
        player2Timer.stop()
        for(imageView in buttonList) imageView.isClickable=false
    }
    override fun onDestroy() {
        super.onDestroy()
        job1?.cancel()
        putValueJob?.cancel()
    }
    private fun cancelItemClick(on:Int){
        for (i in buttonList) {
            if(on==1) i.isClickable = true
            else i.isClickable=false
        }
    }
    private fun checkWinner(board: Array<IntArray>): Pair<Int, List<Int>> {
        for (row in 0..8) {
            for (col in 0..4) {
                if (board[row][col] != 0 &&
                    board[row][col] == board[row][col + 1] &&
                    board[row][col] == board[row][col + 2] &&
                    board[row][col] == board[row][col + 3] &&
                    board[row][col] == board[row][col + 4]) {

                    val positions = (col..col + 4).map { rCol -> row * 9 + rCol }
                    return Pair(if (board[row][col] == 2) 1 else 2, positions)
                }
            }
        }
        for (col in 0..8) {
            for (row in 0..4) {
                if (board[row][col] != 0 &&
                    board[row][col] == board[row + 1][col] &&
                    board[row][col] == board[row + 2][col] &&
                    board[row][col] == board[row + 3][col] &&
                    board[row][col] == board[row + 4][col]) {

                    val positions = (row..row + 4).map { rRow -> rRow * 9 + col }
                    return Pair(if (board[row][col] == 2) 1 else 2, positions)
                }
            }
        }
        for (row in 0..4) {
            for (col in 0..4) {
                if (board[row][col] != 0 &&
                    board[row][col] == board[row + 1][col + 1] &&
                    board[row][col] == board[row + 2][col + 2] &&
                    board[row][col] == board[row + 3][col + 3] &&
                    board[row][col] == board[row + 4][col + 4]) {

                    val positions = (0..4).map { i -> (row + i) * 9 + (col + i) }
                    return Pair(if (board[row][col] == 2) 1 else 2, positions)
                }
            }
        }
        for (row in 0..4) {
            for (col in 4..8) {
                if (board[row][col] != 0 &&
                    board[row][col] == board[row + 1][col - 1] &&
                    board[row][col] == board[row + 2][col - 2] &&
                    board[row][col] == board[row + 3][col - 3] &&
                    board[row][col] == board[row + 4][col - 4]) {

                    val positions = (0..4).map { i -> (row + i) * 9 + (col - i) }
                    return Pair(if (board[row][col] == 2) 1 else 2, positions)
                }
            }
        }
        if (board.all { row -> row.all { it != 0 } }) {
            return Pair(0, emptyList())
        }
        return Pair(-1, emptyList())
    }
    //======== here the implementation of miniMaxOf python code =========//
    private fun bot():Int{
        val (move, updatedBoard) = computer(board)
        val col = move[0] - 'a'
        val row = move.substring(1).toInt() - 1
        for (i in board.indices) {
            for (j in board[i].indices) {
                board[i][j] = updatedBoard[i][j]
            }
        }
        val id=row*9 + col
        board[row][col]=2
        return id
    }
    private fun getCoordinateAround(boardSize: Int, board: Array<IntArray>): Pair<List<Int>, List<Int>> {
        val potentialValues = mutableMapOf<Pair<Int, Int>, Int>()
        for (i in board.indices) {
            for (j in board[i].indices) {
                if (board[i][j] != 0) {
                    val x = j
                    val y = i

                    if (y > 0) {
                        potentialValues[(x to y - 1)] = 1
                        if (x > 0) {
                            potentialValues[(x - 1 to y - 1)] = 1
                        }
                        if (x < boardSize - 1) {
                            potentialValues[(x + 1 to y - 1)] = 1
                        }
                    }
                    if (x > 0) {
                        potentialValues[(x - 1 to y)] = 1
                        if (y < boardSize - 1) {
                            potentialValues[(x - 1 to y + 1)] = 1
                        }
                    }
                    if (y < boardSize - 1) {
                        potentialValues[(x to y + 1)] = 1
                        if (x < boardSize - 1) {
                            potentialValues[(x + 1 to y + 1)] = 1
                        }
                        if (x > 0) {
                            potentialValues[(x - 1 to y + 1)] = 1
                        }
                    }
                    if (x < boardSize - 1) {
                        potentialValues[(x + 1 to y)] = 1
                        if (y > 0) {
                            potentialValues[(x + 1 to y - 1)] = 1
                        }
                    }
                }
            }
        }
        val finalValueX = mutableListOf<Int>()
        val finalValueY = mutableListOf<Int>()
        for (key in potentialValues.keys) {
            finalValueX.add(key.first)
            finalValueY.add(key.second)
        }
        return Pair(finalValueX, finalValueY)
    }
    private fun convertArrToMove(row: Int, col: Int): String {
        val colVal = ('a' + col)
        val rowVal = (row + 1).toString()
        return "$colVal$rowVal"
    }
    private fun getRandomMove(board: Array<IntArray>): Pair<Int, Int> {
        var ctr = 0
        val idx = 9 / 2
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
                if (x in 0 until 9 && y in 0 until 9 && board[y][x] == 0) {
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
        return -1 to -1
    }
    private fun btsConvert(board: Array<IntArray>, player: Int): List<String> {
        val cList = mutableListOf<String>()
        val rList = mutableListOf<String>()
        val dList = mutableListOf<String>()
        val boardSize = 9
        for (i in -boardSize + 5 until boardSize - 4) {
            val boardDiaVal = StringBuilder()
            for (j in 0 until boardSize) {
                val x = j
                val y = i + j
                if (x in 0 until boardSize && y in 0 until boardSize) {
                    boardDiaVal.append(
                        when (board[y][x]) {
                            0 -> "0"
                            player -> "1"
                            else -> "2"
                        }
                    )
                }
            }
            if (boardDiaVal.isNotEmpty()) dList.add(boardDiaVal.toString())
        }
        for (i in -boardSize + 5 until boardSize - 4) {
            val fDiaValues = StringBuilder()
            for (j in 0 until boardSize) {
                val x = j
                val y = i + j
                if (x in 0 until boardSize && y in 0 until boardSize) {
                    fDiaValues.append(
                        when (board[boardSize - 1 - y][x]) {
                            0 -> "0"
                            player -> "1"
                            else -> "2"
                        }
                    )
                }
            }
            if (fDiaValues.isNotEmpty()) dList.add(fDiaValues.toString())
        }
        for (col in board.indices) {
            val colValues = StringBuilder()
            for (row in board.indices) {
                colValues.append(
                    when (board[row][col]) {
                        0 -> "0"
                        player -> "1"
                        else -> "2"
                    }
                )
            }
            cList.add(colValues.toString())
        }
        for (row in board.indices) {
            val rowValues = StringBuilder()
            for (col in board[row].indices) {
                rowValues.append(
                    when (board[row][col]) {
                        0 -> "0"
                        player -> "1"
                        else -> "2"
                    }
                )
            }
            rList.add(rowValues.toString())
        }
        return dList + cList + rList
    }
    private fun points(board: Array<IntArray>, player: Int): Int {
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
    private fun otherPlayerStone(player: Int): Int {
        return if (player == 1) 2 else 1
    }
    private fun minimax(board: Array<IntArray>, isMaximizer: Boolean, depth: Int, alphaVar: Int, betaVar: Int, player: Int): Int {
        val point = points(board, player)
        if (depth == 3 || point >= 20000000 || point <= -20000000) {
            return point
        }
        var alpha = alphaVar
        var beta = betaVar
        var best = if (isMaximizer) min else max
        val potentialValues = getCoordinateAround(board.size, board)

        for (i in potentialValues.first.indices) {
            val x = potentialValues.first[i]
            val y = potentialValues.second[i]
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
    private fun computer(board: Array<IntArray>): Pair<String, Array<IntArray>> {
        var mostPoints = min
        var alpha = min
        val beta = max
        val mark = 2
        var bestMoveRow = -1
        var bestMoveCol = -1
        val potentialValues = getCoordinateAround(9, board)
        for (i in potentialValues.first.indices) {
            val x = potentialValues.first[i]
            val y = potentialValues.second[i]
            if (board[y][x] == 0) {
                val newBoard = Array(board.size) { board[it].copyOf() }
                newBoard[y][x] = mark
                val movePoints = max(mostPoints, minimax(newBoard, false, 2, alpha, beta, mark))
                alpha = max(alpha, movePoints)
                if (movePoints > mostPoints) {
                    bestMoveRow = y
                    bestMoveCol = x
                    mostPoints = movePoints
                    if (movePoints >= 20000000) break
                }
            }
        }

        if (bestMoveRow == -1) {
            val (x, y) = getRandomMove(board)
            return convertArrToMove(y, x) to board.apply { this[y][x] = mark }
        }
        val updatedBoard = Array(board.size) { board[it].copyOf() }
        updatedBoard[bestMoveRow][bestMoveCol] = mark
        return convertArrToMove(bestMoveRow, bestMoveCol) to updatedBoard
    }
    //================================================================/
    private fun  isItWinUser(row: Int, col: Int): Boolean {
        val grid=board
        var count = 1
        var i = col - 1
        while (i >= 0 && grid[row][i] == 1) {
            count++
            if (count == 5) return true
            i--
        }
        i = col + 1
        while (i < 9 && grid[row][i] == 1) {
            count++
            if (count == 5) return true
            i++
        }
        count = 1
        i = row - 1
        while (i >= 0 && grid[i][col] == 1) {
            count++
            if (count == 5) return true
            i--
        }
        i = row + 1
        while (i < 9 && grid[i][col] == 1) {
            count++
            if (count == 5) return true
            i++
        }
        count = 1
        i = 1
        while (row - i >= 0 && col - i >= 0 && grid[row - i][col - i] == 1) {
            count++
            if (count == 5) return true
            i++
        }
        i = 1
        while (row + i < 9 && col + i < 9 && grid[row + i][col + i] == 1) {
            count++
            if (count == 5) return true
            i++
        }
        count = 1
        i = 1
        while (row - i >= 0 && col + i < 9 && grid[row - i][col + i] == 1) {
            count++
            if (count == 5) return true
            i++
        }
        i = 1
        while (row + i < 9 && col - i >= 0 && grid[row + i][col - i] == 1) {
            count++
            if (count == 5) return true
            i++
        }
        return false
    }

}