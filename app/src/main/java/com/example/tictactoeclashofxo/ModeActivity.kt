package com.example.tictactoeclashofxo

import android.app.Dialog
import com.example.tictactoeclashofxo.gameLogic.Connect5TicTacToe
import com.example.tictactoeclashofxo.gameLogic.SimpleTicTacToe
import com.example.tictactoeclashofxo.gameLogic.UltimateTicTacToe
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.tictactoeclashofxo.database.InternetCheck
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.Task
import com.example.tictactoeclashofxo.databinding.ActivityModeBinding
import com.example.tictactoeclashofxo.gameLogic.Connect4TicTacToe
import com.example.tictactoeclashofxo.gameLogic.EatTicTacToe
import com.example.tictactoeclashofxo.gameLogic.InfiniteTicTacToe
import com.example.tictactoeclashofxo.gameLogic.SuperTicTacToe
import com.example.tictactoeclashofxo.newCode.GameActivity
import com.google.android.material.textfield.TextInputEditText

class ModeActivity : AppCompatActivity() {
    private lateinit var binding:ActivityModeBinding
    private var arena:Int=0
    private var background:Int=0
    private var mode:Int=0
    private var whoFirst:Int=0
    private var turn:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        arena=intent.getIntExtra("ARENA",0)
        background=intent.getIntExtra("BACKGROUND",0)
        mode=intent.getIntExtra("MODE",0)
        binding.arenaImage.setImageResource(arena)

        //---------------------back to home---------------------------------//
        binding.cancel.setOnClickListener {
            val intent=Intent(this, GameActivity::class.java)
            intent.putExtra("ARENA",arena)
            intent.putExtra("BACKGROUND",background)
            intent.putExtra("MODE",mode)
            startActivity(intent)
            finish()
        }
        //-----------------------about the game mode------------------------//
        val tempParent=FrameLayout(this)
        val ruleDialog = when (mode) {
            1 -> layoutInflater.inflate(R.layout.rule_1, tempParent, false)
            2 -> layoutInflater.inflate(R.layout.rule_2, tempParent, false)
            3 -> layoutInflater.inflate(R.layout.rule_3, tempParent, false)
            4 -> layoutInflater.inflate(R.layout.rule_1, tempParent, false)
            5 -> layoutInflater.inflate(R.layout.rule_2, tempParent, false)
            6 -> layoutInflater.inflate(R.layout.rule_3, tempParent, false)
            7 -> layoutInflater.inflate(R.layout.rule_3, tempParent, false)
            else -> null
        }
        val dialogRule=Dialog(this)
        dialogRule.setContentView(ruleDialog!!)
        binding.more.setOnClickListener {
            showDialog(dialogRule)
            val close=dialogRule.findViewById<ImageButton>(R.id.close)
            close.setOnClickListener {
                dialogRule.dismiss()
            }
        }
        //---------------------- this id for set the player name ------------//
        binding.addPlayerName.setOnClickListener {
            val dialog=Dialog(this)
            val customDialog=layoutInflater.inflate(R.layout.name_set_dialog,tempParent,false)
            dialog.setContentView(customDialog)
            Task.showDialogPerfect(dialog)
            val player1=dialog.findViewById<TextInputEditText>(R.id.player1Name)
            val player2=dialog.findViewById<TextInputEditText>(R.id.player2Name)
            val save=dialog.findViewById<AppCompatButton>(R.id.saveName)
            val close=dialog.findViewById<AppCompatButton>(R.id.close)
            val myPref=SessionManager(this)
            save.setOnClickListener {
                if(!player1.text?.trim().isNullOrEmpty() && !player2.text?.trim().isNullOrEmpty()){
                    myPref.setPlayerName(player1.text.toString(), player2.text.toString())
                    Toast.makeText(this,"Name Set Successful",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                else Toast.makeText(this,"Invalid Input",Toast.LENGTH_SHORT).show()
            }
            close.setOnClickListener { dialog.dismiss() }
        }
        //---------------------for online multiplayer-----------------------//
        binding.play.setOnClickListener {
            if(InternetCheck.checkInternet(this)) gotoGame(1)
            else Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show()
        }
        //---------------------for Local multiplayer-----------------------//
        binding.localPlay.setOnClickListener { gotoGame(2) }
        //---------------------with Bot-----------------------//
        binding.botPlay.setOnClickListener {gotoGame(3) }
        //--------------------To set who will play first --------//
        binding.whoFirstPlay.setOnClickListener {
            whoFirst++
            turn=whoFirst%3
            when(turn){
                0->{
                    // that means set the random sign in the button
                    Toast.makeText(this,"Random Turn Choose",Toast.LENGTH_SHORT).show()
                    binding.whoFirstPlay.setBackgroundResource(R.drawable.random_turn)
                }
                1->{
                    // that means set the bot sign in the button
                    Toast.makeText(this,"Bot Turn First",Toast.LENGTH_SHORT).show()
                    binding.whoFirstPlay.setBackgroundResource(R.drawable.bot_turn)
                }
                2->{
                    // that means set the user sign in the button
                    Toast.makeText(this,"User Turn First",Toast.LENGTH_SHORT).show()
                    binding.whoFirstPlay.setBackgroundResource(R.drawable.user_turn)
                }
            }
        }
    }
    private fun gotoGame(type:Int){
        when(mode){
            1->{
                val intent=Intent(this,SimpleTicTacToe::class.java)
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
                val intent=Intent(this,UltimateTicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()
            }
            5->{
                val intent=Intent(this,Connect4TicTacToe::class.java)
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
                val intent=Intent(this,Connect5TicTacToe::class.java)
                intent.putExtra("BACKGROUND",background)
                intent.putExtra("TYPE",type)
                intent.putExtra("TURN",turn)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun showDialog(dialog: Dialog){
        val layoutParam2= WindowManager.LayoutParams()
        layoutParam2.copyFrom(dialog.window?.attributes)
        layoutParam2.width= WindowManager.LayoutParams.MATCH_PARENT
        layoutParam2.height= WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes=layoutParam2
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}