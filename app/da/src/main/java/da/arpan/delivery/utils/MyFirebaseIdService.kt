package da.arpan.delivery.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import da.arpan.delivery.R
import da.arpan.delivery.ui.home.HomeActivity
import java.util.*


class MyFirebaseIdService : FirebaseMessagingService() {

    override fun onNewToken(s: String) {
        super.onNewToken(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("msg", "onMessageReceived: " + remoteMessage.data.toString())
        val sound: Uri = when(getSharedPreferences("NOTIFICATION_SOUND", MODE_PRIVATE).getInt("SOUND_ADD", 0)){
            0 -> Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://da.arpan.delivery/" + R.raw.da_notification_2) //Here is FILE_NAME is the name of file that you want to play
            1 -> Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://da.arpan.delivery/" + R.raw.da_notification_1) //Here is FILE_NAME is the name of file that you want to play
            2 -> Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://da.arpan.delivery/" + R.raw.mixkit_residential_burglar_alert_1656) //Here is FILE_NAME is the name of file that you want to play
            else -> Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://da.arpan.delivery/" + R.raw.da_notification_1) //Here is FILE_NAME is the name of file that you want to play
        }

        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        for(entry in remoteMessage.data.entries){
            intent.putExtra(entry.key, entry.value.toString())
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = "Default"
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_arpan_icon_notification)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["body"]).setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(sound)
            .setVibrate(longArrayOf(1000L, 1000L, 1000L, 1000L, 1000L))
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N){
            builder.priority = NotificationCompat.PRIORITY_HIGH
            builder.setDefaults(Notification.DEFAULT_ALL)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.priority = NotificationManager.IMPORTANCE_HIGH
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val channel = NotificationChannel(
                channelId,
                "Default channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = remoteMessage.data["body"]
            channel.setSound(sound, attributes)
            channel.enableVibration(true)
            manager.createNotificationChannel(channel)
        }
        val notification = builder.build()
        notification.sound = sound
        notification.defaults = Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE
        manager.notify(0, notification)
    }

    companion object {
        private const val TAG = "MyFirebaseIdService"
    }
}