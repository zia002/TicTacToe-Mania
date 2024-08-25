package com.example.tictactoeclashofxo.database

import android.content.Context

class SessionManager(val context: Context) {
    private var fileName="AppPreference"
    private var isDataLoad="IS_DATA_LOAD"
    private var username="KEY_USER_NAME"
    private var coinNo="KEY_COIN"
    private var curCircle="KEY_CIRCLE"
    private var curCross="KEY_CROSS"
    private var curDP="KEY_DP"
    private var sound="SOUND"
    private var first="FIRST"
    private var player1="PLAYER1"
    private var player2="PLAYER2"
    private var index="INDEX"
    private var lastAd="LAST_AD"
    private var nextAd="NEXT_AD"
    private var nextTime="NEXT_TIME"
    private var gameCount="GAME_COUNT"
    private var clickCount="CLICK_COUNT"
    private var sharedPreference=context.getSharedPreferences(fileName,0)
    private var editor=sharedPreference.edit()
    fun isDataLoad(): Boolean {
       return sharedPreference.getBoolean(isDataLoad,false)
    }
    fun setDataLoad(){
        editor.putBoolean(isDataLoad,true)
        editor.commit()
    }
    fun updateCoin(value: Long){
        var currentCoin=getCoin()
        currentCoin = currentCoin.plus(value)
        editor.putLong(coinNo,currentCoin)
        editor.commit()
    }
    fun setName(s: String){
        editor.putString(username, s)
        editor.commit()
    }
    fun getName(): String? {
        return sharedPreference.getString(username,"")
    }
    fun getDetails(key:String): String? {
        return sharedPreference.getString(key,"")
    }
    fun setCross(refId:Int){
        editor.putInt(curCross,refId)
        editor.commit()
    }
    fun getCross():Int {
        return sharedPreference.getInt(curCross,0)
    }
    fun setCircle(refId:Int){
        editor.putInt(curCircle,refId)
        editor.commit()
    }
    fun getCircle(): Int {
        return sharedPreference.getInt(curCircle,0)
    }
    fun setDP(refId:Int){
        editor.putInt(curDP,refId)
        editor.commit()
    }
    fun getDP():Int {
        return sharedPreference.getInt(curDP,0)
    }
    fun getCoin(): Long {
        return sharedPreference.getLong(coinNo,0)
    }
    fun soundSystem(ok:String){
        editor.putString(sound,ok)
        editor.commit()
    }
    fun setFirstTime(){
        editor.putString(first,"1")
        editor.commit()
    }
    fun setPlayerName(p1:String,p2:String){
        editor.putString(player1,p1)
        editor.commit()
        editor.putString(player2,p2)
        editor.commit()
    }
    fun setIndex(t:Int){
        editor.putInt(index,t)
        editor.commit()
    }
    fun getIndex():Int{
       return sharedPreference.getInt(index,0)
    }
    fun setAdTime(key:String,value:Long){
        editor.putLong(key,value)
        editor.commit()
    }
    fun nextAd():Long{
        return sharedPreference.getLong(nextAd,0)
    }
    fun setTime(value:String){
        editor.putString("NEXT_TIME",value)
        editor.commit()
    }

}