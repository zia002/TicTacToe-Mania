package com.example.tictactoeclashofxo.gameLogic

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.WinnerActivity
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.Task
import com.example.tictactoeclashofxo.databinding.ActivityInvisibleTicTacToeBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import kotlin.random.Random
import kotlin.random.nextInt

data class PlayerData(var index:Int, var row:Int, var col:Int)
class InfiniteTicTacToe : AppCompatActivity() {
    lateinit var binding:ActivityInvisibleTicTacToeBinding
    private var board = Array(3) {CharArray(3) {'-'} }
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
    private var player1Tmp= listOf('1','1','1')
    private var player2Tmp= listOf('2','2','2')
    private lateinit var player1Data:Queue<PlayerData>
    private lateinit var player2Data:Queue<PlayerData>
    private var whoseTurn:Int=0
    private var background:Int=0
    private var rand=1
    private var putSound: MediaPlayer? =null
    private var winSound: MediaPlayer?=null
    private var loseSound: MediaPlayer?=null
    private var soundOn="0"
    private var turn=0
    private var count=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityInvisibleTicTacToeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //----------- here we collect all the value which we need ---------------//
        background=intent.getIntExtra("BACKGROUND",0)
        type=intent.getIntExtra("TYPE",0)
        turn=intent.getIntExtra("TURN",0)
        val myPref=SessionManager(this)
        player1Data= LinkedList()
        player2Data= LinkedList()
        player1Name= myPref.getDetails("PLAYER1")
        player2Name= myPref.getDetails("PLAYER2")

        buttonList= arrayOf(binding.b00,binding.b01,binding.b02,
            binding.b10, binding.b11,binding.b12,
            binding.b20, binding.b21,binding.b22
        )
        player1Timer=binding.player1Timer;player1Timer.stop()
        player2Timer=binding.player2Timer;player2Timer.stop()
        putSound=MediaPlayer.create(this,R.raw.put_item)
        winSound=MediaPlayer.create(this,R.raw.win_game)
        loseSound=MediaPlayer.create(this,R.raw.lose_game)
        soundOn=myPref.getDetails("SOUND").toString()
        //----------- here we show the initial dialog about the match,who's turn first and who will have circle or cross ------------//
        val dialog=Dialog(this)
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
        val randomTurn=Random.nextInt(0,10) //---> if even player1 turn first
        val randomItem=Random.nextInt(0,10) //---> if even player1 have cross
        val list= listOf(35,5,9,11,19,7,32,40)
        rand=list.random()
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
    private var result=-1
    private var job1: Job? = null
    private var putValueJob: Job?=null
    private fun play(turn:Int){
        job1?.cancel()
        putValueJob?.cancel()
        result=checkWinner()
        if(result==1 || result==2 || result==3){
            cancelClick()
            val time1=(SystemClock.elapsedRealtime()-player1Timer.base).toString()
            val time2=(SystemClock.elapsedRealtime()-player2Timer.base).toString()
            val winner: String
            val loser: String
            val winnerTime: String
            val loserTime: String
            if(result==1){
                winner=player1Name.toString()
                loser=player2Name.toString()
                winnerTime=time1
                loserTime=time2
                val res=checkWinnerTurn(board)
                for(i in res.second){
                    buttonList[i].setBackgroundResource(R.drawable.win_bg)
                }
                if (soundOn=="1") loseSound?.start()
            }
            else if(result==2){
                winner=player2Name.toString()
                loser=player1Name.toString()
                winnerTime=time2
                loserTime=time1
                val res=checkWinnerTurn(board)
                for(i in res.second){
                    buttonList[i].setBackgroundResource(R.drawable.win_bg)
                }
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
                if(result==0) { //----draw----//
                    winCoin = Random.nextInt(5..10)
                    loseCoin=Random.nextInt(2..5)
                }else{   //----win----//
                    winCoin=Random.nextInt(100..200)
                    loseCoin=Random.nextInt(2..5)
                }
            }
            else if(type==3){//-----Bot------//
                if(result==0) {//----draw----//
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
            intent.putExtra("MODE",3)
            job1=lifecycleScope.launch {
                delay(3000)
                startActivity(intent)
                finish()
            }
        }
        else if(turn%2!=0){
            changeTurn(3) //=== it will faded the bg of the last removal item if exist =====//
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
                cancelTurn(1)
                //----------------- it is only for the local play mode ---------------//
                var player1InTime = 0
                job1 = lifecycleScope.launch {
                    putValueJob = launch {
                        put(1) {
                            player1InTime = 1
                            changeTurn(2)
                            removeTurn(1)
                            play(turn + 1)
                        }
                    }
                    delay(5010)
                    if (player1InTime == 0) {
                        job1?.cancel()
                        putValueJob!!.cancel()
                        changeTurn(2)
                        removeTurn(1)
                        play(turn + 1)
                    }
                }
            }
            else{
                cancelTurn(0)
                //----it is for the bot and trained mode,return the row and column where to put the item ---------//
                if(type==1){
//                    //------------ trained mode control here ---------------//
                    val delayTime=Task.getRandomTime()
                    if(turn%rand==0 || (turn+1)%rand==0){
                        val list = mutableListOf<Pair<Int, Int>>()
                        for(i in 0..2){
                            for(j in 0..2){
                                if(board[i][j]=='-'){
                                    list.add(Pair(i,j))
                                }
                            }
                        }
                        val id=list.random()
                        board[id.first][id.second]='1'
                        job1=lifecycleScope.launch {
                            delay(delayTime)
                            putItem(id.first, id.second)
                            changeTurn(2)
                            removeTurn(1)
                            play(turn + 1)
                        }
                    }else{
                        job1 = lifecycleScope.launch {
                            val res2=bot(board)
                            delay(delayTime)
                            putItem(res2.first, res2.second)
                            changeTurn(2)
                            removeTurn(1)
                            play(turn + 1)
                        }
                    }
                }else{
                    //------------ bot mode control here ---------------//
                    val delayTime=Task.getRandomTime()
                    val b=board
                    var res=findWinningMove(b,'1')
                    if(res.first==1) {
                        job1=lifecycleScope.launch {
                            delay(delayTime)
                            val row=res.second?.first
                            val col=res.second?.second
                            if(row==null || col==null){
                                val res2=emptyMove()
                                putItem(res2.first,res2.second)
                            }
                            else putItem(row,col)
                            Log.d("runTime",count.toString())
                            count=0
                            changeTurn(2)
                            removeTurn(1)
                            play(turn + 1)
                        }
                    }
                    res=findWinningMove(b,'2')

                    if(res.first==1) {
                        job1=lifecycleScope.launch {
                            delay(delayTime)
                            val row=res.second?.first
                            val col=res.second?.second
                            putItem(row!!,col!!)
                            Log.d("runTime",count.toString())
                            count=0
                            changeTurn(2)
                            removeTurn(1)
                            play(turn + 1)
                        }
                    }
                    else {
                        job1 = lifecycleScope.launch {
                            val res2=bot(board)
                            delay(delayTime)
                            putItem(res2.first, res2.second)
                            Log.d("runTime",count.toString())
                            count=0
                            changeTurn(2)
                            removeTurn(1)
                            play(turn + 1)
                        }
                   }
                }
            }
        }
        else{
            changeTurn(4)
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
                        removeTurn(2)
                        play(turn +1)
                    }
                }
                delay(5010)
                if(player2InTime==0){
                    job1?.cancel()
                    putValueJob!!.cancel()
                    changeTurn(1)
                    removeTurn(2)
                    play(turn+1)
                }
            }
        }
    }
    private fun put(turn: Int, callback: () -> Unit) {
        for ((index, imageView) in buttonList.withIndex()) {
            val row = index / 3
            val column = index % 3
            imageView.setOnClickListener {
                if (board[row][column]=='-') {
                    if (turn == 1) {
                        imageView.setImageResource(player1Item)
                        if (soundOn=="1") putSound?.start()
                        board[row][column]='1'
                        player1Data.add(PlayerData(index,row,column))
                        callback()
                    } else if (turn == 2 && board[row][column]=='-') {
                        imageView.setImageResource(player2Item)
                        if (soundOn=="1") putSound?.start()
                        board[row][column]='2'
                        player2Data.add(PlayerData(index,row,column))
                        cancelTurn(0)
                        callback()
                    }
                }
            }
        }
    }
    private fun cancelTurn(ok:Int){
        if(ok==1){
            for(i in buttonList){
                i.isClickable=true
            }
        }
        else {
            for(i in buttonList){
                i.isClickable=false
            }
        }
    }
    private fun changeTurn(turn: Int){
        //=========== it is for the timer control after putting item and it will remove the last item ============//
        if(turn==1){
            player1Timer.start()
            player2Timer.stop()
        }
        else if(turn==2){
            player2Timer.start()
            player1Timer.stop()
        }
        //=========== this is for vanishing the last putted item from the board ==============//
        else if(turn==3) {  //====player-1====//
            if(!player1Data.isEmpty() && player1Data.size>=3){
                val frontData=player1Data.peek()
                buttonList[frontData!!.index].setBackgroundResource(R.drawable.faded_bg)
            }
        }
        else if(turn==4){   //====player-2====//
            if(!player2Data.isEmpty() && player1Data.size>=3){
                val frontData=player2Data.peek()
                if (frontData != null) {
                    buttonList[frontData.index].setBackgroundResource(R.drawable.faded_bg)
                }
            }
        }
    }
    private fun removeTurn(turn:Int){
        if(turn==1){ //=== if 1 then it will remove the player 1 item ===//
            if(!player1Data.isEmpty() && player1Data.size>=4){
                val frontData=player1Data.peek()
                buttonList[frontData!!.index].background=null
                buttonList[frontData.index].setImageDrawable(null)
                board[frontData.row][frontData.col]='-'
                player1Data.poll()
            }
        }
        else{
            if(!player2Data.isEmpty() && player2Data.size>=4){
                val frontData=player2Data.peek()
                buttonList[frontData!!.index].background=null
                buttonList[frontData.index].setImageDrawable(null)
                board[frontData.row][frontData.col]='-'
                player2Data.poll()
            }
        }
    }
    private fun putItem(row:Int,col:Int){
        val index=(row*2)+row+col
        player1Data.add(PlayerData(index,row,col))
        board[row][col]='1'
        buttonList[index].setImageResource(player1Item)
        if (soundOn=="1") putSound?.start()
    }
    private fun cancelClick(){
        binding.player1Bg.background=null
        binding.player2Bg.background=null
        player1Timer.stop()
        player2Timer.stop()
        for(imageView in buttonList) imageView.isClickable=false
    }
    private fun isEmpty(b: Array<CharArray>): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (b[i][j] == '-') return true
            }
        }
        return false
    }
    private fun win(b: Array<CharArray>): Int {
        for (i in 0 until 3) {
            if (b[i][0] == b[i][1] && b[i][1] == b[i][2] && b[i][2] == '1') return 1
            else if (b[i][0] == b[i][1] && b[i][1] == b[i][2] && b[i][2] == '2') return -1
            else if (b[0][i] == b[1][i] && b[1][i] == b[2][i] && b[2][i] == '1') return 1
            else if (b[0][i] == b[1][i] && b[1][i] == b[2][i] && b[2][i] == '2') return -1
        }
        if (b[0][0] == b[1][1] && b[1][1] == b[2][2] && b[2][2] == '1') return 1
        else if (b[0][0] == b[1][1] && b[1][1] == b[2][2] && b[2][2] == '2') return -1
        else if (b[0][2] == b[1][1] && b[1][1] == b[2][0] && b[0][2] == '1') return 1
        else if (b[0][2] == b[1][1] && b[1][1] == b[2][0] && b[0][2] == '2') return -1
        return 2
    }
    private fun minimax(b: Array<CharArray>, ai: Boolean, alpha: Int, beta: Int): Int {
        count++
        val score = win(b)
        if (score == 1) return score
        else if (score == -1) return score
        else if (!isEmpty(b)) return 0
        var alphaVar = alpha
        var betaVar = beta
        if (ai) {
            var best = Int.MIN_VALUE
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    if (b[i][j] == '-') {
                        b[i][j] = '1'
                        best = maxOf(best, minimax(b, false, alphaVar, betaVar))
                        b[i][j] = '-'
                        alphaVar = maxOf(alphaVar, best)
                        if (betaVar <= alphaVar) break
                    }
                }
                if (betaVar <= alphaVar) break
            }
        return best
    } else {
        var best = Int.MAX_VALUE
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (b[i][j] == '-') {
                        b[i][j] = '2'
                        best = minOf(best, minimax(b, true, alphaVar, betaVar))
                        b[i][j] = '-'
                        betaVar = minOf(betaVar, best)
                        if (betaVar <= alphaVar) break
                    }
                }
                if (betaVar <= alphaVar) break
            }
            return best
        }
    }
    private fun bot(b: Array<CharArray>):Pair<Int,Int> {
        var score: Int
        var best = Int.MIN_VALUE
        var row = 0
        var col = 0
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (b[i][j] == '-') {
                    b[i][j] = '1'
                    score = minimax(b,false,Int.MIN_VALUE,Int.MAX_VALUE)
                    b[i][j] = '-'
                    if (score > best) {
                        row = i
                        col = j
                        best = score
                    }
                }
            }
        }
        board[row][col] = '1'
        return Pair(row, col)
    }
    private fun checkWinner():Int{
        var nothingLeft=3
        for(i in 0..2){
            val ansData=ArrayList<Char>()
            for(j in 0..2){
                ansData.add(board[i][j])
                if(board[i][j]=='-') nothingLeft=0
            }
            if(ansData==player1Tmp) return 1
            else if(ansData==player2Tmp) return 2
        }
        for(i in 0..2){
            val ansData=ArrayList<Char>()
            for(j in 0..2){
                if(board[i][j]=='-') nothingLeft=0
                ansData.add(board[j][i])
            }
            if(ansData==player1Tmp) return 1
            else if(ansData==player2Tmp) return 2
        }
        if(board[0][0]==board[1][1] && board[1][1]==board[2][2] && board[2][2]=='1') return 1
        else if(board[0][0]==board[1][1] && board[1][1]==board[2][2] && board[2][2]=='2') return 2
        else if(board[0][2]==board[1][1] && board[2][0]==board[1][1] && board[1][1]=='1') return 1
        else if(board[0][2]==board[1][1] && board[2][0]==board[1][1] && board[1][1]=='2') return 2
        else return nothingLeft
    }
    override fun onDestroy() {
        super.onDestroy()
        job1?.cancel()
        putValueJob?.cancel()
    }
    private fun checkWinnerTurn(board: Array<CharArray>): Pair<Int, List<Int>> {
        val winningCombinations = listOf(
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)), // Top row
            listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)), // Middle row
            listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2)), // Bottom row
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)), // Left column
            listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)), // Middle column
            listOf(Pair(0, 2), Pair(1, 2), Pair(2, 2)), // Right column
            listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)), // Top-left to bottom-right diagonal
            listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))  // Top-right to bottom-left diagonal
        )
        for (combination in winningCombinations) {
            val (aRow, aCol) = combination[0]
            val (bRow, bCol) = combination[1]
            val (cRow, cCol) = combination[2]

            val a = board[aRow][aCol]
            val b = board[bRow][bCol]
            val c = board[cRow][cCol]

            if (a == '1' && b == '1' && c == '1') {
                return Pair(1, combination.map { it.first * 3 + it.second })
            }
            if (a == '2' && b == '2' && c == '2') {
                return Pair(2, combination.map { it.first * 3 + it.second })
            }
        }
        if (board.all { row -> row.all { it != '-' } }) {
            return Pair(0, emptyList())
        }
        return Pair(-1, emptyList())
    }
    //=== this is for safety ===//
    private fun findWinningMove(board: Array<CharArray>, ch: Char): Pair<Int, Pair<Int, Int>?> {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == '-') {  // Check if the cell is empty
                    board[i][j] = ch  // Temporarily place the character
                    if (checkIfWinningMove(board, ch)) {  // Check if this move results in a win
                        board[i][j] = '-'  // Revert the cell back
                        return Pair(1, Pair(i, j))  // Return 1 and the position of the winning move
                    }
                    board[i][j] = '-'  // Revert the cell back if not a winning move
                }
            }
        }
        return Pair(0, null)  // Return 0 if no winning move is found
    }
    private fun checkIfWinningMove(board: Array<CharArray>, ch: Char): Boolean {
        val winningCombinations = listOf(
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),  // Top row
            listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)),  // Middle row
            listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2)),  // Bottom row
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),  // Left column
            listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)),  // Middle column
            listOf(Pair(0, 2), Pair(1, 2), Pair(2, 2)),  // Right column
            listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)),  // Top-left to bottom-right diagonal
            listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))   // Top-right to bottom-left diagonal
        )

        for (combination in winningCombinations) {
            val (aRow, aCol) = combination[0]
            val (bRow, bCol) = combination[1]
            val (cRow, cCol) = combination[2]

            if (board[aRow][aCol] == ch &&
                board[bRow][bCol] == ch &&
                board[cRow][cCol] == ch) {
                return true  // Winning combination found
            }
        }
        return false  // No winning combination found
    }
    private fun emptyMove():Pair<Int,Int>{
        for(i in 0..2){
            for(j in 0..2){
                if(board[i][j]=='-') return Pair(i,j)
            }
        }
        return Pair(0,0)
    }
}