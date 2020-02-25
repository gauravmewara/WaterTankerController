package com.example.watertankercontroller.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    ConstraintLayout pickupLayout,dropLayout;
    PickupPlaceModal selectedpickup = null;
    PickupPlaceModal selecteddrop = null;
    boolean pickupselected=false,dropselected=false;
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
        pickupLayout = (ConstraintLayout)findViewById(R.id.cl_bookingform_pickup);
        pickupLayout.setOnClickListener(this);
        drop = (ImageView)findViewById(R.id.iv_bookingform_dropmap);
        dropLayout = (ConstraintLayout)findViewById(R.id.cl_bookingform_drop);
        dropLayout.setOnClickListener(this);
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
            case R.id.cl_bookingform_pickup:
                intent = new Intent(BookingForm.this,PickupActivity.class);
                if(pickupselected)
                    intent.putExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE,selectedpickup);
                if(dropselected)
                    intent.putExtra(Constants.DROP_LOCATION_INTENT_DATA_TITLE,selectedpickup);

                startActivityForResult(intent,Constants.BOOKINGFORM_ACTIVITY_MAP_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.cl_bookingform_drop:
                intent = new Intent(BookingForm.this,PickupActivity.class);
                if(pickupselected)
                    intent.putExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE,selectedpickup);
                if(dropselected)
                    intent.putExtra(Constants.DROP_LOCATION_INTENT_DATA_TITLE,selectedpickup);

                startActivityForResult(intent,Constants.BOOKINGFORM_ACTIVITY_MAP_ACTIVITY_REQUEST_CODE);
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
        switch (requestCode){
            case Constants.BOOKINGFORM_ACTIVITY_MAP_ACTIVITY_REQUEST_CODE:
                if(resultCode== Activity.RESULT_OK){
                    if(data.hasExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE)) {
                        pickupselected = true;
                        selectedpickup = data.getParcelableExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
                        pickuplocation.setText(selectedpickup.getLocationname());
                        pickupaddress.setText(selectedpickup.getLocationaddress());
                    }
                    if(data.hasExtra(Constants.DROP_LOCATION_INTENT_DATA_TITLE)) {
                        dropselected = true;
                        selecteddrop = data.getParcelableExtra(Constants.DROP_LOCATION_INTENT_DATA_TITLE);
                        droplocation.setText(selecteddrop.getLocationname());
                        dropaddress.setText(selecteddrop.getLocationaddress());
                    }
                }
                break;
        }
    }
}
