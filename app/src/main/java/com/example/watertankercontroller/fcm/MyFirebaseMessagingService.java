package com.example.watertankercontroller.fcm;



import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.watertankercontroller.Activity.BookingDetails;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    String timestamp;
    private static final int REQUEST_CODE = 1;
    private static final int NOTIFICATION_ID = 65789;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("PushNotification",remoteMessage.toString());
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        Log.i(TAG, "Notification Data : " + remoteMessage.getData().toString());
        if (remoteMessage == null)
            return;
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            try {
                Map<String, String> s = remoteMessage.getData();
                Log.e(TAG, "string: " + s.toString());
                JSONObject json = new JSONObject(s);
                showNotifications(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception at Ini: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showNotifications(JSONObject json) {
        try {
            String bk_id = json.getString("booking_id");
            String message = json.getString("body");
            String title = json.getString("title");
            String notid = json.getString("notification_id");
            SharedPrefUtil.setPreferences(getApplicationContext(), Constants.SHARED_PREF_NOTICATION_TAG, Constants.SHARED_NOTIFICATION_UPDATE_KEY, "yes");
            timestamp = DateFormat.getDateTimeInstance().format(new Date());
            final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String NOTIFICATION_CHANNEL_ID = "controller_channel_01";
            Intent intent = new Intent(this, BookingDetails.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Bundle b = new Bundle();
            b.putString("init_type", "notification");
            b.putString("message",title);
            b.putString("booking_id", bk_id);
            b.putString("ispush","1");
            b.putString("notification_id",notid);
            intent.putExtras(b);
            if (!NotificationUtilsFcm.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Log.i("Push Notification","Broadcasting");
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
            }
            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
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
                mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
                mChannel.setDescription(description);
                mChannel.setVibrationPattern(new long[]{0, 1000, 500});
                mChannel.enableVibration(true);
                mChannel.enableLights(true);
                mChannel.setSound(alarmSound,audioAttributes);
                mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                if(mNotificationManager!=null)
                    mNotificationManager.createNotificationChannel(mChannel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.Black))
                    .setColorized(true)
                    .setVibrate(new long[]{0, 1000, 500})
                    .setSound(alarmSound)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);
            Log.i("Push Notification","Notifying");
            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (Exception e){
            Log.e("Push Notification",e.toString());
            e.printStackTrace();
        }
    }
}