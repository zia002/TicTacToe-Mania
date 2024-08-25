package com.example.tictactoeclashofxo
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import com.example.tictactoeclashofxo.database.AdLoad
import com.example.tictactoeclashofxo.database.Arena
import com.example.tictactoeclashofxo.database.ArenaDB
import com.example.tictactoeclashofxo.database.CartData
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.database.CartDB
import com.example.tictactoeclashofxo.newCode.BattleActivity
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_activity)
        //======== first we need to load or start the loading the Ad =======//
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(this@StartActivity) {}
        }
        //======= then load the data and other info and go to next activity ======//
        val myPref=SessionManager(this)
        val db=CartDB(this)
        if(!myPref.isDataLoad()){
           //----- set the initial value of trophy and coin ------//
            myPref.updateCoin(10000000)
            //==========  here we initialize the shop items ==========//
            myPref.setCircle(R.drawable.circle)
            myPref.setCross(R.drawable.cross)
            myPref.setDP(R.drawable.person)
            myPref.setPlayerName("Player-1","Player-2")
            myPref.setName("User")
            myPref.setIndex(0)
            //-----here we will set the item in the database of cart -------//
            //----- set the profile pic in DB control id is 1-------//
            //----- set circle item  control id is 2-------//
            db.insertCartData(CartData("b",2000,R.drawable.circle))
            db.insertCartData(CartData("c",2000,R.drawable.circle_1))
            db.insertCartData(CartData("d",2000,R.drawable.circle_2))
            db.insertCartData(CartData("e",2000,R.drawable.circle_3))
            db.insertCartData(CartData("f",2000,R.drawable.circle_4))
            db.insertCartData(CartData("g",2000,R.drawable.circle_5))
            db.insertCartData(CartData("h",2000,R.drawable.circle_6))
            db.insertCartData(CartData("i",2000,R.drawable.circle_7))
            //----- set cross item control id 3 --------//
            db.insertCartData(CartData("B",4000,R.drawable.cross))
            db.insertCartData(CartData("C",4000,R.drawable.cross_1))
            db.insertCartData(CartData("D",4000,R.drawable.cross_2))
            db.insertCartData(CartData("E",4000,R.drawable.cross_3))
            db.insertCartData(CartData("F",4000,R.drawable.cross_4))
            db.insertCartData(CartData("G",4000,R.drawable.cross_5))
            db.insertCartData(CartData("H",4000,R.drawable.cross_6))
            db.insertCartData(CartData("I",4000,R.drawable.cross_7))
            db.buyItem("B")
            db.buyItem("b")
            myPref.setDataLoad()
            myPref.soundSystem("1")
            //========= here we insert the arena and background image in the database ======//
            val arenaDb=ArenaDB(this)
            val dataList=ArrayList<Arena>()
            dataList.add(Arena(1,R.drawable.forest,R.drawable.forest_svg,1,1))
            dataList.add(Arena(2,R.drawable.desert,R.drawable.desert_svg,25000,0))
            dataList.add(Arena(4,R.drawable.pinecrest,R.drawable.pinecrest_svg,50000,0))
            dataList.add(Arena(3,R.drawable.toriigrove,R.drawable.torigrove_svg,100000,0))
            dataList.add(Arena(8,R.drawable.pirateship,R.drawable.pirateship_svg,150000,0))
            dataList.add(Arena(7,R.drawable.graveyard,R.drawable.graveyard_svg,180000,0))
            dataList.add(Arena(5,R.drawable.rockyglade,R.drawable.rockyglade_svg,200000,0))
            dataList.add(Arena(6,R.drawable.iceland,R.drawable.iceland_svg,300000,0))
            arenaDb.loadData(dataList)
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, BattleActivity::class.java) )
                finish()
            },4000)
        }
        else {
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, BattleActivity::class.java))
                finish()
            }, 3000)
        }
    }
}