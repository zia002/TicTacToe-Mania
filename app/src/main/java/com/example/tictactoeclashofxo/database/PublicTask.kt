package com.example.tictactoeclashofxo.database

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.WindowManager
import com.example.tictactoeclashofxo.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlin.random.Random
import kotlin.random.nextInt

object InternetCheck {
    fun checkInternet(context: Context): Boolean {
        val connectivityManager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities=connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return if(networkCapabilities!=null){
            if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) true
            else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) true
            else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) true
            else false
        } else false
    }
}
object Convert{
    fun number(num:Long):String{
        var ans=num.toString()
        if(num/10000000>=1){
            val tmp=(num/10000000.0).toFloat()
            ans=tmp.toString()
            ans+="M"
        }
        else if(num/1000>=1){
            val tmp=(num/1000.0).toFloat()
            ans=tmp.toString()
            ans+="K"
        }
        return ans
    }
}
object Task{
    fun showDialogPerfect(dialog: Dialog){
        val layoutParam2= WindowManager.LayoutParams()
        layoutParam2.copyFrom(dialog.window?.attributes)
        layoutParam2.width= WindowManager.LayoutParams.MATCH_PARENT
        layoutParam2.height= WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes=layoutParam2
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
    fun getRandomCircle():Int{
        val circleArray= listOf(R.drawable.circle,R.drawable.circle_1,R.drawable.circle_2,R.drawable.circle_3,R.drawable.circle_4,R.drawable.circle_5,R.drawable.circle_6,R.drawable.circle_7)
        val n= Random.nextInt(circleArray.indices)
        return circleArray[n]
    }
    fun getRandomCross():Int{
        val crossArray= listOf(R.drawable.cross,R.drawable.cross_1,R.drawable.cross_2,R.drawable.cross_3,R.drawable.cross_4,R.drawable.cross_5,R.drawable.cross_6,R.drawable.circle_7)
        val n= Random.nextInt(crossArray.indices)
        return crossArray[n]
    }
    fun getRandomTime():Long{
        val timeList= listOf(500,1000,1500)
        val n=Random.nextInt(0..2)
        return timeList[n].toLong()
    }
}
object AdLoad{
    /*
       *implementing the interstitial ad
       *we need to add frequency capping from admob account, so that the winner activity will not show too much ad
       *
    */
    var mInterstitialAd: InterstitialAd? = null
    fun loadInterstitial(context: Context){
        val adRequest = AdRequest.Builder().build()
        val id=context.getString(R.string.ad_id_interstitial)
        InterstitialAd.load(context,id, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                //Log.d(TAG, 'Ad was loaded.')
                mInterstitialAd = interstitialAd
                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        //Log.d(TAG, "Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        //Log.d(TAG, "Ad dismissed fullscreen content.")
                        mInterstitialAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        // Called when ad fails to show.
                        //Log.e(TAG, "Ad failed to show fullscreen content.")
                        mInterstitialAd = null
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        //Log.d(TAG, "Ad showed fullscreen content.")
                    }
                }
            }
        })
    }
    var rewardedAd:RewardedAd?=null
    fun loadRewardAd(context: Context){
        val adRequest = AdRequest.Builder().build()
        val id=context.getString(R.string.ad_id_reward)
        RewardedAd.load(context,id, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        //Log.d(TAG, "Ad was clicked.")
                    }
                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        //Log.d(TAG, "Ad dismissed fullscreen content.")
                        rewardedAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        // Called when ad fails to show.
                        //Log.e(TAG, "Ad failed to show fullscreen content.")
                        rewardedAd = null
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        //Log.d(TAG, "Ad recorded an impression.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        //Log.d(TAG, "Ad showed fullscreen content.")
                    }
                }
            }

        })
    }
}