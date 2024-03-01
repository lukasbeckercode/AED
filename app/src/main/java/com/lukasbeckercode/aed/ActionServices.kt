package com.lukasbeckercode.aed

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast

class ActionServices: Service() {

    private val delayTwoMinutes = 120000L
    private val delayFiveSeconds =  5000L

    private val runnerAnalyze = Runnable {
        val broadcastIntent = Intent(IntentNames.broadcastIntentName)
        broadcastIntent.putExtra(IntentNames.broadcastStateIntentName,State.ANALYSIS.name)
        sendBroadcast(broadcastIntent)
    }


    private val runnerAnalysisShock = Runnable {
        val broadcastIntent = Intent(IntentNames.broadcastIntentName)
        broadcastIntent.putExtra(IntentNames.broadcastStateIntentName,State.DELIVER_SHOCK.name)
        sendBroadcast(broadcastIntent)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = createNotification()
        startForeground(33,notification)

        val stateStr = intent!!.getStringExtra(IntentNames.stateIntentName)
        if(stateStr != null){
            val state = State.valueOf(stateStr)
            val handler = Handler(Looper.getMainLooper())

            when(state){
                State.SHOCK_DELIVERED -> handler.postDelayed(runnerAnalyze,delayTwoMinutes)
                State.ANALYSIS-> handler.postDelayed(runnerAnalysisShock,delayFiveSeconds)
                else ->  Toast.makeText(this,"Internal Error: Invalid State!",Toast.LENGTH_SHORT).show()
            }

        }else{
            Toast.makeText(this,"Internal Error: State not found!",Toast.LENGTH_SHORT).show()
        }

        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {

        val serviceChannel = NotificationChannel(
            IntentNames.channelID,
            "CPR Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for notifications"
            setSound(null,null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, IntentNames.channelID)
            .setContentTitle("CPR Timer")
            .setContentText("Timer gestartet!")
            .setContentIntent(pendingIntent)
            .setSound(null)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

}