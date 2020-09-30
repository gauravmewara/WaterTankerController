package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.Adapter.BookingListAdapter;
import com.example.watertankercontroller.Modal.BookingModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.GETAPIRequest;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.PaginationScrollListener;
import com.example.watertankercontroller.Utils.RequestQueueService;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;
import com.example.watertankercontroller.fcm.Config;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CompletedActivity extends AppCompatActivity implements View.OnClickListener{
    RelativeLayout menuback;
    TextView pagetitle,nodata;
    ProgressBar completedprogress;
    BookingListAdapter adapter;
    RecyclerView completedlistview;
    private final int PAGE_START  = 1;
    private int TOTAL_PAGES = 1;
    private static int page_size = 7;
    //private int page_no = 1;

    RelativeLayout toolbar_notification,noticountlayout;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    TextView notiCount;
    static String notificationCount;
    static Context context;


    LinearLayoutManager mLayoutManager;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;
    private int totalBookingCount;
    boolean isListNull = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        nodata = (TextView)findViewById(R.id.tv_completed_nodata);


        context = this;

        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        completedlistview = (RecyclerView)findViewById(R.id.rv_completed_bookinglist);
        completedprogress = (ProgressBar)findViewById(R.id.pg_completedlist);
        nodata.setVisibility(View.GONE);
        completedprogress.setVisibility(View.VISIBLE);
        pagetitle.setText(Constants.COMPLETED_PAGE_TITLE);

        adapter = new BookingListAdapter(CompletedActivity.this,Constants.COMPLETED_CALL);
        mLayoutManager = new LinearLayoutManager(this);
        completedlistview.setLayoutManager(mLayoutManager);
        completedlistview.setItemAnimator(new DefaultItemAnimator());
        completedlistview.setAdapter(adapter);
        completedlistview.addOnScrollListener(new PaginationScrollListener(mLayoutManager) {
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
        createBookingData();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
        }
    }
    public void createBookingData(){
            try{
                GETAPIRequest getapiRequest=new GETAPIRequest();
                String url = URLs.BASE_URL+URLs.COMPLETED_BOOKING_LIST+"?page_size="+String.valueOf(page_size)+"&page=1";
                Log.i("url", String.valueOf(url));
                String token = SessionManagement.getUserToken(this);
                HeadersUtil headparam = new HeadersUtil(token);
                getapiRequest.request(this.getApplicationContext(),completebookinglistener,url,headparam);
            }catch (Exception e){
                e.printStackTrace();
            }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    FetchDataListener completebookinglistener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            try {
                if (response != null) {
                    if (response.getInt("error")==0) {
                        ArrayList<BookingModal> tmodalList=new ArrayList<>();
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
                                    Log.i("Completed Booking", jsonObject.toString());
                                    BookingModal bmod = new BookingModal();
                                    bmod.setId(jsonObject.getString("_id"));
                                    bmod.setBookingid(jsonObject.getString("booking_id"));
                                    bmod.setPhonecode(jsonObject.getString("phone_country_code"));
                                    bmod.setMessage(jsonObject.getString("message"));
                                    bmod.setFromtime(jsonObject.getString("trip_start_at"));
                                    bmod.setTotime(jsonObject.getString("trip_end_at"));
                                    bmod.setPickuppointid(jsonObject.getString("pickup_point_id"));
                                    bmod.setControllerid(jsonObject.getString("controller_id"));
                                    bmod.setPhone(jsonObject.getString("phone"));
                                    bmod.setBookedby(jsonObject.getString("booked_by"));
                                    bmod.setBookingtype("completed");

                                    JSONObject distance = jsonObject.getJSONObject("distance");
                                    bmod.setDistance(distance.getString("text"));

                                    JSONObject droppoint = jsonObject.getJSONObject("drop_point");
                                    bmod.setTolocation(droppoint.getString("address").trim());
                                    bmod.setTolongitude(droppoint.getJSONObject("geometry").getJSONArray("coordinates").getString(0));
                                    bmod.setTolatitude(droppoint.getJSONObject("geometry").getJSONArray("coordinates").getString(1));

                                    JSONObject pickupoint = jsonObject.getJSONObject("pickup_point");
                                    bmod.setFromlocation(pickupoint.getString("address").trim());
                                    bmod.setFromlongitude(pickupoint.getJSONObject("geometry").getJSONArray("coordinates").getString(0));
                                    bmod.setFromlatitude(pickupoint.getJSONObject("geometry").getJSONArray("coordinates").getString(1));
                                    tmodalList.add(bmod);
                                }
                                isListNull = false;
                            }
                        }
                        Log.d("Completed Booking:",array.toString());
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
            RequestQueueService.showAlert(msg, CompletedActivity.this);
            setRecyclerView();
        }
        @Override
        public void onFetchStart() {
        }

    };

    public void setRecyclerView(){
        if(isListNull){
            completedprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.VISIBLE);
            completedlistview.setVisibility(View.GONE);
        }else{
            completedprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.GONE);
            completedlistview.setVisibility(View.VISIBLE);
        }
    }

    public void loadNextPage(){
        Log.d("loadNextPage: ", String.valueOf(currentPage));
        JSONObject jsonBodyObj = new JSONObject();
        try{
            GETAPIRequest getapiRequest=new GETAPIRequest();
            String url = URLs.BASE_URL+URLs.COMPLETED_BOOKING_LIST+"?page_size="+page_size+"&page="+currentPage;
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
                        ArrayList<BookingModal> tmodalList=new ArrayList<>();
                        JSONArray array = mydata.getJSONArray("data");
                        if(array!=null) {
                            if(array.length()!=0) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) array.get(i);
                                    Log.i("Completed Booking", jsonObject.toString());
                                    BookingModal bmod = new BookingModal();
                                    bmod.setId(jsonObject.getString("_id"));
                                    bmod.setBookingid(jsonObject.getString("booking_id"));
                                    bmod.setPhonecode(jsonObject.getString("phone_country_code"));
                                    bmod.setMessage(jsonObject.getString("message"));
                                    bmod.setFromtime(jsonObject.getString("trip_start_at"));
                                    bmod.setTotime(jsonObject.getString("trip_end_at"));
                                    bmod.setPickuppointid(jsonObject.getString("pickup_point_id"));
                                    bmod.setControllerid(jsonObject.getString("controller_id"));
                                    bmod.setPhone(jsonObject.getString("phone"));
                                    bmod.setBookedby(jsonObject.getString("booked_by"));
                                    bmod.setBookingtype("completed");
                                    JSONObject distance = jsonObject.getJSONObject("distance");
                                    bmod.setDistance(distance.getString("text"));
                                    JSONObject droppoint = jsonObject.getJSONObject("drop_point");
                                    bmod.setTolocation(droppoint.getString("address").trim());
                                    if (droppoint.has("coordinated")) {
                                        bmod.setTolongitude(droppoint.getJSONArray("coordinates").getString(0));
                                        bmod.setTolatitude(droppoint.getJSONArray("coordinates").getString(1));
                                    }
                                    JSONObject pickupoint = jsonObject.getJSONObject("pickup_point");
                                    bmod.setFromlocation(pickupoint.getString("address").trim());
                                    if (pickupoint.has("coordinates")) {
                                        bmod.setFromlongitude(pickupoint.getJSONArray("coordinates").getString(0));
                                        bmod.setFromlatitude(pickupoint.getJSONArray("coordinates").getString(1));
                                    }
                                    tmodalList.add(bmod);
                                }
                            }
                        }
                        Log.d("CompletedBooking", mydata.toString());
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

            RequestQueueService.showAlert(msg,CompletedActivity.this);
        }

        @Override
        public void onFetchStart() {

            //RequestQueueService.showProgressDialog(Login.this);
        }

    };



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
