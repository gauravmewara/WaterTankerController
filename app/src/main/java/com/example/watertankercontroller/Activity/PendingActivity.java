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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PendingActivity extends AppCompatActivity implements View.OnClickListener {
    RelativeLayout menuback;
    TextView pagetitle,nodata;
    ProgressBar pendingprogress;
    BookingListAdapter adapter;
    RecyclerView pendinglistview;

    RelativeLayout toolbar_notification,noticountlayout;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    TextView notiCount;
    static String notificationCount;
    static Context context;

    private final int PAGE_START  = 1;
    private int TOTAL_PAGES = 1;
    private static int page_size = 7;
    //private int page_no = 1;
    LinearLayoutManager mLayoutManager;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;
    private int totalBookingCount;
    boolean isListNull = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        nodata = (TextView)findViewById(R.id.tv_pending_nodata);


        context = this;
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pendinglistview = (RecyclerView)findViewById(R.id.rv_pending_bookinglist);
        pendingprogress = (ProgressBar)findViewById(R.id.pg_pendinglist);
        nodata.setVisibility(View.GONE);
        pendingprogress.setVisibility(View.VISIBLE);
        pagetitle.setText(Constants.PENDING_PAGE_TITLE);


        adapter = new BookingListAdapter(PendingActivity.this,Constants.PENDING_CALL);
        mLayoutManager = new LinearLayoutManager(this);
        pendinglistview.setLayoutManager(mLayoutManager);
        pendinglistview.setItemAnimator(new DefaultItemAnimator());
        pendinglistview.setAdapter(adapter);
        pendinglistview.addOnScrollListener(new PaginationScrollListener(mLayoutManager) {
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
            String url = URLs.BASE_URL+URLs.PENDING_BOOKING_LIST+"?page_size="+String.valueOf(page_size)+"&page=1";
            Log.i("url", String.valueOf(url));
            String token = SessionManagement.getUserToken(this);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(this,pendingbookinglistener,url,headparam);
        }catch (Exception e){
            e.printStackTrace();
        }
     }

    FetchDataListener pendingbookinglistener = new FetchDataListener() {
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
                                    Log.i("Pending Booking", jsonObject.toString());
                                    BookingModal bmod = new BookingModal();
                                    bmod.setId(jsonObject.getString("_id"));
                                    bmod.setBookingid(jsonObject.getString("booking_id"));
                                    bmod.setPhonecode(jsonObject.getString("phone_country_code"));
                                    bmod.setFromtime("Trip not yet started");
                                    bmod.setTotime("Trip not yet started");
                                    bmod.setPickuppointid(jsonObject.getString("pickup_point_id"));
                                    bmod.setControllerid(jsonObject.getString("controller_id"));
                                    bmod.setPhone(jsonObject.getString("phone"));
                                    bmod.setBookedby(jsonObject.getString("booked_by"));
                                    bmod.setBookingtype("pending");

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
                        Log.d("Pending Booking:",array.toString());
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
            RequestQueueService.showAlert(msg, PendingActivity.this);
            setRecyclerView();
        }
        @Override
        public void onFetchStart() {
        }

    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setRecyclerView(){
        if(isListNull){
            pendingprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.VISIBLE);
            pendinglistview.setVisibility(View.GONE);
        }else{
            pendingprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.GONE);
            pendinglistview.setVisibility(View.VISIBLE);
        }
    }

    public void loadNextPage(){
        Log.d("loadNextPage: ", String.valueOf(currentPage));
        try{
            GETAPIRequest getapiRequest=new GETAPIRequest();
            String url = URLs.BASE_URL+URLs.PENDING_BOOKING_LIST+"?page_size="+page_size+"&page="+currentPage;
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
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = (JSONObject) array.get(i);
                                Log.i("Pending Booking", jsonObject.toString());
                                BookingModal bmod = new BookingModal();
                                bmod.setId(jsonObject.getString("_id"));
                                bmod.setBookingid(jsonObject.getString("booking_id"));
                                bmod.setPhonecode(jsonObject.getString("phone_country_code"));
                                bmod.setFromtime("Trip not yet started");
                                bmod.setTotime("Trip not yet started");
                                bmod.setPickuppointid(jsonObject.getString("pickup_point_id"));
                                bmod.setControllerid(jsonObject.getString("controller_id"));
                                bmod.setPhone(jsonObject.getString("phone"));
                                bmod.setBookedby(jsonObject.getString("booked_by"));
                                bmod.setBookingtype("pending");

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
                        }
                        Log.d("Pending Booking", mydata.toString());
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
            RequestQueueService.showAlert(msg,PendingActivity.this);
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
