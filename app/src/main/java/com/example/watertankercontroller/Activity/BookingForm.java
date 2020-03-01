package com.example.watertankercontroller.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.Modal.PickupPlaceModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.POSTAPIRequest;
import com.example.watertankercontroller.Utils.RequestQueueService;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;
import com.example.watertankercontroller.fcm.Config;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.w3c.dom.Text;

public class BookingForm extends AppCompatActivity implements View.OnClickListener {
    EditText mobile,message;
    TextView pagetitle;
    TextView pickuplocation,pickupaddress,droplocation,dropaddress,messagelength;
    RelativeLayout create;
    ImageView pickup,drop;
    RelativeLayout menuback;
    ConstraintLayout pickupLayout,dropLayout;
    PickupPlaceModal selectedpickup = null;
    PickupPlaceModal selecteddrop = null;

    RelativeLayout toolbar_notification,noticountlayout;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    TextView notiCount;
    static String notificationCount;
    static Context context;

    boolean pickupselected=false,dropselected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);

        toolbar_notification = (RelativeLayout) findViewById(R.id.rl_toolbar2_notification_view);
        toolbar_notification.setOnClickListener(this);
        noticountlayout = (RelativeLayout)findViewById(R.id.rl_toolbar2_notificationcount);
        notiCount = (TextView)findViewById(R.id.tv_toolbar2_notificationcount);
        context = this;

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
        messagelength = (TextView)findViewById(R.id.tv_bookingform_message_length);
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                messagelength.setText(String.valueOf(message.getText().length()));
            }
        });

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
                    int count = Integer.parseInt(SessionManagement.getNotificationCount(BookingForm.this));
                    setNotificationCount(count+1,false);
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.rl_toolbar2_notification_view:
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
                boolean valid = isDataValid();
                if(valid){
                    create.setClickable(false);
                    createBooking();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
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

    public boolean isDataValid(){
        String message="";
        boolean valid = true;
        if(!pickupselected){
            message = "Select Pickup Location";
            valid = false;
        }else if(!dropselected){
            message = "select Drop Location";
            valid = false;
        }else if(mobile.getText().toString().trim().length()<=0){
            message = "Enter Mobile Number";
            valid = false;
        }else if(mobile.getText().toString().trim().length()<10){
            message = "Mobile number should be of 10 digits";
            valid = false;
        }
        if(!valid)
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();

        return valid;
    }

    public void createBooking(){
        JSONObject jsonbody = new JSONObject();
        try{
            POSTAPIRequest getapiRequest=new POSTAPIRequest();
            jsonbody.put("pickup_point_id",selectedpickup.getPlaceid());
            jsonbody.put("lat",selecteddrop.getLatitude());
            jsonbody.put("lng",selecteddrop.getLongitude());
            jsonbody.put("location",selecteddrop.getLocationname()+","+selecteddrop.getLocationaddress());
            jsonbody.put("geofence_radius","100");
            jsonbody.put("phone",mobile.getText().toString().trim());
            if(message.getText().toString().trim().length()>0){
                jsonbody.put("message",message.getText().toString().trim());
            }
            String url = URLs.BASE_URL+URLs.CREATE_BOOKING;
            Log.i("url", String.valueOf(url));
            String token = SessionManagement.getUserToken(this);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(this.getApplicationContext(),createBookingListener,url,headparam,jsonbody);
        }catch (Exception e){
            e.printStackTrace();
            create.setClickable(true);
        }
    }

    FetchDataListener createBookingListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            try{
                if(response!=null){
                    if(response.getInt("error")==0){
                        Toast.makeText(BookingForm.this,"Booking Created",Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                create.setClickable(true);
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            RequestQueueService.showAlert(msg, BookingForm.this);
            create.setClickable(true);
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
