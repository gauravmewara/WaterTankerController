package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;

public class BookingDetails extends AppCompatActivity implements View.OnClickListener {

    TextView bookingid,distance,pickup,drop,drivername,contact_no,message,pagetitle;
    ImageView calltous;
    ImageView menunotification;
    RelativeLayout menuback;
    String init_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        init_type = getIntent().getExtras().getString("init_type");
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        bookingid = (TextView)findViewById(R.id.tv_bookingdetail_bookingid);
        distance = (TextView)findViewById(R.id.tv_bookingdetail_distance);
        pickup = (TextView)findViewById(R.id.tv_bookingdetail_pickup);
        drop = (TextView)findViewById(R.id.tv_bookingdetail_pickup);
        drivername = (TextView)findViewById(R.id.tv_bookingdetail_pickup);
        contact_no = (TextView)findViewById(R.id.tv_bookingdetail_pickup);
        message = (TextView)findViewById(R.id.tv_bookingdetail_pickup);
        calltous = (ImageView)findViewById(R.id.iv_bookingdetail_bookingid_call);
        calltous.setOnClickListener(this);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        menunotification = (ImageView)findViewById(R.id.iv_toolabar2_notification);
        menunotification.setOnClickListener(this);
        if(init_type.equals(Constants.COMPLETED_CALL)){
            pagetitle.setText("Completed Booking Details");
        }else if(init_type.equals(Constants.ABORTED_CALL)){
            pagetitle.setText("Aborted Booking Details");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.iv_toolabar2_notification:
                Intent intent;
                intent = new Intent(BookingDetails.this,NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_bookingdetail_bookingid_call:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
