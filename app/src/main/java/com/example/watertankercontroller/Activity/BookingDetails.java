package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.Modal.BookingModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.GETAPIRequest;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.RequestQueueService;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;
import com.example.watertankercontroller.fcm.Config;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BookingDetails extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    TextView bookingid,distance,pickup,drop,drivername,contact_no,message,pagetitle;
    ImageView calltous;

    RelativeLayout menuback;
    String init_type,bookingidval;
    SupportMapFragment mapFragment;
    RelativeLayout maplayout;
    RelativeLayout toolbar_notification,noticountlayout;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    TextView notiCount;
    static String notificationCount;
    ArrayList<LatLng>finalpath = null;
    static Context context;
    BookingModal bmod;
    GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        init_type = getIntent().getExtras().getString("init_type");
        bookingidval = getIntent().getExtras().getString("booking_id");
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        bookingid = (TextView)findViewById(R.id.tv_bookingdetail_bookingid);
        distance = (TextView)findViewById(R.id.tv_bookingdetail_distance);
        pickup = (TextView)findViewById(R.id.tv_bookingdetail_pickup);
        drop = (TextView)findViewById(R.id.tv_bookingdetail_drop);
        drivername = (TextView)findViewById(R.id.tv_bookingdetail_drivername);
        contact_no = (TextView)findViewById(R.id.tv_bookingdetail_contact);
        message = (TextView)findViewById(R.id.tv_bookingdetail_message);
        calltous = (ImageView)findViewById(R.id.iv_bookingdetail_bookingid_call);
        calltous.setOnClickListener(this);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        maplayout = (RelativeLayout)findViewById(R.id.rl_bookingdetail_map);
        toolbar_notification = (RelativeLayout) findViewById(R.id.rl_toolbar2_notification_view);
        toolbar_notification.setOnClickListener(this);
        noticountlayout = (RelativeLayout)findViewById(R.id.rl_toolbar2_notificationcount);
        notiCount = (TextView)findViewById(R.id.tv_toolbar2_notificationcount);
        context = this;
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fg_booking_map);
        /*if(init_type.equals(Constants.COMPLETED_CALL)){
            pagetitle.setText("Completed Booking Details");
        }else if(init_type.equals(Constants.ABORTED_CALL)){
            pagetitle.setText("Aborted Booking Details");
        }else if(init_type.equals(Constants.PENDING_CALL)){
            pagetitle.setText("Pending Booking Details");
        }else{
            pagetitle.setText("Ongoing Booking Details");
        }*/

        pagetitle.setText("Booking Details");



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
                    int count = Integer.parseInt(SessionManagement.getNotificationCount(BookingDetails.this));
                    setNotificationCount(count+1,false);
                }
            }
        };
        getBookingDetails();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.rl_toolbar2_notification_view:
                intent = new Intent(BookingDetails.this,NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_bookingdetail_bookingid_call:
                if(bmod!=null) {
                    String phone = "+91" + bmod.getPhone();
                    intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                    startActivity(intent);
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void getBookingDetails(){
        try{
            GETAPIRequest getapiRequest=new GETAPIRequest();
            String url = URLs.BASE_URL+URLs.BOOKING_DETAILS+bookingidval;
            Log.i("url", String.valueOf(url));
            String token = SessionManagement.getUserToken(this);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(this.getApplicationContext(),getBookingListener,url,headparam);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    FetchDataListener getBookingListener=new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            //RequestQueueService.cancelProgressDialog();
            try {
                if (response != null) {
                    if (response.getInt("error")==0) {
                        JSONObject jsonObject = response.getJSONObject("data");
                        if(jsonObject!=null) {
                            bmod = new BookingModal();
                            bmod.setBookingid(jsonObject.getString("_id"));
                            bookingid.setText(bmod.getBookingid());
                            bmod.setPhonecode(jsonObject.getString("phone_country_code"));
                            bmod.setMessage(jsonObject.getString("message"));
                            message.setText(bmod.getMessage());
                            bmod.setFromtime(jsonObject.getString("trip_start_at"));
                            bmod.setTotime(jsonObject.getString("trip_end_at"));
                            bmod.setPickuppointid(jsonObject.getString("pickup_point_id"));
                            bmod.setControllerid(jsonObject.getString("controller_id"));
                            bmod.setPhone(jsonObject.getString("phone"));
                            contact_no.setText("+"+bmod.getPhonecode()+"-"+bmod.getPhone());
                            bmod.setBookedby(jsonObject.getString("booked_by"));
                            bmod.setDrivername("No Driver Name in Response");
                            drivername.setText(bmod.getDrivername());
                            bmod.setController_name(jsonObject.getString("controller_name"));

                            JSONObject distance1 = jsonObject.getJSONObject("distance");
                            bmod.setDistance(distance1.getString("text"));
                            distance.setText(bmod.getDistance());

                            JSONObject droppoint = jsonObject.getJSONObject("drop_point");
                            bmod.setTolocation(droppoint.getString("address").trim());
                            bmod.setTolongitude(droppoint.getJSONObject("geometry").getJSONArray("coordinates").getString(0));
                            bmod.setTolatitude(droppoint.getJSONObject("geometry").getJSONArray("coordinates").getString(1));
                            drop.setText(bmod.getTolocation());

                            JSONObject pickupoint = jsonObject.getJSONObject("pickup_point");
                            bmod.setFromlocation(pickupoint.getString("address").trim());
                            bmod.setFromlongitude(pickupoint.getJSONObject("geometry").getJSONArray("coordinates").getString(0));
                            bmod.setFromlatitude(pickupoint.getJSONObject("geometry").getJSONArray("coordinates").getString(1));
                            pickup.setText(bmod.getFromlocation());
                            if(jsonObject.has("snapped_path")){
                                String snapstring = jsonObject.getString("snapped_path");
                                JSONObject snap = new JSONObject(snapstring);
                                JSONArray snaparray = snap.getJSONArray("snappedpoints");
                                if(finalpath == null)
                                    finalpath = new ArrayList<>();
                                for(int i=0;i<snaparray.length();i++){
                                    JSONObject point = snaparray.getJSONObject(i);
                                    JSONObject location = point.getJSONObject("location");
                                    double lat = Double.parseDouble(location.getString("latitude"));
                                    double longi = Double.parseDouble(location.getString("longitude"));
                                    LatLng temp = new LatLng(lat,longi);
                                    finalpath.add(temp);
                                }
                                mapFragment.getMapAsync(BookingDetails.this);
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            //RequestQueueService.cancelProgressDialog();
            RequestQueueService.showAlert(msg,BookingDetails.this);
        }

        @Override
        public void onFetchStart() {

            //RequestQueueService.showProgressDialog(Login.this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        maplayout.setVisibility(View.VISIBLE);
        PolylineOptions op = new PolylineOptions();
        op.addAll(finalpath);
        op.width(30);
        op.color(ContextCompat.getColor(BookingDetails.this,R.color.Green2));
        mMap.addPolyline(op);
        LatLng pickupLatLng = finalpath.get(0);
        LatLng dropLatLng = finalpath.get(finalpath.size()-1);
        MarkerOptions pickupop,dropop,currentop;
        pickupop = new MarkerOptions()
                .position(pickupLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
        dropop = new MarkerOptions()
                .position(dropLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.droppoint_create));
        mMap.addMarker(pickupop);
        mMap.addMarker(dropop);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 15));


    }
}
