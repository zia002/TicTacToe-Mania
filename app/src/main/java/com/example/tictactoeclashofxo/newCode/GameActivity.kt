package com.example.tictactoeclashofxo.newCode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoeclashofxo.ModeActivity
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.databinding.ActivityGameBinding

data class ModeData(val modeName:String, val modeImage:Int)
class ModeAdapter(private val context: Context, private val dataList:ArrayList<ModeData>):BaseAdapter(){
    override fun getCount(): Int { return dataList.size}
    override fun getItem(position: Int): Any {
       return dataList[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, ConvertView: View?, parent: ViewGroup?): View? {
        var convertView=ConvertView
        if (convertView==null) {
            val inflater = LayoutInflater.from(context)
            convertView=inflater.inflate(R.layout.custom_game_mode,parent,false)
        }
        val modeImg=convertView?.findViewById<ImageView>(R.id.customModeImage)
        val modeName=convertView?.findViewById<TextView>(R.id.customModeName)
        modeImg?.setImageResource(dataList[position].modeImage)
        modeName?.text=dataList[position].modeName
        modeImg?.setOnClickListener {
        }
        modeName?.setOnClickListener {

        }
        return convertView
    }
}
class GameActivity : AppCompatActivity() {
    private lateinit var binding:ActivityGameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val arena=intent.getIntExtra("ARENA",0)
        val background=intent.getIntExtra("BACKGROUND",0)
        val intent=Intent(this, ModeActivity::class.java)
        intent.putExtra("ARENA",arena)
        intent.putExtra("BACKGROUND",background)
        binding.mode1.setOnClickListener {
            intent.putExtra("MODE",1)
            startActivity(intent)
            finish()
        }
        binding.mode2.setOnClickListener {
            intent.putExtra("MODE",2)
            startActivity(intent)
            finish()
        }
        binding.mode3.setOnClickListener {
            intent.putExtra("MODE",3)
            startActivity(intent)
            finish()
        }
        binding.mode4.setOnClickListener {
            intent.putExtra("MODE",4)
            startActivity(intent)
            finish()
        }
        binding.mode5.setOnClickListener {
            intent.putExtra("MODE",5)
            startActivity(intent)
            finish()
        }
        binding.mode6.setOnClickListener {
            intent.putExtra("MODE",6)
            startActivity(intent)
            finish()
        }
        binding.mode7.setOnClickListener {
            intent.putExtra("MODE",7)
            startActivity(intent)
            finish()
        }
        binding.backToArena.setOnClickListener {
            startActivity(Intent(this,BattleActivity::class.java))
            finish()
        }
    }
}