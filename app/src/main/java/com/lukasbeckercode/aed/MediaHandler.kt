package com.lukasbeckercode.aed

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class MediaHandler {
    private lateinit var player :MediaPlayer

    fun play(context:Context,state: State){

        val filename = when(state){
            State.ANALYSIS -> R.raw.analysis
            State.DELIVER_SHOCK -> R.raw.shock
            State.CONTINUE_CPR -> R.raw.continue_cpr
            else -> 9999
        }

        if(filename == 9999){
            Log.e("MediaHandler","Invalid State for Audio: " + state.name)
            return
        }

        player = MediaPlayer.create(context,filename)
        player.start()
    }

    fun destroy(){
        player.release()
    }
}