package com.example.tictactoeclashofxo.newCode

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tictactoeclashofxo.R
import com.example.tictactoeclashofxo.database.CartDB
import com.example.tictactoeclashofxo.database.CartData
import com.example.tictactoeclashofxo.database.Convert
import com.example.tictactoeclashofxo.database.SessionManager
import com.example.tictactoeclashofxo.databinding.ActivityShopBinding
import com.example.tictactoeclashofxo.gameLogic.SimpleTicTacToe

class MyAdapter(private val select:Int,private val context: Context,private val dataList:ArrayList<CartData>,private val txtView:TextView):RecyclerView.Adapter<MyAdapter.MyHolder>(){
    private val myPref=SessionManager(context)
    private val db=CartDB(context)
    inner class MyHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val img:ImageView=itemView.findViewById(R.id.itemImage)
        val txt:TextView=itemView.findViewById(R.id.itemText)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyHolder {
        val inflater=LayoutInflater.from(context)
        val customView=inflater.inflate(R.layout.custom_shopitem,parent,false)
        return MyHolder(customView)
    }
    override fun onBindViewHolder(holder: MyAdapter.MyHolder, position: Int) {
        //==== select 0 means it's the cart item and select 1 means it's a inventory item =====//
        holder.img.setImageResource(dataList[position].resId)
        if(select==1) {
            if(dataList[position].resId==myPref.getCross() || dataList[position].resId==myPref.getCircle()) {
                holder.txt.text="Set"
                holder.txt.visibility=View.INVISIBLE
            }
            else holder.txt.apply {
                holder.txt.text="Set"
                visibility=View.VISIBLE
            }
        }
        else{
            holder.txt.visibility=View.VISIBLE
            holder.txt.text=dataList[position].price.toString()
        }
        holder.txt.setOnClickListener {
            if(select==0){
                if(myPref.getCoin()>=dataList[position].price){
                    myPref.updateCoin(-dataList[position].price.toLong())
                    txtView.text= Convert.number(myPref.getCoin())
                    db.buyItem(dataList[position].id)
                    Toast.makeText(context,"Item Unlocked",Toast.LENGTH_SHORT).show()
                    dataList.removeAt(position)
                    notifyItemRemoved(position)
                }
                else Toast.makeText(context,"Not Enough Coin to Unlocked",Toast.LENGTH_SHORT).show()
                if(dataList.isNotEmpty()) {
                    for (i in dataList.indices) {
                        if (dataList[i].resId == myPref.getCross() || dataList[i].resId == myPref.getCircle()) {
                            holder.txt.text = "Set"
                            holder.txt.visibility = View.INVISIBLE
                        }
                    }
                }
            }
            else{
                val changed= mutableListOf<Int>()
                holder.txt.visibility=View.INVISIBLE
                if(dataList[position].id>="A" && dataList[position].id<="Z"){
                    myPref.setCross(dataList[position].resId)
                }
                else if(dataList[position].id>="a" && dataList[position].id<="z"){
                    myPref.setCircle(dataList[position].resId)
                }
                if (dataList.isNotEmpty()) {
                    for (i in dataList.indices) {
                        if (dataList[i].resId == myPref.getCross() || dataList[i].resId == myPref.getCircle()) {
                            holder.txt.text = "Set"
                            holder.txt.visibility = View.INVISIBLE
                        } else holder.txt.text = dataList[i].price.toString()
                        changed.add(i)
                    }
                }
                changed.forEach{(notifyItemChanged(it))}
                notifyItemChanged(position)
            }
        }
    }
    override fun getItemCount(): Int {
        return dataList.size
    }
}

class ShopActivity : AppCompatActivity() {

    private lateinit var binding:ActivityShopBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db= CartDB(this)
        val myCoin=binding.coin
        binding.coin.text= Convert.number(SessionManager(this).getCoin())

        var inventoryDataList=db.getAllData(0)
        inventoryDataList.shuffle()
        var cartDataList=db.getAllData(1)
        binding.dataList.apply {
            adapter=MyAdapter(1,context,inventoryDataList,myCoin)
            layoutManager=GridLayoutManager(applicationContext,2)
        }
        binding.cart.setOnClickListener {
            binding.cart.setBackgroundResource(R.drawable.blue)
            binding.inventory.background=null
            cartDataList=db.getAllData(1)
            cartDataList.shuffle()
            binding.dataList.apply {
                adapter=MyAdapter(0,context,cartDataList,myCoin)
                layoutManager=GridLayoutManager(applicationContext,2)
            }
        }
        //------------- set the inventory item in the list view ----//
        binding.inventory.setOnClickListener {
            binding.cart.background=null
            binding.inventory.setBackgroundResource(R.drawable.blue)
            inventoryDataList=db.getAllData(0)
            inventoryDataList.shuffle()
            binding.dataList.apply {
                adapter=MyAdapter(1,context,inventoryDataList,myCoin)
                layoutManager=GridLayoutManager(applicationContext,2)
            }
        }
        binding.backToBattle.setOnClickListener {
            val intent= Intent(this,BattleActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
