package com.example.tictactoeclashofxo.gameLogic

import android.app.Dialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.Task
import com.example.tictactoeclashofxo.databinding.ActivityUltimateTicTacToeBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Stack
import java.util.Vector
import kotlin.random.Random

class UltimateTicTacToe : AppCompatActivity() {
    private lateinit var binding:ActivityUltimateTicTacToeBinding
    private lateinit var buttonList:Array<ImageView>
    private lateinit var gridList:Array<GridLayout>
    private var trackBoard = Array(9) {CharArray(9) {'-'} }
    private var localBoard = Array(9) {CharArray(9) {'-'} }
    private var globalBoard = Array(3) {CharArray(3) {'-'} }
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
    private var randomCh:Int=0
    private lateinit var botPlay: Stack<Int>
    private var globalIndex:Int=-1 // it will store the value of GB index which is play able //
    private var taken:Int=0
    private var lastUserPlay:Int=5
    private var whoseTurn:Int=0
    private var background:Int=0
    private var turn=0
    private var putSound: MediaPlayer? =null
    private var winSound: MediaPlayer?=null
    private var loseSound: MediaPlayer?=null
    private var soundOn="0"
    /*
       *the variable <table> contain value in which global board, the user play
       *the variable <index> contain value in which position of local board user play
       *the <index> value will become <table> value for the bot
    */
    private var table:Int=5
    private var index:Int=3
    val B = Array(9) { Board() }
    val Wx = mutableMapOf<Int, Int>()
    val Wo = mutableMapOf<Int, Int>()
    val Tie = mutableMapOf<Int, Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityUltimateTicTacToeBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        gridList= arrayOf(binding.grid1,binding.grid2,binding.grid3,binding.grid4,binding.grid5,binding.grid6,binding.grid7,binding.grid8,binding.grid9)
        for(i in gridList) i.setBackgroundResource(R.drawable.select_bg)
        for (i in 1..9) {
            Wx[i] = -1
            Wo[i] = -1
            Tie[i] = -1
        }
        background=intent.getIntExtra("BACKGROUND",0)
        type=intent.getIntExtra("TYPE",0)
        val myPref= SessionManager(this)
        turn=intent.getIntExtra("TURN",0)
        whoseTurn=turn
        player1Name= myPref.getDetails("PLAYER1")
        player2Name= myPref.getDetails("PLAYER2")
        
        botPlay=Stack<Int>()
        botPlay.push(1)

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
                    playGame(turn)
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
                    playGame(turn)
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
                    playGame(turn)
                }
            }
        }
    }
    //=========== some global variable which are need to run the game ============//
    private var player1First=0
    private var player2First=0
    private var job1: Job? = null
    private var putValueJob: Job?=null
    private fun playGame(turn:Int){
        clickAbility()
        job1?.cancel()
        putValueJob?.cancel()
        if(turn%2==0){
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
                            playGame(turn + 1)
                        }
                    }
                    delay(15000)
                    if (player1InTime == 0) {
                        taken=1
                        job1?.cancel()
                        putValueJob!!.cancel()
                        changeTurn(2)
                        playGame(turn + 1)
                    }
                }
            }
            else{
                //----it is for the bot and trained mode,return the row and column where to put the item ---------//
                if(type==1){
                    //------------ trained mode control here ---------------//
                    val delayTime=Task.getRandomTime()
                    job1=lifecycleScope.launch {
                        delay(delayTime)
                        //putItem(res.first, res.second)
                        changeTurn(2)
                        playGame(turn + 1)
                    }
                }else{
                    //------------ bot mode control here ---------------//
                    val list= listOf(1900,1800,2000,2500,3000)
                    val delayTime=list.random().toLong()
                    job1=lifecycleScope.launch {
                        botTurn()
                        delay(delayTime)
                        changeTurn(2)
                        playGame(turn + 1)
                    }
                }
            }
        }
        else{
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
                        playGame(turn +1)
                    }
                }
                delay(15000)
                if(player2InTime==0){
                    taken=1
                    job1?.cancel()
                    putValueJob!!.cancel()
                    changeTurn(1)
                    playGame(turn+1)
                }
            }
        }
    }
    //========== this is my code ===========//
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
    private fun put(turn: Int, callback: () -> Unit) {
        // user will put cross and bot will put circle i //
        for ((indexes, imageView) in buttonList.withIndex()) {
            val row = indexes / 9
            val column = indexes % 9
            val localRow = row % 3
            val localColumn = column % 3
            val localIndex = (localRow * 3) + localColumn + 1
            if(imageView.isClickable && trackBoard[row][column]=='-') {
                imageView.setOnClickListener {
                    if (turn == 1 && trackBoard[row][column] == '-') {
                        imageView.setImageResource(player1Item)
                        globalIndex = nextGlobalGrid(row, column)
                        userTurn(globalIndex, localIndex, true)
                        trackBoard[row][column] = 'O'
                        callback()
                    } else if (turn == 2 && trackBoard[row][column] == '-') {
                        imageView.setImageResource(player2Item)
                        globalIndex = nextGlobalGrid(row, column)
                        userTurn(globalIndex, localIndex, false)
                        trackBoard[row][column] = 'X'
                        index=localIndex
                        callback()
                    }
                }
            }
        }
    }
    private fun nextGlobalGrid(row:Int,col:Int):Int{
        val localRow = row % 3
        val localCol = col % 3
        return localRow * 3 + localCol + 1
    }
    /*
    *  this function will control the click ability and background set of the global board will control, each turn we need to
    *  set the globalIndex value and every turn first the clickAbility function will be called.
    */
    private fun clickAbility() {
        for (i in gridList) i.background = null // make all grid bg null
        if (globalIndex >= 1) gridList[globalIndex - 1].setBackgroundResource(R.drawable.select_bg) // only the clickable grid has a bg
        when (globalIndex) { //depending on the globalIndex it will only in the local board click able
            1 -> makeClickAble(0, 0, 2, 2)
            2 -> makeClickAble(0, 3, 2, 5)
            3 -> makeClickAble(0, 6, 2, 8)
            4 -> makeClickAble(3, 0, 5, 2)
            5 -> makeClickAble(3, 3, 5, 5)
            6 -> makeClickAble(3, 6, 5, 8)
            7 -> makeClickAble(6, 0, 8, 2)
            8 -> makeClickAble(6, 3, 8, 5)
            9 -> makeClickAble(6, 6, 8, 8)
        }
        if (taken == 1 || globalIndex == -1 || Wx[globalIndex]==1 || Wo[globalIndex]==1 || Tie[globalIndex]==1) {
            for (gridLayout in gridList) gridLayout.setBackgroundResource(R.drawable.select_bg)
            for (imageView in buttonList) imageView.isClickable = true
            for (i in 1..9) {
                if (Wx[i] == 1 || Wo[i] == 1 || Tie[i] == 1) {
                    gridList[i - 1].background = null
                }
            }
        }
    }
    private fun makeClickAble(i:Int,j:Int,ii:Int,jj:Int){
        for ((index, imageView) in buttonList.withIndex()){
            val row = index / 9
            val col = index % 9
            if((row in i..ii) && (col in j..jj)){
                imageView.isClickable = true
            }
            else imageView.isClickable = false
        }
    }
    private fun userTurn(gbIndex: Int,index: Int,player:Boolean){
        if(AvailableBoard(gbIndex)) taken=0
        Play(gbIndex,index, player)
        CheckerBoard()
        lastUserPlay=index
//        if(Wx[index]==1) {
//            gridList[index-1].setBackgroundResource(player1Item)
//            taken=1
//        }
//        else if(Wo[index]==1) {
//            gridList[index - 1].setBackgroundResource(player2Item)
//            taken = 1
//        }
//        else if(Tie[index]==1) taken=1
//        else taken=0
        if (BigWin()==10){ //player-1 champion//
            Toast.makeText(this,"Player-1 win",Toast.LENGTH_SHORT).show()
        }
        else if(BigWin()==-10){//player-2 champion//
            Toast.makeText(this,"Player-2 win",Toast.LENGTH_SHORT).show()
        }
        else if(BigWin()==0){// match is Tie//
            Toast.makeText(this,"Tie",Toast.LENGTH_SHORT).show()
        }
    }
    private fun botTurn(){
        var best = 999999
        var ind = -1
        val alpha=-99999999
        val beta=99999999
        val emp: List<Int>
        if (AvailableBoard(index)) {
            emp = B[index - 1].Empty()
            for (x in emp) {
                val v = Minmax(index - 1, x, 6, alpha, beta, false)
                Undo(index, x, false)
                CheckerBoard()
                if (v < best) {
                    best = v
                    ind = x
                }
            }
        } else {
            for (i in 1..9) {
                val em = All(i)
                for (x in em) {
                    val v = Minmax(i - 1, x, 6, alpha, beta, false)
                    Undo(i, x, false)
                    CheckerBoard()
                    if (v < best) {
                        best = v
                        ind = x
                        index = i
                    }
                }
            }
        }
        Play(index, ind, false)
        setItemInBoard(index,ind)
        CheckerBoard()
        if (Wx[ind] == 1 || Wo[ind] == 1 || Tie[ind] == 1) {
            if (Wx[ind]==1) gridList[ind-1].setBackgroundResource(player1Item)
            else if(Wo[ind]==1) gridList[ind-1].setBackgroundResource(player2Item)
            taken =1
        }
        else globalIndex = ind
        if (BigWin()==2000){ //player-1 champion//

        }
        else if(BigWin()==-2000){//player-2 champion//

        }
        else if(BigWin()==0){// match is Tie//

        }
    }

    //====== this function will take two value gbIndex and lbIndex and put the item in the board ========//
    private fun setItemInBoard(globalIndex: Int, localIndex: Int){
        val globalRow = (globalIndex - 1) / 3
        val globalColumn = (globalIndex - 1) % 3
        val localRow = (localIndex - 1) / 3
        val localColumn = (localIndex - 1) % 3
        val actualRow = globalRow * 3 + localRow
        val actualColumn = globalColumn * 3 + localColumn
        val ans=actualRow * 9 + actualColumn
        buttonList[ans].setImageResource(player1Item)
        val row = index / 9
        val column = index % 9
        trackBoard[row][column]='O'
    }
    private fun Minmax(table: Int, Index: Int, Depth: Int, alpha: Int, beta: Int, whoTurn: Boolean): Int {
        var Max = -Int.MAX_VALUE
        var Min = Int.MAX_VALUE
        var alphaValue=alpha
        var betaValue=beta
        if (Depth == 0) return Calculater()
        if (whoTurn) {
            B[table].play(Index, whoTurn)
            CheckerBoard()
            val Score = BigWin()
            if (Score != 100) return Score
            val emp: List<Int>
            if (AvailableBoard(Index)) {
                emp = B[Index - 1].Empty()
                for (x in emp) {
                    val v = Minmax(Index - 1, x, Depth - 1, alphaValue, betaValue, !whoTurn)
                    if (Depth != 1) Undo(Index, x, whoTurn)
                    CheckerBoard()
                    Min = minOf(v, Min)
                    betaValue = minOf(betaValue, Min)
                    if (betaValue <= alphaValue) break
                }
                return Min
            } else {
                for (i in 1..9) {
                    val em = All(i)
                    for (x in em) {
                        val v = Minmax(i - 1, x, Depth - 1, alphaValue, betaValue, !whoTurn)
                        if (Depth != 1) Undo(i, x, !whoTurn)
                        if (Depth != 1) CheckerBoard()
                        Min = minOf(v, Min)
                        betaValue = minOf(betaValue, Min)
                        if (betaValue <= alphaValue) break
                    }
                }
                return Min
            }
        } else {
            B[table].play(Index,whoTurn)
            CheckerBoard()
            val Score = BigWin()
            if (Score != 100) return Score
            val emp: List<Int>
            if (AvailableBoard(Index)) {
                emp = B[Index - 1].Empty()
                for (x in emp) {
                    val v = Minmax(Index - 1, x, Depth - 1, alphaValue, betaValue, !whoTurn)
                    if (Depth != 1) Undo(Index, x, !whoTurn)
                    CheckerBoard()
                    Max = maxOf(Max, v)
                    alphaValue= maxOf(alphaValue, Max)
                    if (betaValue <= alphaValue) break
                }
                return Max
            } else {
                for (i in 1..9) {
                    val em = All(i)
                    for (x in em) {
                        val v = Minmax(i - 1, x, Depth - 1, alphaValue, betaValue, !whoTurn)
                        if (Depth != 1) Undo(i, x, !whoTurn)
                        CheckerBoard()
                        Max = maxOf(Max, v)
                        alphaValue = maxOf(alphaValue, Max)
                        if (betaValue <= alphaValue) break
                    }
                }
                return Max
            }
        }
    }

    //====== this is for the class components ======//
    fun Undo(board: Int, index: Int, turn: Boolean) {
        B[board - 1].undo(index, turn)
    }
    fun Play(board: Int, index: Int, t: Boolean) {
        B[board - 1].play(index, t)
    }
    fun CheckerBoard() {
        for (q in 0 until 9) {
            when (B[q].score()) {
                0 -> Tie[q + 1] = 1
                1 -> Wx[q + 1] = 1
                -1 -> Wo[q + 1] = 1
                else -> {
                    Wx[q + 1] = -1
                    Wo[q + 1] = -1
                    Tie[q + 1] = -1
                }
            }
        }
    }
    fun AvailableBoard(id: Int): Boolean {
        return B[id - 1].score() == 10
    }
    fun BigWin(): Int {
        if (Wx[1] == 1 && Wx[2] == 1 && Wx[3] == 1) return 2000
        if (Wx[4] == 1 && Wx[5] == 1 && Wx[6] == 1) return 2000
        if (Wx[7] == 1 && Wx[8] == 1 && Wx[9] == 1) return 2000
        if (Wx[1] == 1 && Wx[4] == 1 && Wx[7] == 1) return 2000
        if (Wx[2] == 1 && Wx[5] == 1 && Wx[8] == 1) return 2000
        if (Wx[3] == 1 && Wx[6] == 1 && Wx[9] == 1) return 2000
        if (Wx[1] == 1 && Wx[5] == 1 && Wx[9] == 1) return 2000
        if (Wx[3] == 1 && Wx[5] == 1 && Wx[7] == 1) return 2000
        if (Wo[1] == 1 && Wo[2] == 1 && Wo[3] == 1) return -2000
        if (Wo[4] == 1 && Wo[5] == 1 && Wo[6] == 1) return -2000
        if (Wo[7] == 1 && Wo[8] == 1 && Wo[9] == 1) return -2000
        if (Wo[1] == 1 && Wo[4] == 1 && Wo[7] == 1) return -2000
        if (Wo[2] == 1 && Wo[5] == 1 && Wo[8] == 1) return -2000
        if (Wo[3] == 1 && Wo[6] == 1 && Wo[9] == 1) return -2000
        if (Wo[1] == 1 && Wo[5] == 1 && Wo[9] == 1) return -2000
        if (Wo[3] == 1 && Wo[5] == 1 && Wo[7] == 1) return -2000

        for (i in 0 until 9) {
            if (Wo[i + 1] != 1 && Wx[i + 1] != 1 && Tie[i + 1] != 1)
                return 100
        }
        return 0
    }
    fun Calculater(): Int {
        var avg = 0
        val scores = IntArray(9)
        CheckerBoard()
        for (a in 0 until 9) {
            scores[a] = when {
                Wx[a + 1] == 1 -> 15
                Wo[a + 1] == 1 -> -15
                Tie[a + 1] == 1 -> -999999
                else -> B[a].CalculaterSmall()
            }
        }
        for (score in scores) {
            if (score != -999999) avg += score
        }
        return avg
    }
    fun All(index: Int): List<Int> {
        val emp = mutableListOf<Int>()
        if (!AvailableBoard(index)) {
            return emp
        }
        emp.addAll(B[index - 1].Empty())
        return emp
    }
}

// here we control the minimax and every thing //
class Board{
    var board = ArrayList(listOf('1', '2', '3', '4', '5', '6', '7', '8', '9'))
    var x=Vector<Int>()
    var o=Vector<Int>()
    fun undo(index: Int, turn: Boolean) {
        board[index - 1] = ('0' + index)
        if (turn && x.size>=1) {
            x.removeLast()
        } else {
            if(o.size>=1) o.removeLast()
        }
    }
    fun play(index: Int, turn: Boolean) {
        if (turn) {
            board[index - 1] = 'X'
            x.add(index)
        } else {
            board[index - 1] = 'O'
            o.add(index)
        }
    }
    fun score(): Int {
        if (board[0] == 'X' && board[1] == 'X' && board[2] == 'X')
            return 1
        if (board[3] == 'X' && board[4] == 'X' && board[5] == 'X')
            return 1
        if (board[6] == 'X' && board[7] == 'X' && board[8] == 'X')
            return 1
        if (board[0] == 'X' && board[3] == 'X' && board[6] == 'X')
            return 1
        if (board[1] == 'X' && board[4] == 'X' && board[7] == 'X')
            return 1
        if (board[2] == 'X' && board[5] == 'X' && board[8] == 'X')
            return 1
        if (board[0] == 'X' && board[4] == 'X' && board[8] == 'X')
            return 1
        if (board[2] == 'X' && board[4] == 'X' && board[6] == 'X')
            return 1
        if (board[0] == 'O' && board[1] == 'O' && board[2] == 'O')
            return -1
        if (board[3] == 'O' && board[4] == 'O' && board[5] == 'O')
            return -1
        if (board[6] == 'O' && board[7] == 'O' && board[8] == 'O')
            return -1
        if (board[0] == 'O' && board[3] == 'O' && board[6] == 'O')
            return -1
        if (board[1] == 'O' && board[4] == 'O' && board[7] == 'O')
            return -1
        if (board[2] == 'O' && board[5] == 'O' && board[8] == 'O')
            return -1
        if (board[0] == 'O' && board[4] == 'O' && board[8] == 'O')
            return -1
        if (board[2] == 'O' && board[4] == 'O' && board[6] == 'O')
            return -1
        if (o.size + x.size == 9)
            return 0
        return 10
    }
    fun Empty(): List<Int> {
        val m = mutableListOf<Int>()
        for (i in 1..9) {
            if (check(i)) m.add(i)
        }
        return m
    }
    fun check(index: Int): Boolean {
        for (i in x) {
            if (index == i) {
                return false
            }
        }
        for (i in o) {
            if (index == i) {
                return false
            }
        }
        return true
    }
    fun CheckXO(inx: Int): Int {
        for (ch in x) {
            if (ch == inx) {
                return 1
            }
        }
        for (ch in o) {
            if (ch == inx) {
                return -1
            }
        }
        return 0
    }
    fun CalculaterSmall():Int{
        val Scores = IntArray(9) { CheckXO(it + 1) }
        for(i in 1..9){
            Scores[i-1]=CheckXO(i)
        }
        var avg = 0
        for (i in 1..9) {
            Scores[i - 1] = CheckXO(i)
        }
        val flag = BooleanArray(9) { true}

        if (Scores[0] == 1) {
            if (Scores[1] != -1 || Scores[2] != -1) {
                if (Scores[1] == 1 || Scores[2] == 1) {
                    avg += 2
                    flag[0] = false
                }
            }
            if (Scores[3] != -1 || Scores[6] != -1) {
                if (Scores[3] == 1 || Scores[6] == 1) {
                    avg += 2;
                    flag[1] = false;
                }
            }
            if (Scores[4] != -1 || Scores[8] != -1) {
                if (Scores[4] == 1 || Scores[8] == 1) {
                    avg += 2;
                    flag[2] = false;
                }
            }
        }

        if (Scores[1] == 1) {
            if ((Scores[0] != -1 || Scores[2] != -1) && flag[0]) {
                if (Scores[0] == 1 || Scores[2] == 1) {
                    avg += 2;
                    flag[0] = false;
                }
            }
            if (Scores[4] != -1 || Scores[7] != -1) {
                if (Scores[4] == 1 || Scores[7] == 1) {
                    avg += 2;
                    flag[3] = false;
                }
            }

        }
        if (Scores[2] == 1) {
            if ((Scores[1] != -1 || Scores[0] != -1) && flag[0]) {
                if (Scores[1] == 1 || Scores[0] == 1) {
                    avg += 2;
                    flag[0] = false;
                }
            }
            if (Scores[5] != -1 || Scores[8] != -1) {
                if (Scores[5] == 1 || Scores[8] == 1) {
                    avg += 2;
                    flag[4] = false;
                }
            }
            if (Scores[4] != -1 || Scores[6] != -1) {
                if (Scores[4] == 1 || Scores[6] == 1) {
                    avg += 2;
                    flag[5] = false;
                }
            }
        }
        if (Scores[3] == 1) {
            if ((Scores[0] != -1 || Scores[6] != -1) && flag[1]) {
                if (Scores[0] == 1 || Scores[6] == 1) {
                    avg += 2;
                    flag[1] = false;
                }
            }
            if (Scores[4] != -1 || Scores[5] != -1) {
                if (Scores[4] == 1 || Scores[5] == 1) {
                    avg += 2;
                    flag[6] = false;
                }
            }
        }
        if (Scores[4] == 1) {
            if ((Scores[0] != -1 || Scores[8] != -1) && flag[2]) {
                if (Scores[0] == 1 || Scores[8] == 1) {
                    avg += 2;
                    flag[2] = false;
                }
            }
            if ((Scores[1] != -1 || Scores[7] != -1) && flag[3]) {
                if (Scores[1] == 1 || Scores[7] == 1) {
                    avg += 2;
                    flag[3] = false;
                }
            }
            if ((Scores[2] != -1 || Scores[6] != -1) && flag[5]) {
                if (Scores[2] == 1 || Scores[6] == 1) {
                    avg += 2;
                    flag[5] = false;
                }
            }
            if ((Scores[3] != -1 || Scores[5] != -1) && flag[6]) {
                if (Scores[3] == 1 || Scores[5] == 1) {
                    avg += 2;
                    flag[6] = false;
                }
            }
        }
        if (Scores[5] == 1) {
            if ((Scores[2] != -1 || Scores[8] != -1) && flag[4]) {
                if (Scores[2] == 1 || Scores[8] == 1) {
                    avg += 2;
                    flag[4] = false;
                }
            }
            if ((Scores[3] != -1 || Scores[4] != -1) && flag[6]) {
                if (Scores[3] == 1 || Scores[4] == 1) {
                    avg += 2;
                    flag[6] = false;
                }
            }
        }
        if (Scores[6] == 1) {
            if ((Scores[0] != -1 || Scores[3] != -1) && flag[1]) {
                if (Scores[0] == 1 || Scores[3] == 1) {
                    avg += 2;
                    flag[1] = false;
                }
            }
            if (Scores[7] != -1 || Scores[8] != -1) {
                if (Scores[7] == 1 || Scores[8] == 1) {
                    avg += 2;
                    flag[7] = false;
                }
            }
            if ((Scores[2] != -1 || Scores[4] != -1) && flag[5]) {
                if (Scores[2] == 1 || Scores[4] == 1) {
                    avg += 2;
                    flag[5] = false;
                }
            }
        }
        if (Scores[7] == 1) {
            if ((Scores[1] != -1 || Scores[4] != -1) && flag[3]) {
                if (Scores[1] == 1 || Scores[4] == 1) {
                    avg += 2;
                    flag[3] = false;
                }
            }
            if ((Scores[6] != -1 || Scores[8] != -1) && flag[7]) {
                if (Scores[6] == 1 || Scores[8] == 1) {
                    avg += 2;
                    flag[7] = false;
                }
            }
        }
        if (Scores[8] == 1) {
            if ((Scores[6] != -1 || Scores[7] != -1) && flag[7]) {
                if (Scores[6] == 1 || Scores[7] == 1) {
                    avg += 2;
                    flag[7] = false;
                }
            }
            if ((Scores[2] != -1 || Scores[5] != -1) && flag[4]) {
                if (Scores[2] == 1 || Scores[5] == 1) {
                    avg += 2;
                    flag[4] = false;
                }
            }
            if ((Scores[0] != -1 || Scores[4] != -1) && flag[2]) {
                if (Scores[0] == 1 || Scores[4] == 1) {
                    avg += 2;
                    flag[2] = false;
                }
            }
        }
        if (Scores[0] == 1 && flag[0] && flag[1] && flag[2]) {
            avg++;
        }
        if (Scores[1] == 1 && flag[0] && flag[3]) {
            avg++;
        }
        if (Scores[2] == 1 && flag[0] && flag[4] && flag[5]) {
            avg++;
        }
        if (Scores[3] == 1 && flag[1] && flag[6]) {
            avg++;
        }
        if (Scores[4] == 1 && flag[2] && flag[3] && flag[5] && flag[6]) {
            avg++;
        }
        if (Scores[5] == 1 && flag[4] && flag[6]) {
            avg++;
        }
        if (Scores[6] == 1 && flag[1] && flag[5] && flag[7]) {
            avg++;
        }
        if (Scores[7] == 1 && flag[3] && flag[7]) {
            avg++;
        }
        if (Scores[8] == 1 && flag[2] && flag[7] && flag[4]) {
            avg++;
        }

        // O Scores
        if (Scores[0] == -1) {
            if (Scores[1] != 1 || Scores[2] != 1) {
                if (Scores[1] == -1 || Scores[2] == -1) {
                    avg -= 2;
                    flag[0] = false;
                }
            }
            if (Scores[3] != 1 || Scores[6] != 1) {
                if (Scores[3] == -1 || Scores[6] == -1) {
                    avg -= 2;
                    flag[1] = false;
                }
            }
            if (Scores[4] != 1 || Scores[8] != 1) {
                if (Scores[4] == -1 || Scores[8] == -1) {
                    avg -= 2;
                    flag[2] = false;
                }
            }
        }

        if (Scores[1] == -1) {
            if ((Scores[0] != 1 || Scores[2] != 1) && flag[0]) {
                if (Scores[0] == -1 || Scores[2] == -1) {
                    avg -= 2;
                    flag[0] = false;
                }
            }
            if (Scores[4] != 1 || Scores[7] != 1) {
                if (Scores[4] == -1 || Scores[7] == -1) {
                    avg -= 2;
                    flag[3] = false;
                }
            }

        }
        if (Scores[2] == -1) {
            if ((Scores[1] != 1 || Scores[0] != 1) && flag[0]) {
                if (Scores[1] == -1 || Scores[0] == -1) {
                    avg -= 2;
                    flag[0] = false;
                }
            }
            if (Scores[5] != 1 || Scores[8] != 1) {
                if (Scores[5] == -1 || Scores[8] == -1) {
                    avg -= 2;
                    flag[4] = false;
                }
            }
            if (Scores[4] != 1 || Scores[6] != 1) {
                if (Scores[4] == -1 || Scores[6] == -1) {
                    avg -= 2;
                    flag[5] = false;
                }
            }
        }
        if (Scores[3] == -1) {
            if ((Scores[0] != 1 || Scores[6] != 1) && flag[1]) {
                if (Scores[0] == -1 || Scores[6] == -1) {
                    avg -= 2;
                    flag[1] = false;
                }
            }
            if (Scores[4] != 1 || Scores[5] != 1) {
                if (Scores[4] == -1 || Scores[5] == -1) {
                    avg -= 2;
                    flag[6] = false;
                }
            }
        }
        if (Scores[4] == -1) {
            if ((Scores[0] != 1 || Scores[8] != 1) && flag[2]) {
                if (Scores[0] == -1 || Scores[8] == -1) {
                    avg -= 2;
                    flag[2] = false;
                }
            }
            if ((Scores[1] != 1 || Scores[7] != 1) && flag[3]) {
                if (Scores[1] == -1 || Scores[7] == -1) {
                    avg -= 2;
                    flag[3] = false;
                }
            }
            if ((Scores[2] != 1 || Scores[6] != 1) && flag[5]) {
                if (Scores[2] == -1 || Scores[6] == -1) {
                    avg -= 2;
                    flag[5] = false;
                }
            }
            if ((Scores[3] != 1 || Scores[5] != 1) && flag[6]) {
                if (Scores[3] == -1 || Scores[5] == -1) {
                    avg -= 2;
                    flag[6] = false;
                }
            }
        }
        if (Scores[5] == -1) {
            if ((Scores[2] != 1 || Scores[8] != 1) && flag[4]) {
                if (Scores[2] == -1 || Scores[8] == -1) {
                    avg -= 2;
                    flag[4] = false;
                }
            }
            if ((Scores[3] != 1 || Scores[4] != 1) && flag[6]) {
                if (Scores[3] == -1 || Scores[4] == -1) {
                    avg -= 2;
                    flag[6] = false;
                }
            }
        }
        if (Scores[6] == -1) {
            if ((Scores[0] != 1 || Scores[3] != 1) && flag[1]) {
                if (Scores[0] == -1 || Scores[3] == -1) {
                    avg -= 2;
                    flag[1] = false;
                }
            }
            if (Scores[7] != 1 || Scores[8] != 1) {
                if (Scores[7] == -1 || Scores[8] == -1) {
                    avg -= 2;
                    flag[7] = false;
                }
            }
            if ((Scores[2] != 1 || Scores[4] != 1) && flag[5]) {
                if (Scores[2] == 1 || Scores[4] == -1) {
                    avg -= 2;
                    flag[5] = false;
                }
            }
        }
        if (Scores[7] == -1) {
            if ((Scores[1] != 1 || Scores[4] != 1) && flag[3]) {
                if (Scores[1] == -1 || Scores[4] == -1) {
                    avg -= 2;
                    flag[3] = false;
                }
            }
            if ((Scores[6] != 1 || Scores[8] != 1) && flag[7]) {
                if (Scores[6] == -1 || Scores[8] == -1) {
                    avg -= 2;
                    flag[7] = false;
                }
            }
        }
        if (Scores[8] == -1) {
            if ((Scores[6] != 1 || Scores[7] != 1) && flag[7]) {
                if (Scores[6] == -1 || Scores[7] == -1) {
                    avg -= 2;
                    flag[7] = false;
                }
            }
            if ((Scores[2] != 1 || Scores[5] != 1) && flag[4]) {
                if (Scores[2] == -1 || Scores[5] == -1) {
                    avg -= 2;
                    flag[4] = false;
                }
            }
            if ((Scores[0] != 1 || Scores[4] != 1) && flag[2]) {
                if (Scores[0] == -1 || Scores[4] == -1) {
                    avg -= 2;
                    flag[2] = false;
                }
            }
        }
        if (Scores[0] == -1 && flag[0] && flag[1] && flag[2]) {
            avg--
        }
        if (Scores[1] == -1 && flag[0] && flag[3]) {
            avg--
        }
        if (Scores[2] == -1 && flag[0] && flag[4] && flag[5]) {
            avg--
        }
        if (Scores[3] == -1 && flag[1] && flag[6]) {
            avg--
        }
        if (Scores[4] == -1 && flag[2] && flag[3] && flag[5] && flag[6]) {
            avg--
        }
        if (Scores[5] == -1 && flag[4] && flag[6]) {
            avg--
        }
        if (Scores[6] == -1 && flag[1] && flag[5] && flag[7]) {
            avg--
        }
        if (Scores[7] == -1 && flag[3] && flag[7]) {
            avg--
        }
        if (Scores[8] == -1 && flag[2] && flag[7] && flag[4]) {
            avg--
        }
        return avg;
    }
}
