package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.DrawableWrapper;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.GETAPIRequest;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.POSTAPIRequest;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;
import com.example.watertankercontroller.fcm.Config;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BookingStatus extends AppCompatActivity implements View.OnClickListener {
    RelativeLayout toolbar_notification,noticountlayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    RelativeLayout bookingform,tankerdetail,logout,completedbooking,pendingbooking,ongoingbooking,abortedbooking,toolbar_toggle;
    TextView name,location,notiCount;
    DrawerLayout navdrawer;
    static String notificationCount;
    static Context context;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_status);
        navdrawer = (DrawerLayout)findViewById(R.id.nav_drawer_bookingstatus);
        toolbar_toggle = (RelativeLayout) findViewById(R.id.rl_toolbar_menu);
        toolbar_notification = (RelativeLayout) findViewById(R.id.rl_toolbar_notification_view);
        toolbar_notification.setOnClickListener(this);
        noticountlayout = (RelativeLayout)findViewById(R.id.rl_toolbar_notificationcount);
        notiCount = (TextView)findViewById(R.id.tv_toolbar_notificationcount);
        context = this;
        name = (TextView)findViewById(R.id.tv_nav_name);
        location = (TextView)findViewById(R.id.tv_nav_username);
        bookingform = (RelativeLayout)findViewById(R.id.rl_nav_bookingform);
        bookingform.setOnClickListener(this);
        tankerdetail = (RelativeLayout)findViewById(R.id.rl_nav_tankerdetails);
        tankerdetail.setOnClickListener(this);
        logout = (RelativeLayout)findViewById(R.id.rl_nav_logout);
        logout.setOnClickListener(this);
        completedbooking = (RelativeLayout)findViewById(R.id.rl_bookingstatus_completed);
        completedbooking.setOnClickListener(this);
        pendingbooking = (RelativeLayout)findViewById(R.id.rl_bookingstatus_pending);
        pendingbooking.setOnClickListener(this);
        ongoingbooking = (RelativeLayout)findViewById(R.id.rl_bookingstatus_ongoing);
        ongoingbooking.setOnClickListener(this);
        abortedbooking = (RelativeLayout)findViewById(R.id.rl_bookingstatus_aborted);
        abortedbooking.setOnClickListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, navdrawer,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        context = this;
        navdrawer.setScrimColor(Color.TRANSPARENT);
        navdrawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navdrawer.setDrawerElevation(0f);
        toolbar_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerMenu(view);
            }
        });
        name.setText(SessionManagement.getName(BookingStatus.this));
        location.setText(SessionManagement.getLocation(BookingStatus.this));
        int noticount = Integer.parseInt(SessionManagement.getNotificationCount(this));
        if(noticount<=0){
            clearNotificationCount();
        }else{
            notiCount.setText(String.valueOf(noticount));
            noticountlayout.setVisibility(View.VISIBLE);
        }
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    String message = intent.getStringExtra("message");
                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                    int count = Integer.parseInt(SessionManagement.getNotificationCount(BookingStatus.this));
                    setNotificationCount(count+1,false);
                }
            }
        };
    }

    public void drawerMenu (View view ){
        navdrawer.openDrawer(Gravity.LEFT);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        navdrawer.closeDrawers();
        switch(view.getId()){
            case R.id.rl_nav_bookingform:
                intent = new Intent(BookingStatus.this,BookingForm.class);
                startActivity(intent);
                break;
            case R.id.rl_nav_tankerdetails:
                intent = new Intent(BookingStatus.this,TankerDetails.class);
                startActivity(intent);
                break;
            case R.id.rl_nav_logout:
                logout.setClickable(false);
                logoutApiCalling();
                /*intent = new Intent(BookingStatus.this,LoginActivity.class);
                startActivity(intent);
                finish();*/
                break;
            case R.id.rl_bookingstatus_completed:
                intent = new Intent(BookingStatus.this,CompletedActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_bookingstatus_pending:
                intent = new Intent(BookingStatus.this,PendingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_bookingstatus_ongoing:
                intent = new Intent(BookingStatus.this,OngoingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_bookingstatus_aborted:
                intent = new Intent(BookingStatus.this,AbortedActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_toolbar_notification_view:
                intent = new Intent(BookingStatus.this,NotificationActivity.class);
                startActivity(intent);
                break;
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void logoutApiCalling(){
        try {
            POSTAPIRequest getapiRequest = new POSTAPIRequest();
            String url = URLs.BASE_URL + URLs.SIGN_OUT_URL;
            Log.i("url", String.valueOf(url));
            Log.i("Request", String.valueOf(getapiRequest));
            //String token = FirebaseInstanceId.getInstance().getToken();
            String token = SessionManagement.getUserToken(this);
            Log.i("Token:",token);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(BookingStatus.this,logoutListener,url,headparam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    FetchDataListener logoutListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject data) {
            try {
                if (data != null) {
                    if (data.getInt("error") == 0) {
                        FirebaseAuth.getInstance().signOut();
                        SessionManagement.logout(logoutListener, BookingStatus.this);
                        Intent i = new Intent(BookingStatus.this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        Toast.makeText(BookingStatus.this, "You are now logout", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            logout.setClickable(true);
        }

        @Override
        public void onFetchStart() {

        }
    };

    public void setNotificationCount(int count,boolean isStarted){
        notificationCount = SessionManagement.getNotificationCount(context);
        if(Integer.parseInt(notificationCount)!=count) {
            notificationCount = String.valueOf(count);
            if (count <= 0) {
                clearNotificationCount();
            } else if (count < 100) {
                notiCount.setText(String.valueOf(count));
                noticountlayout.setVisibility(View.VISIBLE);
            } else {
                notiCount.setText("99+");
                noticountlayout.setVisibility(View.VISIBLE);
            }
            SharedPrefUtil.setPreferences(context,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_COUNT_KEY,notificationCount);
            boolean b2 = SharedPrefUtil.getStringPreferences(this,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_UPDATE_KEY).equals("yes");
            if(b2)
                SharedPrefUtil.setPreferences(context,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_UPDATE_KEY,"no");
        }
    }
    public void clearNotificationCount(){
        notiCount.setText("");
        noticountlayout.setVisibility(View.GONE);
    }

    public void newNotification(){
        Log.i("newNotification","Notification");
        int count = Integer.parseInt(SharedPrefUtil.getStringPreferences(context,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_COUNT_KEY));
        setNotificationCount(count+1,false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        // clear the notification area when the app is opened
        int sharedCount = Integer.parseInt(SharedPrefUtil.getStringPreferences(this,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_COUNT_KEY));
        int viewCount = Integer.parseInt(notiCount.getText().toString());
        boolean b1 = sharedCount!=viewCount;
        boolean b2 = SharedPrefUtil.getStringPreferences(this,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_UPDATE_KEY).equals("yes");
        if(b2){
            newNotification();
        }else if (b1){
            if (sharedCount < 100 && sharedCount>0) {
                notiCount.setText(String.valueOf(sharedCount));
                noticountlayout.setVisibility(View.VISIBLE);
            } else {
                notiCount.setText("99+");
                noticountlayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
}
