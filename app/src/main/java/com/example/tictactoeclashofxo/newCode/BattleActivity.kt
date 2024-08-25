package com.example.tictactoeclashofxo.newCode

import android.app.Dialog
import android.content.Intent
import android.text.format.DateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

import com.airbnb.lottie.LottieAnimationView
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.database.AdLoad
import com.example.tictactoeclashofxo.database.Arena
import com.example.tictactoeclashofxo.database.ArenaDB
import com.example.tictactoeclashofxo.database.Convert
import com.example.tictactoeclashofxo.database.InternetCheck
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.Task
import com.example.tictactoeclashofxo.databinding.ActivityBattleBinding
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.material.textfield.TextInputEditText
import java.util.Date
import kotlin.random.Random
import kotlin.random.nextInt

class BattleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBattleBinding
    private lateinit var myPref: SessionManager
    private var index:Int=0
    private var curTime:Long=0
    private var nextReward:Long=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //-------- get and set data from shared preference ---------//
        myPref= SessionManager(this)
        binding.UserName.text=myPref.getDetails("KEY_USER_NAME")
        binding.personImage.setImageResource(myPref.getDP())
        binding.goldCount.text=Convert.number(myPref.getCoin())
        //======= here we control the reward ad and next reward =======//
        curTime=System.currentTimeMillis()
        nextReward=myPref.nextAd()
        val tempParent = FrameLayout(this)
        if(curTime>=nextReward){
            AdLoad.loadRewardAd(this)
            binding.adIcon.visibility=View.VISIBLE
            binding.rewardTxt.text=getString(R.string.ad)
            binding.later.visibility=View.VISIBLE
            binding.accept.visibility=View.VISIBLE
        }
        else{
            val next=myPref.getDetails("NEXT_TIME")
            binding.adIcon.visibility=View.GONE
            binding.rewardTxt.text=getString(R.string.adTxt,next)
            binding.rewardBtn.visibility=View.GONE
        }
        //======= when user click on the later button =========//
        binding.later.setOnClickListener {
            val extraTime=30000
            myPref.setAdTime("LAST_AD",System.currentTimeMillis())
            val nextTime=System.currentTimeMillis()+extraTime
            myPref.setAdTime("NEXT_AD",nextTime)
            val dateFormat = DateFormat.getTimeFormat(this)
            val formattedTime = dateFormat.format(Date(nextTime))
            myPref.setTime(formattedTime)
            binding.adIcon.visibility=View.GONE
            binding.accept.visibility=View.GONE
            binding.later.visibility=View.GONE
            binding.rewardTxt.text=getString(R.string.adTxt,formattedTime)

        }
        val rewardDialog=layoutInflater.inflate(R.layout.reward,tempParent,false)
        val dialog2=Dialog(this)
        dialog2.setContentView(rewardDialog)
        binding.accept.setOnClickListener {
            //====== here we need to implement the reward ad claim process and everything =======//
            if(AdLoad.rewardedAd!=null){
                AdLoad.rewardedAd?.let { ad ->
                    ad.show(this) {
                        Task.showDialogPerfect(dialog2)
                        val rewardCoin= Random.nextInt(500..1000)
                        val closeDialogReward=dialog2.findViewById<AppCompatButton>(R.id.closeReward)
                        val myReward=dialog2.findViewById<TextView>(R.id.rewardCoin)
                        val animation=dialog2.findViewById<LottieAnimationView>(R.id.chestOpenLottie)
                        Toast.makeText(this,"You Have Earn $rewardCoin Reward Coin",Toast.LENGTH_SHORT).show()
                        myReward.text=rewardCoin.toString()
                        animation.playAnimation()
                        closeDialogReward.setOnClickListener { dialog2.dismiss() }
                        myPref.updateCoin(rewardCoin.toLong())
                        binding.goldCount.text=Convert.number(myPref.getCoin())
                        //==== here we need to apply the update status about the reward ====//
                        val extraTime=30000
                        myPref.setAdTime("LAST_AD",System.currentTimeMillis())
                        val nextTime=System.currentTimeMillis()+extraTime
                        myPref.setAdTime("NEXT_AD",nextTime)
                        val dateFormat = DateFormat.getTimeFormat(this)
                        val formattedTime = dateFormat.format(Date(nextTime))
                        myPref.setTime(formattedTime)
                        binding.adIcon.visibility=View.GONE
                        binding.accept.visibility=View.GONE
                        binding.later.visibility=View.GONE
                        binding.rewardTxt.text=getString(R.string.adTxt,formattedTime)
                    }
                } ?: run {
                    Toast.makeText(this,"The rewarded ad wasn't ready yet.Check Internet Connection",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"The rewarded ad wasn't ready yet.Check Internet Connection",Toast.LENGTH_SHORT).show()
            }
        }
        //-- here we get the data value --//
        val db = ArenaDB(this)
        val dataList = db.getArenaData()
        //-- here we work o the arena control --//
        val imageSwitcher= ImageSwitcher(this)
        imageSwitcher.setFactory{
            val imageview= ImageView(this)
            imageview
        }
        binding.modeViewer.addView(imageSwitcher)
        index=myPref.getIndex()
        //------- here initially we set the first arena free ------//
        imageSwitcher.setImageResource(dataList[index].arena )
        controlArenaView(dataList)
        binding.nextBtn.setOnClickListener {
            index = if(index+1<dataList.size) index+1
            else 0
            controlArenaView(dataList)
            imageSwitcher.setImageResource(dataList[index].arena)
            myPref.setIndex(index)
        }
        binding.previousBtn.setOnClickListener {
            index = if(index-1>=0) index-1
            else dataList.size -1
            controlArenaView(dataList)
            imageSwitcher.setImageResource(dataList[index].arena)
            myPref.setIndex(index)
        }
        //-------------------------------Starting new fragment where sent the battle type---------------------//
        binding.battleBtn.setOnClickListener{
            if(dataList[index].buy==1) {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("ARENA", dataList[index].arena)
                intent.putExtra("BACKGROUND",dataList[index].background)
                startActivity(intent)
            }
            else{
                if(myPref.getCoin()>=dataList[index].price){
                    myPref.updateCoin(-dataList[index].price.toLong())
                    binding.goldCount.text=Convert.number(myPref.getCoin())
                    db.unlockedArena(dataList[index].id)
                    binding.lock.visibility=View.INVISIBLE
                    binding.battleBtn.text=getString(R.string.battle)
                    binding.battleBtn.setBackgroundResource(R.drawable.battlebtn)
                    binding.battleBtn.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                    dataList[index].buy=1
                    Toast.makeText(this,"Arena Unlocked",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this,"Not Enough Coin to Unlock Arena",Toast.LENGTH_SHORT).show()
                }
            }
        }
        //-----------------------------For setting----------------------------------//
        binding.setting.setOnClickListener {
            val settingDialog = layoutInflater.inflate(R.layout.setting_dialog, tempParent,false)
            val dialog = Dialog(this)
            dialog.setContentView(settingDialog)
            //---finding those button id-----//
            val cancelSetting = dialog.findViewById<ImageButton>(R.id.cancelSetting)
            val sound = dialog.findViewById<AppCompatButton>(R.id.sound)
            val privacyPolicy = dialog.findViewById<AppCompatButton>(R.id.privacyPolicy)
            val rateUs = dialog.findViewById<AppCompatButton>(R.id.rateUs)
            val rewardRule=dialog.findViewById<AppCompatButton>(R.id.rewardRule)
            val changeName = dialog.findViewById<AppCompatButton>(R.id.changeName)
            Task.showDialogPerfect(dialog)
            cancelSetting.setOnClickListener { dialog.dismiss() }
            //------- here we control the sound system -------//
            if (myPref.getDetails("SOUND") == "1") {
                sound.text=getString(R.string.on)
                sound.setBackgroundResource(R.drawable.green)
            } else {
                sound.setBackgroundResource(R.drawable.red)
                sound.text =getString(R.string.off)
            }
            sound.setOnClickListener {
                if (myPref.getDetails("SOUND") == "1") {
                    sound.setBackgroundResource(R.drawable.red)
                    sound.text =getString(R.string.off)
                    myPref.soundSystem("0")
                } else {
                    sound.setBackgroundResource(R.drawable.green)
                    sound.text = getString(R.string.on)
                    myPref.soundSystem("1")
                }
            }
            //------- here we control the change name task -----//
            changeName.setOnClickListener {
                val confirmDialog = layoutInflater.inflate(R.layout.change_name, tempParent,false)
                val dialog4 = Dialog(this)
                dialog4.setContentView(confirmDialog)
                val name = dialog4.findViewById<TextInputEditText>(R.id.newName)
                val confirm = dialog4.findViewById<AppCompatButton>(R.id.confirmNameChange)
                val closeBtn = dialog4.findViewById<AppCompatButton>(R.id.cancelConfirm)
                val notice=dialog4.findViewById<TextView>(R.id.notice)
                Task.showDialogPerfect(dialog4)
                if (myPref.getDetails("FIRST")=="0") notice.visibility=View.INVISIBLE
                confirm.setOnClickListener {
                    val newName = name.text
                    if (newName?.trim() != null && newName.isNotBlank()) {
                        if(myPref.getDetails("FIRST")=="0"){
                            myPref.setName(newName.toString())
                            binding.UserName.text = myPref.getName()
                            dialog4.dismiss()
                            Toast.makeText(this, "Name Changed Successful", Toast.LENGTH_LONG).show()
                            myPref.setFirstTime()
                        }
                        else if (myPref.getCoin() >= 100) {
                                myPref.setName(newName.toString())
                                myPref.updateCoin(-100)
                                binding.goldCount.text = Convert.number(myPref.getCoin())
                                binding.UserName.text = myPref.getName()
                                dialog4.dismiss()
                                Toast.makeText(this, "Name Changed Successful", Toast.LENGTH_LONG).show()
                        }
                        else Toast.makeText(this, "No Enough Coin", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show()
                }
                closeBtn.setOnClickListener { dialog4.dismiss() }
            }
        }
        //-- this is for shop and inventory activity ---//
        binding.gotoShop.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    //-------------- here we control the reward collecting -----------//
    private fun controlArenaView(dataList:ArrayList<Arena>){
        if(dataList[index].buy==0){
            binding.lock.visibility=View.VISIBLE
            binding.battleBtn.visibility=View.VISIBLE
            binding.battleBtn.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.coin,0)
            binding.battleBtn.text=Convert.number(dataList[index].price.toLong())

        }
        else{
            binding.lock.visibility=View.INVISIBLE
            binding.battleBtn.setBackgroundResource(R.drawable.battlebtn)
            binding.battleBtn.text=getString(R.string.battle)
            binding.battleBtn.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
        }
    }
    private fun checkRewardArrive(){
        if(curTime>=nextReward){
            binding.adIcon.visibility=View.VISIBLE
            binding.rewardTxt.text=getString(R.string.ad)
            binding.later.visibility=View.VISIBLE
            binding.accept.visibility=View.VISIBLE
        }
    }
}