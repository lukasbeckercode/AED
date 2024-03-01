package com.lukasbeckercode.aed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var state:State = State.TURNED_ON
    private lateinit var commandLabel:TextView
    private val intentFilter = IntentFilter(IntentNames.broadcastIntentName)
    private val player = MediaHandler()

    private  var broadcastReceiver:BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val stateStr = p1!!.getStringExtra(IntentNames.broadcastStateIntentName)
            if(stateStr != null) {
                state = State.valueOf(stateStr)
                if (state == State.ANALYSIS){
                    analyze()
                }else{
                    getNewInstruction(state)
                }
            } else{
                Toast.makeText(this@MainActivity,"Internal Error: Broadcast State invalid!",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        commandLabel = findViewById(R.id.commandLabel)
        val plugButton = findViewById<Button>(R.id.plugButton)
        val shockButton = findViewById<ImageButton>(R.id.deliverShockButton)

        commandLabel.text = getString(R.string.place_electrodes)

        registerReceiver(broadcastReceiver,intentFilter)

        plugButton.setOnClickListener {
            state = State.ANALYSIS
            analyze()
        }

        shockButton.setOnClickListener {
            if(state == State.DELIVER_SHOCK){
                state = State.SHOCK_DELIVERED
                val actionIntent = Intent(this,ActionServices::class.java)
                actionIntent.putExtra(IntentNames.stateIntentName, state.name)
                ContextCompat.startForegroundService(this,actionIntent)
                state = State.CONTINUE_CPR
                getNewInstruction(state)

            } else {
                Toast.makeText(this,"Gerät ist nicht bereit für Schock",Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getNewInstruction(currentState:State){

        commandLabel.text = when(currentState){
            State.ANALYSIS -> "Analyse! Patient nicht berühren!"
            State.DELIVER_SHOCK -> "Schock Empfohlen! Drücken Sie die gelbe Taste"
            State.CONTINUE_CPR -> "CPR fortsetzen!"
            else -> "Invalid State!"
        }

        player.play(this,currentState)

    }

    fun analyze(){
       if(state != State.ANALYSIS){
           Toast.makeText(this,"Gerät is nicht bereit für Analyse!",Toast.LENGTH_SHORT).show()
           return
       }
        getNewInstruction(state)
        val actionIntent = Intent(this,ActionServices::class.java)
        actionIntent.putExtra(IntentNames.stateIntentName, state.name)
        ContextCompat.startForegroundService(this,actionIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        player.destroy()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
        val stopIntent = Intent(this,ActionServices::class.java)
        stopService(stopIntent)
    }
}