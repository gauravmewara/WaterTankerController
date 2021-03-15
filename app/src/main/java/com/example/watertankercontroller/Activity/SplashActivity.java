package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.Activity.BookingStatus;
import com.example.watertankercontroller.Activity.LoginActivity;
import com.example.watertankercontroller.Activity.SelectServer;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.GETAPIRequest;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.RequestQueueService;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    ConstraintLayout cl_NoInternet;
    ImageView iv_refresh;
    TextView tv_NoInternet;
    int refreshlevel = 0;
    boolean authorized = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        cl_NoInternet = (ConstraintLayout)findViewById(R.id.cl_no_internet);
        tv_NoInternet = (TextView)findViewById(R.id.tv_no_internet);
        iv_refresh = (ImageView)findViewById(R.id.iv_refresh_icon);
        iv_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_refresh.setClickable(false);
                cl_NoInternet.setVisibility(View.GONE);
                if(refreshlevel==0)
                    getNotificationCount();
                iv_refresh.setClickable(true);
            }
        });
        NotificationManager nm = (NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        nm.cancelAll();
        if(SharedPrefUtil.hasKey(this,Constants.SHARED_PREF_LOGIN_TAG, Constants.SERVER_IP)) {
            URLs.BASE_URL = SharedPrefUtil.getStringPreferences(this,Constants.SHARED_PREF_LOGIN_TAG, Constants.SERVER_IP)+"/api/controller/";
            URLs.SOCKET_URL=SharedPrefUtil.getStringPreferences(this,Constants.SHARED_PREF_LOGIN_TAG, Constants.SERVER_IP)+"?token=";
            if (SessionManagement.checkSignIn(this)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getNotificationCount();
                    /*Intent i = new Intent(SplashActivity.this, BookingStatus.class);
                    startActivity(i);
                    finish();*/
                    }
                }, SPLASH_TIME_OUT);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, SPLASH_TIME_OUT);
            }
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashActivity.this, SelectServer.class);
                    startActivity(i);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }
        cl_NoInternet.setVisibility(View.GONE);
    }

    private void getNotificationCount() {
        JSONObject jsonBodyObj = new JSONObject();
        try {
            GETAPIRequest getapiRequest = new GETAPIRequest();
            String url = URLs.BASE_URL + URLs.UNREAD_NOTIFICATION_COUNT;
            Log.i("url", String.valueOf(url));
            Log.i("Request", String.valueOf(getapiRequest));
            String token = SessionManagement.getUserToken(this);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(this, notiCountListener, url, headparam, jsonBodyObj);
        } catch (JSONException e) {
            e.printStackTrace();
            refreshlevel = 0;
            tv_NoInternet.setText("An Error Occurred");
            cl_NoInternet.setVisibility(View.VISIBLE);
        }
    }

    FetchDataListener notiCountListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            Log.d("NotiCount:",response.toString());
            try {
                if (response != null) {

                        if (response.getInt("error") == 0) {

                                JSONObject data = response.getJSONObject("data");
                                String count = data.getString("count");
                                SessionManagement.setNotificationCount(SplashActivity.this, count);
                            Intent i = new Intent(SplashActivity.this, BookingStatus.class);
                                startActivity(i);
                                finish();
//                            if (authorized) {
//
//                            }else{
//                                FirebaseAuth.getInstance().signOut();
//                                SharedPrefUtil.deletePreference(SplashActivity.this,Constants.SHARED_PREF_LOGIN_TAG);
//                                SharedPrefUtil.deletePreference(SplashActivity.this,Constants.SHARED_PREF_NOTICATION_TAG);
//
//                                Intent i = new Intent(SplashActivity.this, SelectServer.class);
//                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                startActivity(i);
//                                Toast.makeText(SplashActivity.this, "Due to unauthorized activity ,You are now logout", Toast.LENGTH_SHORT).show();
//                                finish();
//                            }
                        }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                refreshlevel = 0;
                tv_NoInternet.setText("An Error Occurred");
                cl_NoInternet.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onFetchFailure(String msg) {
            refreshlevel = 0;
            tv_NoInternet.setText("No Internet Connectivity");
            cl_NoInternet.setVisibility(View.VISIBLE);
        }

        @Override
        public void onFetchStart() {

        }

    };
}
