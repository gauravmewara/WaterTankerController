package com.example.watertankercontroller.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.watertankercontroller.Modal.PickupPlaceModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;

import org.w3c.dom.Text;

public class BookingForm extends AppCompatActivity implements View.OnClickListener {
    EditText mobile,message;
    TextView pagetitle;
    TextView pickuplocation,pickupaddress,droplocation,dropaddress;
    RelativeLayout create;
    ImageView menunotification,pickup,drop;
    RelativeLayout menuback;
    PickupPlaceModal selectedpickup = null;
    boolean pickupselected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        menunotification = (ImageView)findViewById(R.id.iv_toolabar2_notification);
        menunotification.setOnClickListener(this);
        pickup = (ImageView)findViewById(R.id.iv_bookingform_pickupmap);
        pickup.setOnClickListener(this);
        drop = (ImageView)findViewById(R.id.iv_bookingform_dropmap);
        drop.setOnClickListener(this);
        create = (RelativeLayout)findViewById(R.id.rl_bookingform_create);
        create.setOnClickListener(this);
        mobile = (EditText)findViewById(R.id.et_bookingform_mobile);
        message = (EditText)findViewById(R.id.et_bookingform_meesage);
        pagetitle.setText(Constants.BOOKINGFORM_PAGE_TITLE);
        pickuplocation = (TextView)findViewById(R.id.tv_bookingform_pickup_location1);
        pickupaddress = (TextView)findViewById(R.id.tv_bookingform_pickup_location2);
        droplocation = (TextView)findViewById(R.id.tv_bookingform_drop_location1);
        dropaddress = (TextView)findViewById(R.id.tv_bookingform_drop_location2);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.iv_toolabar2_notification:
                intent = new Intent(BookingForm.this,NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_bookingform_pickupmap:
                intent = new Intent(BookingForm.this,PickupActivity.class);
                if(pickupselected)
                    intent.putExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE,selectedpickup);
                startActivityForResult(intent,Constants.BOOKINGFORM_ACTIVITY_PICKUP_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.iv_bookingform_drop:
                break;
            case R.id.rl_bookingform_create:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constants.BOOKINGFORM_ACTIVITY_PICKUP_ACTIVITY_REQUEST_CODE){
            if(resultCode== Activity.RESULT_OK){
                selectedpickup = data.getParcelableExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
                pickupselected = true;
                pickuplocation.setText(selectedpickup.getLocationname());
                pickupaddress.setText(selectedpickup.getLocationaddress());
            }else{
                if(selectedpickup == null){
                    pickupselected = false;
                    pickuplocation.setText("");
                    pickupaddress.setText("");
                }
            }
        }else if(requestCode==Constants.BOOKINGFORM_ACTIVITY_DROP_ACTIVITY_REQUEST_CODE){

        }
    }
}
