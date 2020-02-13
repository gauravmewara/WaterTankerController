package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.watertankercontroller.Adapter.BookingListAdapter;
import com.example.watertankercontroller.Modal.BookingModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;

import java.util.ArrayList;

public class CompletedActivity extends AppCompatActivity implements View.OnClickListener{
    ImageView menunotification;
    RelativeLayout menuback;
    TextView pagetitle,nodata;
    ProgressBar completedprogress;
    BookingListAdapter adapter;
    RecyclerView completedlistview;
    ArrayList<BookingModal> bookinglist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        nodata = (TextView)findViewById(R.id.tv_completed_nodata);
        menunotification = (ImageView)findViewById(R.id.iv_toolabar2_notification);
        menunotification.setOnClickListener(this);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        completedlistview = (RecyclerView)findViewById(R.id.rv_completed_bookinglist);
        completedprogress = (ProgressBar)findViewById(R.id.pg_completedlist);
        nodata.setVisibility(View.GONE);
        completedprogress.setVisibility(View.VISIBLE);
        pagetitle.setText(Constants.COMPLETED_PAGE_TITLE);
        createBookingData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.iv_toolabar2_notification:
                Intent intent;
                intent = new Intent(CompletedActivity.this,NotificationActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void createBookingData(){
        BookingModal data1 = new BookingModal();
        BookingModal data2 = new BookingModal();
        BookingModal data3 = new BookingModal();
        data1.setBookingid("1234567890");
        data1.setDistance("15 KM");
        data1.setFromlocation("Boranada, Summer Nagar, 115");
        data1.setFromtime("Saturday, 24 January, 04:20 PM");
        data1.setTolocation("Chopasani Housing Board, Shree Krishna Nagar, 161");
        data1.setTotime("Saturday, 24 January, 04:40 PM");

        data2.setBookingid("1234567891");
        data2.setDistance("16 KM");
        data2.setFromlocation("Koranada, Summer Nagar, 115");
        data2.setFromtime("Katurday, 24 January, 04:20 PM");
        data2.setTolocation("Khopasani Housing Board, Shree Krishna Nagar, 161");
        data2.setTotime("Katurday, 24 January, 04:40 PM");

        data3.setBookingid("1234567892");
        data3.setDistance("17 KM");
        data3.setFromlocation("Moranada, Summer Nagar, 115");
        data3.setFromtime("Maturday, 24 January, 04:20 PM");
        data3.setTolocation("Mhopasani Housing Board, Shree Krishna Nagar, 161");
        data3.setTotime("Maturday, 24 January, 04:40 PM");

        bookinglist = new ArrayList<>();
        bookinglist.add(data1);
        bookinglist.add(data2);
        bookinglist.add(data3);
        setRecyclerView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setRecyclerView(){
        if(bookinglist==null){
            completedprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.VISIBLE);
            completedlistview.setVisibility(View.GONE);
        }else{
            completedprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.GONE);
            completedlistview.setVisibility(View.VISIBLE);
            adapter = new BookingListAdapter(CompletedActivity.this, bookinglist, Constants.COMPLETED_CALL);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            completedlistview.setLayoutManager(mLayoutManager);
            completedlistview.setAdapter(adapter);
        }
    }
}
