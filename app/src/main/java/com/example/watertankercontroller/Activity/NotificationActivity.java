package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.example.watertankercontroller.Adapter.NotificationAdapter;
import com.example.watertankercontroller.Modal.BookingModal;
import com.example.watertankercontroller.Modal.NotificationModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.GETAPIRequest;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.POSTAPIRequest;
import com.example.watertankercontroller.Utils.PaginationScrollListener;
import com.example.watertankercontroller.Utils.RequestQueueService;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;
import com.example.watertankercontroller.fcm.Config;
import com.example.watertankercontroller.fcm.NotificationUtilsFcm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView notificationlistview;
    NotificationAdapter adapter;
    RelativeLayout menuback;
    TextView pagetitle,nodata;
    ProgressBar notificationprogress;

    RelativeLayout toolbar_notification,noticountlayout;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    TextView notiCount;
    static String notificationCount;
    static Context context;

    private final int PAGE_START  = 1;
    private int TOTAL_PAGES = 1;
    private static int page_size = 15;
    //private int page_no = 1;
    LinearLayoutManager mLayoutManager;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;
    private int totalBookingCount;
    boolean isListNull = true,isPush=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        isPush = false;
        if(b!=null) {
            if (b.containsKey("ispush")) {
                if (b.getString("ispush").equals("1")) {
                    isPush = true;
                }
            }
        }
        notificationlistview = (RecyclerView)findViewById(R.id.rv_notification);
        notificationlistview.setVisibility(View.GONE);
        nodata = (TextView)findViewById(R.id.tv_notificationitem_nodata);
        nodata.setVisibility(View.GONE);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        context = this;
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pagetitle.setText(Constants.NOTIFICATION_PAGE_TITLE);

        notificationprogress = (ProgressBar)findViewById(R.id.pg_notification);
        notificationprogress.setVisibility(View.VISIBLE);

        adapter = new NotificationAdapter(NotificationActivity.this);
        mLayoutManager = new LinearLayoutManager(this);
        notificationlistview.setLayoutManager(mLayoutManager);
        notificationlistview.setItemAnimator(new DefaultItemAnimator());
        notificationlistview.setAdapter(adapter);
        notificationlistview.addOnScrollListener(new PaginationScrollListener(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 1000);
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    reloadNotification();
                }
            }
        };
        NotificationManager nm = (NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        nm.cancelAll();
        createNotificationData();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if(isPush) {
            Intent i = new Intent(this, BookingStatus.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }else {
            super.onBackPressed();
        }
    }

    public void createNotificationData(){
        try{
            GETAPIRequest getapiRequest=new GETAPIRequest();
            String url = URLs.BASE_URL+URLs.NOTIFICATION_LIST+"?page_size="+String.valueOf(page_size)+"&page=1";
            Log.i("url", String.valueOf(url));
            String token = SessionManagement.getUserToken(this);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(this.getApplicationContext(),getnotificationlistener,url,headparam);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    FetchDataListener getnotificationlistener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            try {
                if (response != null) {
                    if (response.getInt("error")==0) {
                        ArrayList<NotificationModal> tmodalList=new ArrayList<>();
                        JSONArray array = response.getJSONArray("data");
                        totalBookingCount = response.getInt("total");
                        if(totalBookingCount>page_size) {
                            if (totalBookingCount % page_size == 0) {
                                TOTAL_PAGES = totalBookingCount / page_size;
                            } else {
                                TOTAL_PAGES = (totalBookingCount / page_size) + 1;
                            }
                        }
                        if(array!=null) {
                            if(array.length()!=0) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) array.get(i);
                                    Log.i("Notification list", jsonObject.toString());
                                    NotificationModal bmod = new NotificationModal();
                                    bmod.setNotifiactionid(jsonObject.getString("_id"));
                                    bmod.setBookingid(jsonObject.getJSONObject("data").getString("booking_id"));
                                    bmod.setIsread("0");
                                    bmod.setControllerid("controller_id");
                                    bmod.setNotificationtype(jsonObject.getString("type"));
                                    bmod.setText(jsonObject.getJSONObject("text").getString("en"));
                                    if (jsonObject.has("title")) {
                                        bmod.setTitle(jsonObject.getJSONObject("title").getString("en"));
                                    } else {
                                        bmod.setTitle("No Title Recieved");
                                    }
                                    tmodalList.add(bmod);
                                }
                                isListNull = false;
                            }

                        }
                        Log.d("Notification List:",array.toString());
                        isListNull = false;
                        setRecyclerView();
                        //progressBar.setVisibility(View.GONE);
                        adapter.addAll(tmodalList);
                        if (currentPage < TOTAL_PAGES)
                            adapter.addLoadingFooter();
                        else
                            isLastPage = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                setRecyclerView();
            }
        }
        @Override
        public void onFetchFailure(String msg) {
            RequestQueueService.showAlert(msg, NotificationActivity.this);
            setRecyclerView();
        }
        @Override
        public void onFetchStart() {
        }

    };

    public void setRecyclerView(){
        if(isListNull){
            notificationprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.VISIBLE);
            notificationlistview.setVisibility(View.GONE);
        }else{
            notificationprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.GONE);
            notificationlistview.setVisibility(View.VISIBLE);
        }
    }

    public void loadNextPage(){
        Log.d("loadNextPage: ", String.valueOf(currentPage));
        try{
            GETAPIRequest getapiRequest=new GETAPIRequest();
            String url = URLs.BASE_URL+URLs.NOTIFICATION_LIST+"?page_size="+page_size+"&page="+currentPage;
            Log.i("url", String.valueOf(url));
            //Log.i("Request", String.valueOf(getapiRequest));
            String token = SessionManagement.getUserToken(this);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(this,nextListener,url,headparam);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    FetchDataListener nextListener=new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject mydata) {
            //RequestQueueService.cancelProgressDialog();
            try {
                if (mydata != null) {
                    if (mydata.getInt("error")==0) {
                        ArrayList<NotificationModal> tmodalList=new ArrayList<>();
                        JSONArray array = mydata.getJSONArray("data");
                        if(array!=null) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = (JSONObject) array.get(i);
                                Log.i("Notification List", jsonObject.toString());
                                NotificationModal bmod = new NotificationModal();
                                bmod.setNotifiactionid(jsonObject.getString("_id"));
                                bmod.setBookingid(jsonObject.getJSONObject("data").getString("booking_id"));
                                bmod.setIsread("0");
                                bmod.setControllerid("controller_id");
                                bmod.setNotificationtype(jsonObject.getString("type"));
                                bmod.setText(jsonObject.getJSONObject("text").getString("en"));
                                bmod.setTitle(jsonObject.getJSONObject("title").getString("en"));
                                tmodalList.add(bmod);
                            }
                        }
                        Log.d("Notification list", mydata.toString());
                        adapter.removeLoadingFooter();
                        isLoading = false;
                        adapter.addAll(tmodalList);
                        if (currentPage < TOTAL_PAGES) adapter.addLoadingFooter();
                        else isLastPage = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            //RequestQueueService.cancelProgressDialog();
            RequestQueueService.showAlert(msg,NotificationActivity.this);
        }

        @Override
        public void onFetchStart() {
            //RequestQueueService.showProgressDialog(Login.this);
        }

    };

    public void readNotificationApiCall(String notificationId){
        try {
            POSTAPIRequest getapiRequest = new POSTAPIRequest();
            String url = URLs.BASE_URL + URLs.READ_NOTIFICATION+notificationId;
            String token = SessionManagement.getUserToken(this);
            Log.i("Token:",token);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(NotificationActivity.this,readListener,url,headparam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    FetchDataListener readListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject data) {
            try {
                if (data != null) {
                    if (data.getInt("error") == 0) {
                        int count = Integer.parseInt(SharedPrefUtil.getStringPreferences(context,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_COUNT_KEY))-1;
                        //notiCount.setText(String.valueOf(count));
                        SharedPrefUtil.setPreferences(context,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_COUNT_KEY,String.valueOf(count));
                        //reloadNotification();
                        adapter.setReadCalled(true);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                adapter.setReadCalled(false);
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            adapter.setReadCalled(false);
        }

        @Override
        public void onFetchStart() {

        }
    };

    public void reloadNotification(){
        if (SharedPrefUtil.getStringPreferences(this,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_UPDATE_KEY).equals("yes")) {
            NotificationUtilsFcm.clearNotifications(this);
            adapter.clearNotifications();
            notificationprogress.setVisibility(View.VISIBLE);
            SharedPrefUtil.setPreferences(this, Constants.SHARED_PREF_NOTICATION_TAG, Constants.SHARED_NOTIFICATION_UPDATE_KEY, "no");
            createNotificationData();
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

}
