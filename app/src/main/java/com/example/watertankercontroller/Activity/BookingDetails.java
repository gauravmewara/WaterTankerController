package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.watertankercontroller.Modal.BookingModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.GETAPIRequest;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.POSTAPIRequest;
import com.example.watertankercontroller.Utils.RequestQueueService;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;
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
import org.w3c.dom.Text;

import java.util.ArrayList;

public class BookingDetails extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    TextView bookingid,distance,pickup,drop,drivername,contact_no,message,pagetitle,tvmapshow,otp,workorders;
    ImageView calltous,ivmapshow;
    ScrollView scrollview;
    RelativeLayout menuback;
    String init_type,bookingidval;
    SupportMapFragment mapFragment;
    RelativeLayout maplayout,mapshowlayout;
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
        Log.i("Booking Details:",init_type);
        bookingidval = getIntent().getExtras().getString("booking_id");
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        bookingid = (TextView)findViewById(R.id.tv_bookingdetail_bookingid);
        distance = (TextView)findViewById(R.id.tv_bookingdetail_distance);
        workorders=(TextView)findViewById(R.id.tv_bookingdetail_workorder);
        pickup = (TextView)findViewById(R.id.tv_bookingdetail_pickup);
        drop = (TextView)findViewById(R.id.tv_bookingdetail_drop);
        drivername = (TextView)findViewById(R.id.tv_bookingdetail_drivername);
        contact_no = (TextView)findViewById(R.id.tv_bookingdetail_contact);
        otp = (TextView)findViewById(R.id.tv_bookingdetail_otp);
        message = (TextView)findViewById(R.id.tv_bookingdetail_message);
        calltous = (ImageView)findViewById(R.id.iv_bookingdetail_bookingid_call);
        calltous.setOnClickListener(this);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        scrollview = (ScrollView)findViewById(R.id.scroll_booking_details);
        maplayout = (RelativeLayout)findViewById(R.id.rl_bookingdetail_map);
        mapshowlayout = (RelativeLayout)findViewById(R.id.rl_booking_details_showmap);
        mapshowlayout.setOnClickListener(this);
        tvmapshow = (TextView)findViewById(R.id.tv_booking_details_showmap);
        ivmapshow = (ImageView)findViewById(R.id.iv_booking_details_showmap);
        context = this;
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fg_booking_map);
        pagetitle.setText("Booking Details");
        if(init_type.equals("notification")){
            readNotificationApiCall(getIntent().getExtras().getString("notification_id"));
            NotificationManager nm = (NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            nm.cancelAll();
        }
        hideMapBar();
        getBookingDetails();
    }

    public void showMapBar(){mapshowlayout.setVisibility(View.VISIBLE);}

    public void hideMapBar(){mapshowlayout.setVisibility(View.GONE);}
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.iv_bookingdetail_bookingid_call:
                if(bmod!=null) {
                    String phone = "+91" + bmod.getPhone();
                    intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                    startActivity(intent);
                }
                break;
            case R.id.rl_booking_details_showmap:
                if(maplayout.getVisibility()==View.VISIBLE){
                    maplayout.setVisibility(View.GONE);
                    scrollview.setVisibility(View.VISIBLE);
                    tvmapshow.setText("Show Map");
                    ivmapshow.setImageResource(R.drawable.ic_plus);
                }else{
                    scrollview.setVisibility(View.GONE);
                    maplayout.setVisibility(View.VISIBLE);
                    tvmapshow.setText("Hide Map");
                    ivmapshow.setImageResource(R.drawable.ic_minus);
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
            getapiRequest.request(this,getBookingListener,url,headparam);
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
                            bmod.setId(jsonObject.getString("_id"));
                            if(jsonObject.has("path"))
                                bmod.setPath(jsonObject.getString("path"));
                            bmod.setBookingid(jsonObject.getString("booking_id"));
                            bookingid.setText(bmod.getBookingid());
                            bmod.setPhonecode(jsonObject.getString("phone_country_code"));
                            bmod.setMessage(jsonObject.getString("message"));
                            message.setText(bmod.getMessage());
                            bmod.setFromtime(jsonObject.getString("trip_start_at"));
                            bmod.setTotime(jsonObject.getString("trip_end_at"));
                            bmod.setContractor_id(jsonObject.getString("contractor_id"));
                            workorders.setText(bmod.getContractor_id());
                            bmod.setPickuppointid(jsonObject.getString("pickup_point_id"));
                            bmod.setControllerid(jsonObject.getString("controller_id"));
                            bmod.setPhone(jsonObject.getString("phone"));
                            contact_no.setText("+"+bmod.getPhonecode()+"-"+bmod.getPhone());
                            bmod.setOtp(jsonObject.getString("otp"));
                            otp.setText(bmod.getOtp());
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
                                if(!snapstring.equals("")) {
                                    JSONObject snap = new JSONObject(snapstring);
                                    JSONArray snaparray = null;
                                    if (snap.has("snappedPoints")) {
                                        snaparray = snap.getJSONArray("snappedPoints");
                                        if (finalpath == null)
                                            finalpath = new ArrayList<>();
                                        if (snaparray != null) {
                                            for (int i = 0; i < snaparray.length(); i++) {
                                                JSONObject point = snaparray.getJSONObject(i);
                                                JSONObject location = point.getJSONObject("location");
                                                double lat = Double.parseDouble(location.getString("latitude"));
                                                double longi = Double.parseDouble(location.getString("longitude"));
                                                LatLng temp = new LatLng(lat, longi);
                                                finalpath.add(temp);
                                            }
                                        }
                                    }
                                    mapFragment.getMapAsync(BookingDetails.this);
                                }
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
            RequestQueueService.showAlert(msg, BookingDetails.this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        showMapBar();
        LatLng pickupLatLng,dropLatLng;
        if(finalpath!=null) {
            PolylineOptions op = new PolylineOptions();
            op.addAll(finalpath);
            op.width(30);
            op.color(ContextCompat.getColor(BookingDetails.this, R.color.Green2));
            mMap.addPolyline(op);
            pickupLatLng = finalpath.get(0);
            dropLatLng = finalpath.get(finalpath.size() - 1);
        }else{
            pickupLatLng = new LatLng(Double.parseDouble(bmod.getFromlatitude()),Double.parseDouble(bmod.getFromlongitude()));
            dropLatLng = new LatLng(Double.parseDouble(bmod.getTolatitude()),Double.parseDouble(bmod.getTolongitude()));
        }
        MarkerOptions pickupop,dropop;
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

    public void readNotificationApiCall(String notificationId){
        try {
            POSTAPIRequest getapiRequest = new POSTAPIRequest();
            String url = URLs.BASE_URL + URLs.READ_NOTIFICATION+notificationId;
            String token = SessionManagement.getUserToken(this);
            Log.i("Token:",token);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(BookingDetails.this,readListener,url,headparam);
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
                        String count = data.getString("unread_count");
                        SessionManagement.setNotificationCount(BookingDetails.this,count);
                        SharedPrefUtil.setPreferences(BookingDetails.this,Constants.SHARED_PREF_NOTICATION_TAG,Constants.SHARED_NOTIFICATION_COUNT_KEY,count);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFetchFailure(String msg) {Log.e("Read Error",msg);}

        @Override
        public void onFetchStart() {

        }
    };
}
