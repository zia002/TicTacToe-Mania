package com.example.tictactoeclashofxo.gameLogic

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.WinnerActivity
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.Task
import com.example.tictactoeclashofxo.databinding.ActivitySuperTicTacToeBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt

class SuperTicTacToe : AppCompatActivity() {
    private lateinit var binding:ActivitySuperTicTacToeBinding
    private var localBoard = Array(9) {CharArray(9) {'-'} }
    private var globalBoard = Array(3) {CharArray(3) {'-'} }
    private lateinit var buttonList:Array<ImageView>
    private lateinit var gridList:Array<GridLayout>
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
    private var player1tmp= listOf('1','1','1')
    private var player2tmp= listOf('2','2','2')
    private var rand=0
    private var type:Int=0
    private var whoseTurn:Int=0
    private var background:Int=0
    private var turn=0
    private var putSound: MediaPlayer? =null
    private var winSound: MediaPlayer?=null
    private var loseSound: MediaPlayer?=null
    private var soundOn="0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySuperTicTacToeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //----------- here we collect all the value which we need ---------------//
        background=intent.getIntExtra("BACKGROUND",0)
        type=intent.getIntExtra("TYPE",0)
        val myPref= SessionManager(this)
        turn=intent.getIntExtra("TURN",0)
        whoseTurn=turn
        player1Name= myPref.getDetails("PLAYER1")
        player2Name= myPref.getDetails("PLAYER2")
        buttonList= arrayOf(
            binding.g41,binding.g42,binding.g43,binding.g44,binding.g45,binding.g46,binding.g47,binding.g48,binding.g49,binding.g410,
            binding.g411,binding.g412,binding.g413,binding.g414,binding.g415,binding.g416,binding.g417,binding.g418,binding.g419,binding.g420,
            binding.g421,binding.g422,binding.g423,binding.g424,binding.g425,binding.g426,binding.g427,binding.g428,binding.g429,binding.g430,
            binding.g431,binding.g432,binding.g433,binding.g434,binding.g435,binding.g436,binding.g437,binding.g438,binding.g439,binding.g440,
            binding.g441,binding.g442,binding.g443,binding.g444,binding.g445,binding.g446,binding.g447,binding.g448,binding.g449,binding.g450,
            binding.g451,binding.g452,binding.g453,binding.g454,binding.g455,binding.g456,binding.g457,binding.g458,binding.g459,binding.g460,
            binding.g461,binding.g462,binding.g463,binding.g464,binding.g465,binding.g466,binding.g467,binding.g468,binding.g469,binding.g470,
            binding.g471,binding.g472,binding.g473,binding.g474,binding.g475,binding.g476,binding.g477,binding.g478,binding.g479,binding.g480, binding.g481
        )
        gridList= arrayOf(binding.grid1,binding.grid2,binding.grid3,binding.grid4,binding.grid5,binding.grid6,binding.grid7,binding.grid8,binding.grid9)
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
        val list= listOf(19,8,6,13,10)
        rand=list.random()
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
    private var result=-1
    private var job1: Job? = null
    private var putValueJob: Job?=null
    private fun play(turn:Int){
        job1?.cancel()
        putValueJob?.cancel()
        result=checkWinner() //== global board winner check here ==//
        if(result==1 || result==2 || result==0){
            cancelClick()
            binding.player1Bg.background=null
            binding.player2Bg.background=null
            val time1=(SystemClock.elapsedRealtime()-player1Timer.base).toString()
            val time2=(SystemClock.elapsedRealtime()-player2Timer.base).toString()
            var winner:String="Draw"
            var loser:String="Draw"
            var winnerTime:String="0"
            var loserTime:String="0"
            if(result==1){
                winner=player1Name.toString()
                loser=player2Name.toString()
                winnerTime=time1
                loserTime=time2
                val res=checkWinnerTurn(globalBoard)
                for(i in res.second){
                    gridList[i].setBackgroundResource(R.drawable.win_bg)
                }
                if (soundOn=="1") loseSound?.start()
            }
            else if(result==2){
                winner=player2Name.toString()
                loser=player1Name.toString()
                winnerTime=time2
                loserTime=time1
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
            intent.putExtra("MODE",1)
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
                cancelTurn(1)
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
//                    delay(10500)
//                    if (player1InTime == 0) {
//                        job1?.cancel()
//                        putValueJob!!.cancel()
//                        changeTurn(2)
//                        play(turn + 1)
//                    }
                }
            }
            else{
                    /*
                      * when the first time the played grid of the bot is over ,then find, first position from the global board
                      * now get the local board of the position and get the local board position to play
                    */
                    var result=bot(globalBoard,0,0,2,2)
                    var position=result.first*3 + result.second + 1
                    var res=0
                    var index=0
                    if(type==1){
                        // it's now trainer mode to put the item
                        var id=0
                        if(turn%rand==0 || (turn+1)%rand==0){
                            val list= mutableListOf<Int>()
                            for(i in 0..2){
                                for(j in 0..2){
                                    if(globalBoard[i][j]=='-'){
                                        id=i*3 + j
                                        list.add(id)
                                    }
                                }
                            }
                            position=list.random()
                        }
                    }
                    when(position){
                        1->{
                            result=bot(localBoard,0,0,2,2)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(0,0,2,2)
                            if(res!=-1){
                                cancelClickGrid(0,0,2,2,res)
                            }
                        }
                        2->{
                            result=bot(localBoard,0,3,2,5)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(0,3,2,5)
                            if(res!=-1){
                                cancelClickGrid(0,3,2,5,res)
                            }
                        }
                        3->{
                            result=bot(localBoard,0,6,2,8)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(0,6,2,8)
                            if(res!=-1){
                                cancelClickGrid(0,6,2,8,res)
                            }
                        }
                        4->{
                            result=bot(localBoard,3,0,5,2)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(3,0,5,2)
                            if(res!=-1){
                                cancelClickGrid(3,0,5,2,res)
                            }
                        }
                        5->{
                            result=bot(localBoard,3,3,5,5)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(3,3,5,5)
                            if(res!=-1){
                                cancelClickGrid(3,3,5,5,res)
                            }
                        }
                        6->{
                            result=bot(localBoard,3,6,5,8)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(3,6,5,8)
                            if(res!=-1){
                                cancelClickGrid(3,6,5,8,res)
                            }
                        }
                        7->{
                            result=bot(localBoard,6,0,8,2)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(6,0,8,2)
                            if(res!=-1){
                                cancelClickGrid(6,0,8,2,res)
                            }
                        }
                        8->{
                            result=bot(localBoard,6,3,8,5)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(6,3,8,5)
                            if(res!=-1){
                                cancelClickGrid(6,3,8,5,res)
                            }
                        }
                        9->{
                            result=bot(localBoard,6,6,8,8)
                            localBoard[result.first][result.second]='1'
                            index=result.first*9 + result.second
                            res=checkLocalGrid(6,6,8,8)
                            if(res!=-1){
                                cancelClickGrid(6,6,8,8,res)
                            }
                        }
                    }
                    val delayTime=Task.getRandomTime()
                    job1=lifecycleScope.launch {
                        delay(delayTime)
                        buttonList[index].setImageResource(player1Item)
                        if (soundOn=="1") putSound?.start()
                        changeTurn(2)
                        play(turn + 1)
                    }
//                }
            }
        }
        else{
            cancelTurn(1)
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
//                delay(10500)
//                if(player2InTime==0){
//                    job1?.cancel()
//                    putValueJob!!.cancel()
//                    changeTurn(1)
//                    play(turn+1)
//                }
            }
        }
    }
    private fun put(turn: Int, callback: () -> Unit) {
        for ((index, imageView) in buttonList.withIndex()) {
            val row = index / 9
            val column = index % 9
            imageView.setOnClickListener {
                if (localBoard[row][column]=='-') {
                    if (turn == 1) {
                        imageView.setImageResource(player1Item)
                        if (soundOn=="1") putSound?.start()
                        localBoard[row][column]='1'
                        putItem(row,column,1)
                        callback()
                    } else if (turn == 2) {
                        imageView.setImageResource(player2Item)
                        if (soundOn=="1") putSound?.start()
                        localBoard[row][column]='2'
                        putItem(row,column,2)
                        cancelTurn(0)
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
    private fun putItem(row:Int,col:Int,player:Int){
        if(player==1) {
            val index = (row * 8) + row + col
            localBoard[row][col] = '1'
            buttonList[index].setImageResource(player1Item)
        }
        else if(player==2){
            val index = (row * 8) + row + col
            localBoard[row][col] = '2'
            buttonList[index].setImageResource(player2Item)
        }
        if(row in 0..2 && col in 0..2){
            val res=checkLocalGrid(0,0,2,2)
            if(res!=-1){
                cancelClickGrid(0,0,2,2,res)
            }
        }
        else if(row in 0..2 && col in 3..5){
            val res=checkLocalGrid(0,3,2,5)
            if(res!=-1){
                cancelClickGrid(0,3,2,5,res)
            }
        }
        else if(row in 0..2 && col in 6..8){
            val res=checkLocalGrid(0,6,2,8)
            if(res!=-1){
                cancelClickGrid(0,6,2,8,res)
            }
        }
        else if(row in 3..5 && col in 0..2){
            val res=checkLocalGrid(3,0,5,2)
            if(res!=-1){
                cancelClickGrid(3,0,5,2,res)
            }
        }
        else if(row in 3..5 && col in 3..5){
            val res=checkLocalGrid(3,3,5,5)
            if(res!=-1){
                cancelClickGrid(3,3,5,5,res)
            }
        }
        else if(row in 3..5 && col in 6..8){
            val res=checkLocalGrid(3,6,5,8)
            if(res!=-1){
                cancelClickGrid(3,6,5,8,res)
            }
        }
        else if(row in 6..8 && col in 0..2){
            val res=checkLocalGrid(6,0,8,2)
            if(res!=-1){
                cancelClickGrid(6,0,8,2,res)
            }
        }
        else if(row in 6..8 && col in 3..5){
            val res=checkLocalGrid(6,3,8,5)
            if(res!=-1){
                cancelClickGrid(6,3,8,5,res)
            }
        }
        else if(row in 6..8 && col in 6..8){
            val res=checkLocalGrid(6,6,8,8)
            if(res!=-1){
                cancelClickGrid(6,6,8,8,res)
            }
        }
    }
    private fun cancelClick(){
        player1Timer.stop()
        player2Timer.stop()
        for(imageView in buttonList) imageView.isClickable=false
    }
    private fun checkLocalGrid(r1:Int,c1:Int,r2:Int,c2:Int):Int{
        //-------- row check --------//
        for(i in r1..r2){
            val tmp=ArrayList<Char>()
            for(j in c1..c2){
                tmp.add(localBoard[i][j])
            }
            if(tmp==player1tmp) return 1
            else if(tmp==player2tmp) return 2
        }
        //------- col check ---------//
        for(i in c1..c2){
            val tmp=ArrayList<Char>()
            for(j in r1..r2){
                tmp.add(localBoard[j][i])
            }
            if(tmp==player1tmp) return 1
            else if(tmp==player2tmp) return 2
        }
        //-------- diagonal-1 check -------//
        if(localBoard[r1][c1]==localBoard[r1+1][c1+1] && localBoard[r1+2][c1+2]==localBoard[r1+1][c1+1] && localBoard[r1][c1]=='1') return 1
        else if(localBoard[r1][c1]==localBoard[r1+1][c1+1] && localBoard[r1+2][c1+2]==localBoard[r1+1][c1+1] && localBoard[r1][c1]=='2') return 2
        //-------- diagonal-2 check -------//
        if(localBoard[r1][c2]==localBoard[r1+1][c2-1] && localBoard[r1+2][c2-2]==localBoard[r1][c2] && localBoard[r1][c2]=='1') return 1
        else if(localBoard[r1][c2]==localBoard[r1+1][c2-1] && localBoard[r1+2][c2-2]==localBoard[r1][c2] && localBoard[r1][c2]=='2') return 2

        for(i in r1..r2){
            for(j in c1..c2){
                if(localBoard[i][j]=='-') return -1
            }
        }
        return 0
    }
    private fun checkWinner():Int{
        //======== winner logic here,player1 win return 1,player2 win return 2 and filled board return 0 otherwise -1 =======//
        //-------- row check --------//
        for(i in 0..2){
            val tmp=ArrayList<Char>()
            for(j in 0..2){
                tmp.add(globalBoard[i][j])
            }
            if(tmp==player1tmp) return 1
            else if(tmp==player2tmp) return 2
        }
        //------- col check ---------//
        for(i in 0..2){
            val tmp=ArrayList<Char>()
            for(j in 0..2){
                tmp.add(globalBoard[j][i])
            }
            if(tmp==player1tmp) return 1
            else if(tmp==player2tmp) return 2
        }
        //-------- diagonal-1 check -------//
        if(globalBoard[0][0]==globalBoard[1][1] && globalBoard[2][2]==globalBoard[1][1] && globalBoard[0][0]=='1') return 1
        else if(globalBoard[0][0]==globalBoard[1][1] && globalBoard[2][2]==globalBoard[1][1] && globalBoard[0][0]=='2') return 2
        //-------- diagonal-2 check -------//
        if(globalBoard[0][2]==globalBoard[1][1] && globalBoard[2][0]==globalBoard[1][1] && globalBoard[1][1]=='1') return 1
        else if(globalBoard[0][2]==globalBoard[1][1] && globalBoard[2][0]==globalBoard[1][1] && globalBoard[1][1]=='2') return 2

        for(i in 0..2){
            for(j in 0..2){
                if(globalBoard[i][j]=='-') return -1
            }
        }
        return 0
    }
    private fun cancelClickGrid(i:Int,j:Int,ii:Int,jj:Int,pl:Int){
        var first=1
        for ((index, imageView) in buttonList.withIndex()){
            val row = index / 9
            val col = index % 9
            val gRow=row/3
            val gCol=col/3
            if(pl==0){
                imageView.isClickable = false
                if((row in i..ii) && (col in j..jj)){
                    imageView.visibility = View.VISIBLE
                    globalBoard[gRow][gCol]='0'
                }
            }
            else {
                imageView.isClickable = false
                if (first == 1 && (row in i..ii) && (col in j..jj)) {
                    imageView.setPadding(50, 50, 50, 50)
                    imageView.visibility = View.VISIBLE
                    if (pl == 1) {
                        globalBoard[gRow][gCol] = '1'
                        imageView.setImageResource(player1Item)
                    } else if (pl == 2) {
                        globalBoard[gRow][gCol] = '2'
                        imageView.setImageResource(player2Item)
                    }
                    first = 0
                } else if ((row in i..ii) && (col in j..jj)) {
                    imageView.visibility = View.GONE
                }
            }
        }
    }
    //========= bot and trainer mode control here ==============//
    private fun isEmpty(b: Array<CharArray>,r1:Int,c1:Int,r2:Int,c2:Int): Boolean {
        for (i in r1..r2) {
            for (j in c1..c2) {
                if (b[i][j] == '-') return true
            }
        }
        return false
    }
    private fun win(b: Array<CharArray>,r1:Int,c1:Int,r2:Int,c2:Int): Int {
        //--- row check ----//
        for(i in r1..r2){
            if (b[i][c1] == b[i][c1+1] && b[i][c1+1] == b[i][c1+2] && b[i][c1] == '1') return 1
            else if (b[i][c1] == b[i][c1+1] && b[i][c1+1] == b[i][c1+2] && b[i][c1]=='2') return -1
        }
        //--- column check ----//
        for(i in c1..c2){
            if (b[r1][i] == b[r1+1][i] && b[r1+1][i] == b[r1+2][i] && b[r1][i] == '1') return 1
            else if (b[r1][i] == b[r1+1][i] && b[r1+1][i] == b[r1+2][i] && b[r1][i] == '2') return -1
        }
        //--- primary dia check ---//
        if(b[r1][c1]==b[r1+1][c1+1] && b[r1+1][c1+1]==b[r1+2][c1+2] && b[r1][c1]=='1') return 1
        else if(b[r1][c1]==b[r1+1][c1+1] && b[r1+1][c1+1]==b[r1+2][c1+2] && b[r1][c1]=='2') return -1
        //--- secondary dia check ---//
        if(b[r1][c2]==b[r1+1][c2-1] && b[r1+1][c2-1]==b[r1+2][c2-2] && b[r1][c2]=='1') return 1
        else if(b[r1][c2]==b[r1+1][c2-1] && b[r1+1][c2-1]==b[r1+2][c2-2] && b[r1][c2]=='2') return -1

        return 2
    }
    private fun minimax(b: Array<CharArray>, ai: Boolean, alpha: Int, beta: Int, r1:Int, c1:Int, r2:Int, c2:Int): Int {
        val score = win(b,r1,c1,r2,c2)
        if (score == 1) return score
        else if (score == -1) return score
        else if (!isEmpty(b,r1,c1,r2,c2)) return 0
        var alphaVar = alpha
        var betaVar = beta
        if (ai) {
            var best = Int.MIN_VALUE
            for (i in r1 .. r2) {
                for (j in c1.. c2) {
                    if (b[i][j] == '-') {
                        b[i][j] = '1'
                        best = maxOf(best, minimax(b, false, alphaVar, betaVar,r1,c1,r2,c2))
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
            for (i in r1..r2) {
                for (j in c1..c2) {
                    if (b[i][j] == '-') {
                        b[i][j] = '2'
                        best = minOf(best, minimax(b, true, alphaVar, betaVar,r1,c1,r2,c2))
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
    private fun bot(b: Array<CharArray>,r1:Int,c1:Int,r2:Int,c2:Int):Pair<Int,Int> {
        var score: Int
        var best = Int.MIN_VALUE
        var row = 0
        var col = 0
        for (i in r1 ..r2) {
            for (j in c1 ..c2) {
                if (b[i][j] == '-') {
                    b[i][j] = '1'
                    score = minimax(b,false,Int.MIN_VALUE,Int.MAX_VALUE,r1,c1,r2,c2)
                    b[i][j] = '-'
                    if (score > best) {
                        row = i
                        col = j
                        best = score
                    }
                }
            }
        }
        return Pair(row, col)
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
    //==========================================================//
    override fun onDestroy() {
        super.onDestroy()
        job1?.cancel()
        putValueJob?.cancel()
    }
}