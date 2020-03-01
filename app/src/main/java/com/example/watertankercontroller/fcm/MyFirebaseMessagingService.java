package com.example.watertankercontroller.fcm;



import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.watertankercontroller.Activity.NotificationActivity;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    String timestamp;
    private static final int REQUEST_CODE = 1;
    private static final int NOTIFICATION_ID = 6578;
    private NotificationUtilsFcm notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        Log.e(TAG, "Notification Data : " + remoteMessage.getData().toString());

        if (remoteMessage == null)
            return;

        if(remoteMessage.getData().size()>0){
            boolean x = true;
            if(x){

            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            try {
                Map<String,String> s = remoteMessage.getData();
                Log.e(TAG, "string: " + s.toString());
                JSONObject json = new JSONObject(s);
                showNotifications("Seremo",json);
            } catch (Exception e) {
                Log.e(TAG, "Exception at Ini: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showNotifications(String title,JSONObject json) {
        try {
            //String txn_id = json.getString("transaction_id");
            //String not_id = json.getString("notification_id");
            String message = json.getString("message");

            SharedPrefUtil.setPreferences(getApplicationContext(), Constants.SHARED_PREF_NOTICATION_TAG, Constants.SHARED_NOTIFICATION_UPDATE_KEY, "yes");
            if (!NotificationUtilsFcm.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
            }
            timestamp = DateFormat.getDateTimeInstance().format(new Date());
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE,
            //      i, PendingIntent.FLAG_UPDATE_CURRENT);
            final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String NOTIFICATION_CHANNEL_ID = "101";
            Intent intent = new Intent(this, NotificationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Bundle b = new Bundle();
            b.putString("start_from", "notification");
            //b.putString("transaction_id", txn_id);
            //b.putString("notification_id", not_id);
            b.putString("ispush","1");
            intent.putExtras(b);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, 0);

            final PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            getApplicationContext(),
                            REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Controller";
                String description = "Controller Notifications";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
                channel.setDescription(description);
                channel.setVibrationPattern(new long[]{0, 1000, 500});
                channel.enableVibration(true);
                channel.enableLights(true);
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.quantum_black_100))
                    .setColorized(true)
                    .setSound(alarmSound)
                    .setVibrate(new long[]{0, 1000, 500})
                    //.setWhen(getTimeMilliSec(timestamp))
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true);
            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.notify(NOTIFICATION_ID, builder.build());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}