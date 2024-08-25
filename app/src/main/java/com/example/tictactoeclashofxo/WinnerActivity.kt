package com.example.tictactoeclashofxo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoeclashofxo.database.AdLoad
import com.example.tictactoeclashofxo.databinding.ActivityWinner2Binding
import com.example.tictactoeclashofxo.gameLogic.Connect4TicTacToe
import com.example.tictactoeclashofxo.gameLogic.Connect5TicTacToe
import com.example.tictactoeclashofxo.gameLogic.EatTicTacToe
import com.example.tictactoeclashofxo.gameLogic.InfiniteTicTacToe
import com.example.tictactoeclashofxo.gameLogic.SimpleTicTacToe
import com.example.tictactoeclashofxo.gameLogic.SuperTicTacToe
import com.example.tictactoeclashofxo.gameLogic.UltimateTicTacToe
import com.example.tictactoeclashofxo.newCode.BattleActivity

class WinnerActivity : AppCompatActivity() {
    private lateinit var binding:ActivityWinner2Binding
    private var background=0
    private var type=0
    private var turn=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWinner2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        AdLoad.loadInterstitial(this)
        binding.winnerTime.text= intent.getStringExtra("WinTime")
        binding.loserTime.text=intent.getStringExtra("LoseTime")
        binding.WinnerName.text=intent.getStringExtra("WinnerName")
        binding.LoserName.text=intent.getStringExtra("LoserName")
        background=intent.getIntExtra("BACKGROUND",0)
        type=intent.getIntExtra("TYPE",0)
        turn=intent.getIntExtra("TURN",0)
        val mode=intent.getIntExtra("MODE",0)


        if(AdLoad.mInterstitialAd!=null){
            AdLoad.mInterstitialAd!!.show(this)
            Log.d("adShow","load")
            AdLoad.loadInterstitial(this)
        }
        else{
            Log.d("adShow","Not load")
            AdLoad.loadInterstitial(this)
        }
        binding.backToHome.setOnClickListener {
            val intent= Intent(this,BattleActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.againStart.setOnClickListener {
            gotoGame(mode)
        }
    }
    private fun gotoGame(mode:Int){
        when(mode){
            1->{
                val intent=Intent(this, SimpleTicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()
            }
            2->{
                val intent=Intent(this, EatTicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()
            }
            3->{
                val intent=Intent(this, InfiniteTicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()

            }
            4->{
                val intent=Intent(this, UltimateTicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()
            }
            5->{
                val intent=Intent(this, Connect4TicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()
            }
            6->{
                val intent=Intent(this, SuperTicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()
            }
            7->{
                val intent=Intent(this, Connect5TicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()
            }
        }
    }
}
