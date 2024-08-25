package com.example.tictactoeclashofxo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//------------ the data object for each item of the shop cart and inventory -----------//
data class CartData(var id:String, var price: Int, var resId:Int)
class CartDB(context:Context):SQLiteOpenHelper(context, DB_NAME,null,1) {
    companion object{
        const val DB_NAME="cartDatabase"
        const val TABLE_NAME="cartTableName"
        const val COL_ID="imageID"
        const val COL_PRICE="price"
        const val IMAGE_ID="imageRes"
        const val BUY="buyItem"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val query="CREATE TABLE $TABLE_NAME ($COL_ID TEXT UNIQUE,$COL_PRICE INTEGER,$IMAGE_ID INTEGER,$BUY INTEGER)"
        db?.execSQL(query)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropQuery="DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropQuery)
        onCreate(db)
    }
    fun insertCartData(data: CartData){
        val db = this.writableDatabase
        val values =ContentValues().apply {
            put(COL_ID, data.id)
            put(COL_PRICE, data.price)
            put(IMAGE_ID, data.resId)
            put(BUY,0)
        }
        db.insert(TABLE_NAME, null, values)
    }


    fun getAllData(cart: Int): ArrayList<CartData> {
        val dataList = ArrayList<CartData>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID))
            val price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRICE))
            val refId = cursor.getInt(cursor.getColumnIndexOrThrow(IMAGE_ID))
            val buy=cursor.getInt(cursor.getColumnIndexOrThrow(BUY))
            val tmpData = CartData(id, price, refId)
            if(cart==1 && buy==0)  dataList.add(tmpData)
            else if(cart==0 && buy==1) dataList.add(tmpData)
        }
        cursor.close()
        return dataList
    }
    fun makeAllCart(){
        val db=this.writableDatabase
        val query="UPDATE $TABLE_NAME SET $BUY=0 "
        db.execSQL(query)
    }
    //----- when user buy an item then it will be executed it will mark the item as buy -----//
    fun buyItem(item:String){
        val db=this.writableDatabase
        val query="UPDATE $TABLE_NAME SET $BUY=1 WHERE $COL_ID='$item' "
        db.execSQL(query)
        db.close()
    }
}
//---------- this is for rank database --------------//
//======== this is the db class which contain the arena details =========//
data class Arena(var id:Int,var arena:Int,var background:Int,var price:Int,var buy:Int)
class ArenaDB(context: Context):SQLiteOpenHelper(context, dbName,null,1){
    companion object{
        const val dbName="ARENA_DATABASE"
        const val tableName="ARENA_NAME"
        const val colId="COL_ID"
        const val colArena="COL_ARENA"
        const val colBackground="COL_BACKGROUND"
        const val colPrice="COL_PRICE"
        const val colBuy="COL_BUY"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val query="CREATE TABLE $tableName ($colId INTEGER UNIQUE, $colArena INTEGER,$colBackground INTEGER,$colPrice INTEGER,$colBuy INTEGER)"
        db?.execSQL(query)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropQuery="DROP TABLE IF EXISTS $tableName"
        db?.execSQL(dropQuery)
        onCreate(db)
    }
    fun loadData(dataList:ArrayList<Arena>){
        val db=writableDatabase
        val n=dataList.size-1
        for (i in 0..n){
            val values=ContentValues().apply {
                put(colId,dataList[i].id)
                put(colArena,dataList[i].arena)
                put(colBackground,dataList[i].background)
                put(colPrice,dataList[i].price)
                put(colBuy,dataList[i].buy)
            }
            db.insert(tableName,null,values)
        }
        db.close()
    }
    fun unlockedArena(id:Int){
        val db=writableDatabase
        val updateQuery="UPDATE $tableName SET $colBuy=1 WHERE $colId=$id"
        db.execSQL(updateQuery)
        db.close()
    }
    fun getArenaData():ArrayList<Arena>{
        val db=readableDatabase
        val dataList=ArrayList<Arena>()
        val selectQuery="SELECT * FROM $tableName "
        val cursor=db.rawQuery(selectQuery,null)
        while(cursor.moveToNext()){
            val id=cursor.getInt(cursor.getColumnIndexOrThrow(colId))
            val arena=cursor.getInt(cursor.getColumnIndexOrThrow(colArena))
            val background=cursor.getInt(cursor.getColumnIndexOrThrow(colBackground))
            val price=cursor.getInt(cursor.getColumnIndexOrThrow(colPrice))
            val buy=cursor.getInt(cursor.getColumnIndexOrThrow(colBuy))
            dataList.add(Arena(id,arena,background,price,buy))
        }
        cursor.close()
        return dataList
    }
}
