package com.example.tictactoeclashofxo.gameLogic

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.WinnerActivity
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.Task
import com.example.tictactoeclashofxo.databinding.ActivityEatTicTacToeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

class EatTicTacToe : AppCompatActivity() {
    private lateinit var binding: ActivityEatTicTacToeBinding
    private var board = Array(9) { 0 }
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
    private var pl1Mx=3;private var pl1Mid=2;private var pl1Min=1
    private var pl2Mx=3;private var pl2Mid=2;private var pl2Min=1
    private var type:Int=0
    private var firstMove=1
    private var userMove=1
    private var background=0
    private var whoseTurn=0
    private var rand=5
    private var putSound: MediaPlayer? =null
    private var winSound: MediaPlayer?=null
    private var loseSound: MediaPlayer?=null
    private var soundOn="0"
    private var turn=0
    private var count=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEatTicTacToeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //----------- here we collect all the value which we need ---------------//
        background=intent.getIntExtra("BACKGROUND",0)
        type=intent.getIntExtra("TYPE",0)
        turn=intent.getIntExtra("TURN",0)
        val myPref= SessionManager(this)
        player1Name= myPref.getDetails("PLAYER1")
        player2Name= myPref.getDetails("PLAYER2")
        buttonList= arrayOf(binding.b00,binding.b01,binding.b02,
            binding.b10, binding.b11,binding.b12,
            binding.b20, binding.b21,binding.b22
        )
        player1Timer=binding.player1Timer;player1Timer.stop()
        player2Timer=binding.player2Timer;player2Timer.stop()

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
        putSound=MediaPlayer.create(this,R.raw.put_item)
        winSound=MediaPlayer.create(this,R.raw.win_game)
        loseSound=MediaPlayer.create(this,R.raw.lose_game)
        soundOn=myPref.getDetails("SOUND").toString()
        //------------ random number task here -----------//
        val randomTurn= Random.nextInt(0,10) //---> if even player1 turn first
        val randomItem= Random.nextInt(0,10) //---> if even player1 have cross
        rand=Random.nextInt(3..5)
        //------------ select value will be 1 for bot play and 0 for trainer play ---------//
        binding.main.setBackgroundResource(background)
        player2Image=myPref.getDP()
        player2img.setImageResource(player2Image)

        binding.player1MinNo.text='x'.toString().plus(pl1Min) ; binding.player1MidNo.text='x'.toString().plus(pl1Mid) ; binding.player1MxNo.text='x'.toString().plus(pl1Mx)
        binding.player2MinNo.text='x'.toString().plus(pl2Min) ; binding.player2MidNo.text='x'.toString().plus(pl2Mid) ; binding.player2MxNo.text='x'.toString().plus(pl2Mx)
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
                binding.player1MxImg.setImageResource(player1Item)
                binding.player1MidImg.setImageResource(player1Item)
                binding.player1MinImg.setImageResource(player1Item)
                binding.player2MxImg.setImageResource(player2Item)
                binding.player2MidImg.setImageResource(player2Item)
                binding.player2MinImg.setImageResource(player2Item)
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
                binding.player1MxImg.setImageResource(player1Item)
                binding.player1MidImg.setImageResource(player1Item)
                binding.player1MinImg.setImageResource(player1Item)
                binding.player2MxImg.setImageResource(player2Item)
                binding.player2MidImg.setImageResource(player2Item)
                binding.player2MinImg.setImageResource(player2Item)
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
                binding.player1MxImg.setImageResource(player1Item)
                binding.player1MidImg.setImageResource(player1Item)
                binding.player1MinImg.setImageResource(player1Item)
                binding.player2MxImg.setImageResource(player2Item)
                binding.player2MidImg.setImageResource(player2Item)
                binding.player2MinImg.setImageResource(player2Item)
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
        result=checkWinner(board)
        val pl1Over=gameOver(1)
        val pl2Over=gameOver(2)
        if( result==1 || result==-1 || pl1Over==1 || pl2Over==1){
            cancelClick()
            val time1=(SystemClock.elapsedRealtime()-player1Timer.base).toString()
            val time2=(SystemClock.elapsedRealtime()-player2Timer.base).toString()
            val winner: String
            val loser: String
            val winnerTime: String
            val loserTime: String
            val res=checkThreeInARow(board)
            if(result==1){
                winner=player1Name.toString()
                loser=player2Name.toString()
                winnerTime=time1
                loserTime=time2
                for(i in res.second){
                    buttonList[i].setBackgroundResource(R.drawable.win_bg)
                }
                if (soundOn=="1") loseSound?.start()
            }
            else if(result==-1){
                winner=player2Name.toString()
                loser=player1Name.toString()
                winnerTime=time2
                loserTime=time1
                for(i in res.second){
                    buttonList[i].setBackgroundResource(R.drawable.win_bg)
                }
                if (soundOn=="1") winSound?.start()
            }
            else{
                if(pl1Over==1 || pl2Over==1){
                    val toast = Toast.makeText(applicationContext,"No Move Available", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
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
                if(result==3) { //----draw----//
                    winCoin = Random.nextInt(5..10)
                    loseCoin=Random.nextInt(2..5)
                }else{   //----win----//
                    winCoin=Random.nextInt(100..200)
                    loseCoin=Random.nextInt(2..5)
                }
            }
            else if(type==3){//-----Bot------//
                if(result==3) {//----draw----//
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
            intent.putExtra("MODE",2)
            job1=lifecycleScope.launch {
                delay(3000)
                startActivity(intent)
                finish()
            }
        }
        else if(turn%2!=0){
            cancelClickPlayer(2)
            updateUI(1)
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
//                    delay(20010)
//                    if (player1InTime == 0) {
//                        job1?.cancel()
//                        putValueJob!!.cancel()
//                        changeTurn(2)
//                        play(turn + 1)
//                    }
                }
            }
            else{
                //----it is for the bot and trained mode,return the row and column where to put the item ---------//
                if(type==1){
                    //------------ trained mode control here ---------------//
                    if((turn%rand==0 || (turn+1)%rand==0)){
                        var mx=0
                        if(pl1Mx>0) {
                            mx=3
                            binding.player1MxMain.setBackgroundResource(R.drawable.select_bg)
                        }
                        else if(pl1Mid>0) {
                            mx=2
                            binding.player1MidMain.setBackgroundResource(R.drawable.select_bg)
                        }
                        else if(pl1Min>0) {
                            mx=1
                            binding.player1MinMain.setBackgroundResource(R.drawable.select_bg)
                        }
                        /* here we will choose an random position which can be put able by the max
                           after getting the position we will randomly choose the item which can be able to put
                        */
                        job1=lifecycleScope.launch {
                            val list = mutableListOf<Int>()
                            for (i in 0..8) {
                                if (abs(board[i]) < mx) list.add(i)
                            }
                            val id = list.random()
                            val randomTime = listOf(2000, 2500, 1600)
                            val timeDelay = randomTime.random().toLong()
                            botItemBg(mx)
                            board[id]=mx
                            delay(timeDelay)
                            putItem(mx, 1, id)
                            changeTurn(2)
                            play(turn + 1)
                        }
                    }
                    else{
                        val b=board
                        job1=lifecycleScope.launch{
                            val res: Pair<Int, Int>
                            if(firstMove==1){
                                val randIdx= listOf(0,2,6,8)
                                if(userMove==1){// bot first
                                    val choose=randIdx.random()
                                    res=Pair(3,choose)
                                    userMove=0
                                }
                                else{
                                    if(board[4]==0 || abs(board[4])<3) res=Pair(3,4)
                                    else res=Pair(3,randIdx.random())
                                }
                                firstMove=0
                            }
                            else {

                                res = bot(b, pl1Mx, pl1Mid, pl1Min, pl2Mx, pl2Mid, pl2Min)
                            }
                            val randomTime= listOf(2000,2500,1600)
                            val timeDelay = randomTime.random().toLong()
                            botItemBg(res.first)
                            delay(timeDelay)
                            putBotItem(res.first,res.second)
                            changeTurn(2)
                            play(turn + 1)
                            // testing git
                        }
                    }
                }
                else{
                    player1Timer.start()
                    //------------ bot mode control here ---------------//
                    val b=board
                    job1=lifecycleScope.launch{
                        var res=Pair(0,0)
                        if(firstMove==1){
                            val randIdx= listOf(0,2,6,8)
                            if(userMove==1){// bot first
                                val choose=randIdx.random()
                                res=Pair(3,choose)
                                userMove=0
                            }
                            else{
                                if(board[4]==0 || abs(board[4])<3) res=Pair(3,4)
                                else res=Pair(3,randIdx.random())
                            }
                            firstMove=0
                        }
                        else {
                            CoroutineScope(Dispatchers.Default).launch {
                                res = bot(b, pl1Mx, pl1Mid, pl1Min, pl2Mx, pl2Mid, pl2Min)
                            }
                        }
                        val randomTime= listOf(2000,2500,1600)
                        val timeDelay = randomTime.random().toLong()
                        botItemBg(res.first)
                        delay(timeDelay)
                        putBotItem(res.first,res.second)
                        count=0
                        changeTurn(2)
                        play(turn + 1)
                    }
                }
            }
        }
        else{
            cancelClickPlayer(1)
            updateUI(2)
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
                        userMove=0
                        changeTurn(1)
                        play(turn +1)
                    }
                }
//                delay(20010)
//                if(player2InTime==0){
//                    job1?.cancel()
//                    putValueJob!!.cancel()
//                    changeTurn(1)
//                    play(turn+1)
//                }
            }
        }
    }
    private fun putBotItem(item: Int, index: Int) {
        when(item){
            1->{
                binding.player1MinMain.background=null
                pl1Min-=1
                if (pl1Min==0) binding.player1MinMain.visibility=View.GONE
                else binding.player1MinNo.text='x'.toString().plus(pl1Min)
                putItem(1,1,index)
                board[index]=1
            }
            2->{
                binding.player1MidMain.background=null
                pl1Mid-=1
                if (pl1Mid==0) binding.player1MidMain.visibility=View.GONE
                else binding.player1MidNo.text='x'.toString().plus(pl1Mid)
                putItem(2,1,index)
                board[index]=2
            }
            3->{
                binding.player1MxMain.background=null
                pl1Mx-=1
                if (pl1Mx==0) binding.player1MxMain.visibility=View.GONE
                else binding.player1MxNo.text='x'.toString().plus(pl1Mx)
                putItem(3,1,index)
                board[index]=3
            }
        }
    }
    private fun botItemBg(item:Int){
        when(item){
            1-> binding.player1MinMain.setBackgroundResource(R.drawable.select_bg)
            2-> binding.player1MidMain.setBackgroundResource(R.drawable.select_bg)
            3-> binding.player1MxMain.setBackgroundResource(R.drawable.select_bg)
        }
    }
    private fun put(turn: Int,callback: () -> Unit) {
        if (turn == 1) {
            /*
             *player-1 item will be selected here and and it will be pass to the putItem function
             *value:According to the item,turn:Player-1 or player-2 , index
            */
            binding.player1MxImg.setOnClickListener {
                cancelBg(1)
                binding.player1MxMain.setBackgroundResource(R.drawable.select_bg)
                for ((index, imageView) in buttonList.withIndex()) {
                    val row = index / 3
                    val column = index % 3
                    val id = row * 3 + column
                    imageView.setOnClickListener {
                        if (abs(board[index]) < 3) {
                            board[index] = 3
                            putItem(3, 1,id)
                            pl1Mx-=1
                            binding.player1MxMain.background=null
                            if(pl1Mx==0) binding.player1MxMain.visibility=View.GONE
                            else binding.player1MxNo.text='x'.toString().plus(pl1Mx)
                            cancelClickPlayer(1)
                            callback()
                        }
                    }
                }
            }
            binding.player1MidImg.setOnClickListener {
                cancelBg(1)
                binding.player1MidMain.setBackgroundResource(R.drawable.select_bg)
                for ((index, imageView) in buttonList.withIndex()) {
                    val row = index / 3
                    val column = index % 3
                    val id = row * 3 + column
                    imageView.setOnClickListener {
                        if (abs(board[index]) < 2) {
                            board[index] = 2
                            putItem(2, 1,id)
                            pl1Mid-=1
                            binding.player1MidMain.background=null
                            if(pl1Mid==0) binding.player1MidMain.visibility=View.GONE
                            else binding.player1MidNo.text='x'.toString().plus(pl1Mid)
                            cancelClickPlayer(1)
                            callback()
                        }
                    }
                }
            }
            binding.player1MinImg.setOnClickListener {
                cancelBg(1)
                binding.player1MinMain.setBackgroundResource(R.drawable.select_bg)
                for ((index, imageView) in buttonList.withIndex()) {
                    val row = index / 3
                    val column = index % 3
                    val id = row * 3 + column
                    imageView.setOnClickListener {
                        if (abs(board[index]) < 1) {
                            board[index] = 1
                            putItem(1, 1,id)
                            pl1Min-=1
                            binding.player1MinMain.background=null
                            if(pl1Min==0) binding.player1MinMain.visibility=View.GONE
                            else binding.player1MinNo.text='x'.toString().plus(pl1Min)
                            cancelClickPlayer(1)
                            callback()
                        }
                    }
                }
            }
        }else{
            binding.player2MxImg.setOnClickListener {
                cancelBg(2)
                binding.player2MxMain.setBackgroundResource(R.drawable.select_bg)
                for ((index, imageView) in buttonList.withIndex()) {
                    val row = index / 3
                    val column = index % 3
                    val id = row*3 + column
                    imageView.setOnClickListener {
                        if (abs(board[index]) < 3) {
                            board[index] = -3
                            putItem(3, 2,id)
                            pl2Mx-=1
                            binding.player2MxMain.background=null
                            if(pl2Mx==0) binding.player2MxMain.visibility=View.GONE
                            else binding.player2MxNo.text='x'.toString().plus(pl2Mx)
                            cancelClickPlayer(2)
                            callback()
                        }
                    }
                }
            }
            binding.player2MidImg.setOnClickListener {
                cancelBg(2)
                binding.player2MidMain.setBackgroundResource(R.drawable.select_bg)
                for ((index, imageView) in buttonList.withIndex()) {
                    val row = index / 3
                    val column = index % 3
                    val id = row*3 + column
                    imageView.setOnClickListener {
                        if (abs(board[index]) < 2) {
                            board[index] = -2
                            putItem(2, 2,id)
                            pl2Mid-=1
                            binding.player2MidMain.background=null
                            if(pl2Mid==0) binding.player2MidMain.visibility=View.GONE
                            else binding.player2MidNo.text='x'.toString().plus(pl2Mid)
                            cancelClickPlayer(2)
                            callback()
                        }
                    }
                }
            }
            binding.player2MinImg.setOnClickListener {
                cancelBg(2)
                binding.player2MinMain.setBackgroundResource(R.drawable.select_bg)
                for ((index, imageView) in buttonList.withIndex()) {
                    val row = index / 3
                    val column = index % 3
                    val id = row * 3 + column
                    imageView.setOnClickListener {
                        if (abs(board[index]) < 1) {
                            board[index] = -1
                            putItem(1, 2,id)
                            pl2Min-=1
                            binding.player2MinMain.background=null
                            if(pl2Min==0) binding.player2MinMain.visibility=View.GONE
                            else binding.player2MinNo.text='x'.toString().plus(pl2Min)
                            cancelClickPlayer(2)
                            callback()
                        }
                    }
                }
            }
        }
    }
    private fun putItem(value:Int,turn: Int,index:Int){
        val scale = this.resources.displayMetrics.density
        val px1 = (2 * scale + 0.5f).toInt()
        val px2 = (10 * scale + 0.5f).toInt()
        val px3 = (20 * scale + 0.5f).toInt()
        when(value){
            1->{
                if(turn==1) buttonList[index].setImageResource(player1Item)
                else buttonList[index].setImageResource(player2Item)
                buttonList[index].setPadding(px3,px3,px3,px3)
            }

            2->{
                if(turn==1) buttonList[index].setImageResource(player1Item)
                else buttonList[index].setImageResource(player2Item)
                buttonList[index].setPadding(px2,px2,px2,px2)
            }
            3->{
                if(turn==1) buttonList[index].setImageResource(player1Item)
                else buttonList[index].setImageResource(player2Item)
                buttonList[index].setPadding(px1,px1,px1,px1)
            }
        }
    }
    private fun cancelClick(){
        binding.player1Bg.background=null
        binding.player2Bg.background=null
        player1Timer.stop()
        player2Timer.stop()
        for(imageView in buttonList) imageView.isClickable=false
    }
    private fun cancelClickPlayer(player: Int){
        if (player==1){
            binding.player1MxImg.isClickable=false
            binding.player1MidImg.isClickable=false
            binding.player1MinImg.isClickable=false

            binding.player2MxImg.isClickable=true
            binding.player2MidImg.isClickable=true
            binding.player2MinImg.isClickable=true
        }
        else{
            binding.player1MxImg.isClickable=true
            binding.player1MidImg.isClickable=true
            binding.player1MinImg.isClickable=true

            binding.player2MxImg.isClickable=false
            binding.player2MidImg.isClickable=false
            binding.player2MinImg.isClickable=false
        }
    }
    //== in this function we will cancel the item of player bg ==//
    private fun cancelBg(turn:Int){
        if(turn==1){ binding.player1MxMain.background=null;binding.player1MidMain.background=null;binding.player1MinMain.background=null}
        else {binding.player2MxMain.background=null;binding.player2MidMain.background=null;binding.player2MinMain.background=null}
    }
    //== this will update the Ui,count the mini,mid,max===//
    private fun updateUI(turn: Int){
        if(turn==1){
            binding.player1Bg.setBackgroundResource(R.drawable.select_bg)
            binding.player1MxImg.isClickable=true
            binding.player1MidImg.isClickable=true
            binding.player1MinImg.isClickable=true
            binding.player2MxImg.isClickable=false
            binding.player2MidImg.isClickable=false
            binding.player2MinImg.isClickable=false

        }
        else{
            binding.player2Bg.setBackgroundResource(R.drawable.select_bg)
            binding.player1MxImg.isClickable=false
            binding.player1MidImg.isClickable=false
            binding.player1MinImg.isClickable=false
            binding.player2MxImg.isClickable=true
            binding.player2MidImg.isClickable=true
            binding.player2MinImg.isClickable=true
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        job1?.cancel()
        putValueJob?.cancel()
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
    //=== this function will set the item of the player image in the item holder =====//
    private fun setPlayerItem(){
        binding.player1MxImg.setImageResource(player1Item)
        binding.player1MidImg.setImageResource(player1Item)
        binding.player1MinImg.setImageResource(player1Item)
        binding.player2MxImg.setImageResource(player2Item)
        binding.player2MidImg.setImageResource(player2Item)
        binding.player2MinImg.setImageResource(player2Item)
    }
    private fun gameOver(player:Int):Int{
        /*
        * returning zero means the current player can put item
        * returning one means the player-1 can not able to play anymore
        * returning one means the player-2 can not able to play anymore
        */
        var max=0
        if(player==1){
            if(pl1Mx!=0) max=3
            else if(pl1Mid!=0) max=2
            else if(pl1Min!=0) max=1

            for(i in 0..8){
                if(abs(board[i])<max){
                    return 0
                }
            }
            return 1
        }
        else{
            if(pl2Mx!=0) max=-3
            else if(pl2Mid!=0) max=-2
            else if(pl2Min!=0) max=-1
            for(i in 0..8){
                if(abs(board[i])<max){
                    return 0
                }
            }
            return 2
        }
    }
    private fun checkThreeInARow(board: Array<Int>): Pair<Int, List<Int>> {
        val winningCombinations = listOf(listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8), // Bottom row
            listOf(0, 3, 6), // Left column
            listOf(1, 4, 7), // Middle column
            listOf(2, 5, 8), // Right column
            listOf(0, 4, 8), // Top-left to bottom-right diagonal
            listOf(2, 4, 6)  // Top-right to bottom-left diagonal
        )
        for (combination in winningCombinations) {
            val a = board[combination[0]]
            val b = board[combination[1]]
            val c = board[combination[2]]
            if (a > 0 && b > 0 && c > 0) {
                return Pair(1, combination)
            }
            if (a < 0 && b < 0 && c < 0) {
                return Pair(-1, combination)
            }
        }
        return Pair(0, emptyList())
    }
    //======== here we implementing the minimax algorithm for the eat tic tac toe game ==========//
    private suspend fun bot(board: Array<Int>,a3: Int, a2: Int, a1: Int,b3:Int,b2:Int,b1:Int):Pair<Int,Int>{
        var item=0;var index=0
        CoroutineScope(Dispatchers.Default).launch {
            val move = findBestMove(board, a3, a2, a1, b3, b2, b1)
            index = move.first!!
            item = move.second
        }
        return Pair(item,index)
    }
    private fun checkWinner(board: Array<Int>): Int{
        val winningCombinations = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (combination in winningCombinations) {
            val a = board[combination[0]]
            val b = board[combination[1]]
            val c = board[combination[2]]
            if (a > 0 && b > 0 && c > 0) {
                return 1 // player-1 winner //
            }
            if (a < 0 && b < 0 && c < 0) {
                return -1 // player-2 winner //
            }
        }
        for (i in 0..8){
            if(board[i]==0) return 2
        }
        return 0
    }
    private fun evaluate(board: Array<Int>): Int {
        val winner = checkWinner(board)
        return winner
    }
    private suspend fun findBestMove(board: Array<Int>, x3: Int, x2: Int, x1: Int,o3:Int,o2:Int,o1:Int): Pair<Int?, Int> {
        var bestScore = Int.MIN_VALUE
        var bestMove: Int? = null
        var bestItem = 0
        //withTimeoutOrNull(3000) {
            for (i in board.indices) {
                val top = board[i]
                if (x3 > 0 && abs(top) < 3) {
                    board[i] = 3
                    val moveScore = minimax(
                        board,
                        9,
                        Int.MIN_VALUE,
                        Int.MAX_VALUE,
                        false,
                        x3 - 1,
                        x2,
                        x1,
                        o3,
                        o2,
                        o1
                    )
                    board[i] = top
                    if (moveScore > bestScore) {
                        bestScore = moveScore
                        bestMove = i
                        bestItem = 3
                    }
                }
                if (x2 > 0 && abs(top) < 2) {
                    board[i] = 2
                    val moveScore = minimax(
                        board,
                        9,
                        Int.MIN_VALUE,
                        Int.MAX_VALUE,
                        false,
                        x3,
                        x2 - 1,
                        x1,
                        o3,
                        o2,
                        o1
                    )
                    board[i] = top
                    if (moveScore > bestScore) {
                        bestScore = moveScore
                        bestMove = i
                        bestItem = 2
                    }
                }
                if (x1 > 0 && abs(top) < 1) {
                    board[i] = 1
                    val moveScore = minimax(
                        board,
                        9,
                        Int.MIN_VALUE,
                        Int.MAX_VALUE,
                        false,
                        x3,
                        x2,
                        x1 - 1,
                        o3,
                        o2,
                        o1
                    )
                    board[i] = top
                    if (moveScore > bestScore) {
                        bestScore = moveScore
                        bestMove = i
                        bestItem = 1
                    }
                }
            }
        //}
        return Pair(bestMove, bestItem)
    }
    private fun minimax(board: Array<Int>, depth: Int, alphaVal: Int, betaVal: Int, maximizingPlayer: Boolean, x3: Int, x2: Int, x1: Int, o3: Int, o2: Int, o1: Int): Int {
        count++
        var alpha = alphaVal
        var beta = betaVal
        var maxEval = Int.MIN_VALUE
        var minEval = Int.MAX_VALUE
        val winner =checkWinner(board)
        if (winner !=2 || depth == 0) {
            return evaluate(board)
        }
        if (maximizingPlayer) {
            for (i in board.indices) {
                val topVal=board[i]
                if (x3 > 0 && abs(topVal)<3) {
                    board[i] = 3
                    maxEval = max(maxEval, minimax(board, depth - 1, alpha, beta, false, x3 - 1, x2, x1, o3, o2, o1))
                    alpha = max(alpha, maxEval)
                    board[i] = topVal
                }
                if (x2 > 0 && abs(topVal)<2) {
                    board[i] = 2
                    maxEval = max(maxEval, minimax(board, depth - 1, alpha, beta, false, x3, x2 - 1, x1, o3, o2, o1))
                    alpha = max(alpha, maxEval)
                    board[i] = topVal
                }
                if (x1 > 0 && abs(topVal)<1 ) {
                    board[i] = 1
                    maxEval = max(maxEval, minimax(board, depth - 1, alpha, beta, false, x3, x2, x1 - 1, o3, o2, o1))
                    alpha = max(alpha, maxEval)
                    board[i] = topVal
                }
                if (beta <= alpha) break
            }
            return maxEval
        } else {
            for (i in board.indices) {
                val top=board[i]
                if (o3 > 0 && abs(top)<3) {
                    board[i] = -3
                    minEval = min(minEval, minimax(board, depth - 1, alpha, beta, true, x3, x2, x1, o3 - 1, o2, o1))
                    beta = min(beta, minEval)
                    board[i] = top
                }
                if (o2 > 0 && abs(top)<2) {
                    board[i] = -2
                    minEval = min(minEval, minimax(board, depth - 1, alpha, beta, true, x3, x2, x1, o3, o2 - 1, o1))
                    beta = min(beta, minEval)
                    board[i] = top
                }
                if (o1 > 0 && abs(top)<1) {
                    board[i] = -1
                    minEval = min(minEval, minimax(board, depth - 1, alpha, beta, true, x3, x2, x1, o3, o2, o1 - 1))
                    beta = min(beta, minEval)
                    board[i] = top
                }
                if (beta <= alpha) break
            }
            return minEval
        }
    }
}