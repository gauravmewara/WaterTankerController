package com.example.watertankercontroller.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.Modal.BookingModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchURL;
import com.example.watertankercontroller.Utils.PointsParser;
import com.example.watertankercontroller.Utils.RequestQueueService;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.TaskLoadedCallback;
import com.example.watertankercontroller.Utils.URLs;
import com.example.watertankercontroller.fcm.Config;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class OngoingMapActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback,TaskLoadedCallback {
    RelativeLayout menuback;
    RelativeLayout toolbar_notification,noticountlayout;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    TextView notiCount;
    static String notificationCount;
    Polyline currentPolyline;
    static Context context;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    static ArrayList<LatLng> waypoints = null;
    ArrayList<String> allpermissionsrequired;
    boolean permissionGranted = false;
    private GoogleApiClient mGoogleApiClient;
    private LatLng pickupLatLng=null,dropLatLng=null;
    public  LatLng currentlatlng=null;
    boolean fromBuildMethod = false;
    TextView pagetitle,pickuplocation,pickupaddress,droplocation,dropaddress;
    BookingModal blmod;
    Marker pickupMarker,dropMarker,currentMarker=null;
    MarkerOptions currentop;
    private Socket mSocket;
    long distance=0,duration=0;
    ArrayList<LatLng> mapRoute=null;
    String directionMode = "driving";
    String prevpath = "";
    boolean isLocationInProcess = false;
    boolean isMapReady = false;
    BitmapDescriptor smallTankerIcon;
    boolean pathCreated = false;
    ArrayList<LatLng> finalpath = null;
    boolean booking_end = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_map);
        Bundle b = getIntent().getExtras();
        blmod = b.getParcelable("Bookingdata");
        menuback = (RelativeLayout)findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        toolbar_notification = (RelativeLayout)findViewById(R.id.rl_toolbar2_notification_view);
        toolbar_notification.setOnClickListener(this);
        notiCount = (TextView)findViewById(R.id.tv_toolbar2_notificationcount);
        noticountlayout = (RelativeLayout)findViewById(R.id.rl_toolbar2_notificationcount);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pagetitle.setText(Constants.MAP_PAGE_TITLE);
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fg_ongoingMap_map);
        context = this;
        pickuplocation = (TextView)findViewById(R.id.tv_ongoingMap_pickuplocation);
        pickupaddress = (TextView)findViewById(R.id.tv_ongoingMap_pickupaddress);
        droplocation = (TextView)findViewById(R.id.tv_ongoingMap_droplocation);
        dropaddress = (TextView)findViewById(R.id.tv_ongoingMap_dropaddress);
        ArrayList<String> pickuppart,droppart;
        pickuppart = setLocation(blmod.getFromlocation());
        droppart = setLocation(blmod.getTolocation());
        pickuplocation.setText(pickuppart.get(0));
        pickupaddress.setText(pickuppart.get(1));
        droplocation.setText(droppart.get(0));
        dropaddress.setText(droppart.get(1));
        allpermissionsrequired = new ArrayList<>();
        allpermissionsrequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        allpermissionsrequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
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
                    int count = Integer.parseInt(SessionManagement.getNotificationCount(OngoingMapActivity.this));
                    setNotificationCount(count+1,false);
                }
            }
        };
        Bitmap bit = BitmapFactory.decodeResource(getResources(), R.drawable.tenklocation_map);
        Bitmap smallTanker = Bitmap.createScaledBitmap(bit, 70, 70, false);
        smallTankerIcon = BitmapDescriptorFactory.fromBitmap(smallTanker);
        initSocket();
        mapFragment.getMapAsync(OngoingMapActivity.this);
        //checkAndRequestPermissions(this,allpermissionsrequired);
    }

    public void initSocket(){
        try{
            mSocket = IO.socket(URLs.SOCKET_URL+SessionManagement.getUserToken(OngoingMapActivity.this));
        }catch (URISyntaxException e){
            e.printStackTrace();
        }
        mSocket.on("locationUpdate:Booking",onLocationUpdate);
        mSocket.on("aborted:Booking",onBookingAborted);
        mSocket.on("end:Booking",onBookingEnd);
        mSocket.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(permissionGranted){
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMyLocationEnabled(false);
                mMap.setTrafficEnabled(false);
                mMap.setIndoorEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
            }
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setMyLocationEnabled(false);
            mMap.setTrafficEnabled(false);
            mMap.setIndoorEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }*/
        pickupLatLng = new LatLng(Double.parseDouble(blmod.getFromlatitude()),Double.parseDouble(blmod.getFromlongitude()));
        dropLatLng = new LatLng(Double.parseDouble(blmod.getTolatitude()),Double.parseDouble(blmod.getTolongitude()));
        MarkerOptions pickupop,dropop,currentop;
        pickupop = new MarkerOptions()
                .position(pickupLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
        dropop = new MarkerOptions()
                .position(dropLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.droppoint_create));
        /*currentop = new MarkerOptions()
                .position(pickupLatLng)
                .flat(true)
                .alpha(.6f)
                .anchor(0.5f,0.5f)
                .rotation(90)
                .icon(smallTankerIcon);*/
        pickupMarker = mMap.addMarker(pickupop);
        dropMarker = mMap.addMarker(dropop);
        //currentMarker = mMap.addMarker(currentop);
        JSONObject emitparam = new JSONObject();
        JSONObject refreshParam = new JSONObject();
        try {
            emitparam.put("booking_id", blmod.getBookingid());
            refreshParam.put("id", blmod.getBookingid());
        }catch (JSONException e){
            e.printStackTrace();
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 17));
        isMapReady = true;
        //new FetchURL(OngoingMapActivity.this).execute(getUrl(pickupLatLng, dropLatLng, "driving"), "driving");
        mSocket.emit("subscribe:Booking",emitparam);
        mSocket.emit("refreshLocation:Booking",refreshParam);
    }


    private Emitter.Listener onLocationUpdate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            OngoingMapActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject response = (JSONObject)args[0];
                    try {
                        String id = response.getString("id");
                        if(id.equals(blmod.getBookingid())) {
                            if (!isLocationInProcess) {
                                //mSocket.off("locationUpdate:Booking", onLocationUpdate);
                                isLocationInProcess = true;
                                String lat = response.getString("lat");
                                String lng = response.getString("lng");
                                Float bearing = Float.parseFloat(response.getString("bearing"));
                                String path = "";
                                if (response.has("path"))
                                    path = response.getString("path");
                                currentlatlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                                if (currentMarker != null)
                                    currentMarker.remove();
                                currentop = new MarkerOptions()
                                        .position(currentlatlng)
                                        .flat(true)
                                        .alpha(.8f)
                                        .anchor(0.5f, 0.5f)
                                        .rotation(90 + bearing)
                                        .icon(smallTankerIcon);
                                currentMarker = mMap.addMarker(currentop);
                                if (path != "" && !pathCreated) {
                                    prevpath = path;
                                    PointsParser parserTask = new PointsParser(OngoingMapActivity.this, directionMode);
                                    parserTask.execute(path);
                                } else {
                                    //mSocket.on("locationUpdate:Booking", onLocationUpdate);
                                    isLocationInProcess = false;
                                }

                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        isLocationInProcess = false;
                    }

                }
            });
        }
    };


    private Emitter.Listener onBookingAborted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            OngoingMapActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject response = (JSONObject)args[0];
                    try{
                        String id = response.getString("id");
                        if(id.equals(blmod.getBookingid())) {
                            mSocket.off("locationUpdate:Booking",onLocationUpdate);
                            mSocket.off("aborted:Booking",onBookingAborted);
                            mSocket.off("end:Booking",onBookingEnd);
                            mSocket.disconnect();
                            RequestQueueService.showAlert("","Booking Cancelled",OngoingMapActivity.this);
                            booking_end = true;
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onBookingEnd = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            OngoingMapActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject response = (JSONObject)args[0];
                    try{
                        String id = response.getString("id");
                        if(id.equals(blmod.getBookingid())) {
                            mSocket.off("locationUpdate:Booking",onLocationUpdate);
                            mSocket.off("aborted:Booking",onBookingAborted);
                            mSocket.off("end:Booking",onBookingEnd);
                            mSocket.disconnect();
                            JSONObject snap = response.getJSONObject("snapped_path");
                            String distance_travelled = response.getString("distance_travelled");
                            JSONArray snaparray = snap.getJSONArray("snapped_points");
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
                            PolylineOptions op = new PolylineOptions();
                            op.width(30);
                            op.color(ContextCompat.getColor(OngoingMapActivity.this,R.color.Green2));
                            op.addAll(finalpath);
                            if(currentPolyline!=null)
                                currentPolyline.remove();
                            //int size = values.length;
                            currentPolyline = mMap.addPolyline(op);
                            RequestQueueService.showAlert("","Trip finished",OngoingMapActivity.this);
                            booking_end = true;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(OngoingMapActivity.this,"Error In Location End Listener",Toast.LENGTH_LONG);
                    }
                }
            });
        }
    };

    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyline!=null)
            currentPolyline.remove();
        //int size = values.length;
        currentPolyline = mMap.addPolyline((PolylineOptions)values[0]);
        distance = (long)values[1];
        duration = (long)values[2];
        mapRoute = (ArrayList<LatLng>) values[3];
        pathCreated = true;
        isLocationInProcess = false;
        //mSocket.on("locationUpdate:Booking",onLocationUpdate);
    }







    /*public void checkAndRequestPermissions(Activity activity, ArrayList<String> permissions) {
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), Constants.MULTIPLE_PERMISSIONS_REQUEST_CODE);
        }else{
            permissionGranted = true;
            buildGoogleApiClient();
            //createPickUpLocations();
        }
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Constants.MULTIPLE_PERMISSIONS_REQUEST_CODE:
                if(grantResults.length>0){
                    for(int i=0;i<grantResults.length;i++){
                        permissionGranted = true;
                        if(!(grantResults[i]==PackageManager.PERMISSION_GRANTED)){
                            permissionGranted = false;
                            break;
                        }
                    }
                    if(permissionGranted){
                        buildGoogleApiClient();
                        //createPickUpLocations();
                    }else{
                        checkAndRequestPermissions(OngoingMapActivity.this,allpermissionsrequired);
                    }
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/




    public ArrayList<String> setLocation(String address){
        ArrayList<String> addresspart = new ArrayList<>();
        int count = (address.split(Pattern.quote(","),-1).length)-1;
        int splitindex;
        if(count>0) {
            splitindex = count > 3 ? address.indexOf(',', address.indexOf(',') + 1) : address.indexOf(',');
            addresspart.add(address.substring(0, splitindex - 1));
            addresspart.add(address.substring(splitindex + 1).trim());
        }else{
            addresspart.add(address);
            addresspart.add("");
        }
        return addresspart;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.rl_toolbar2_notification_view:
                Intent intent;
                intent = new Intent(OngoingMapActivity.this,NotificationActivity.class);
                startActivity(intent);
                break;
        }
    }

    /*@Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!permissionGranted) {
            return;
        }
        if(fromBuildMethod){
            //startLocationUpdates();
            fromBuildMethod = false;
            mapFragment.getMapAsync(OngoingMapActivity.this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("PICKUP ACTIVITY:", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("PICKUP ACTIVITY:", "Connection failed. Error: " + connectionResult.getErrorCode());
    }*/



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

    /*protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        fromBuildMethod = true;
        mGoogleApiClient.connect();
    }*/

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
    protected void onStart() {
        super.onStart();
        if (!permissionGranted) {
            return;
        }
        /*if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }*/
    }

    @Override
    protected void onStop() {
        /*if(mGoogleApiClient!=null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }*/
        super.onStop();
    }

    public boolean isCurrentLocationSame(LatLng newpos){
        if(currentlatlng.latitude == newpos.latitude && currentlatlng.longitude==newpos.longitude){
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        mSocket.off("locationUpdate:Booking",onLocationUpdate);
        mSocket.off("aborted:Booking",onBookingAborted);
        mSocket.off("end:Booking",onBookingEnd);
        mSocket.disconnect();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(booking_end) {
            Intent newIntent = new Intent(this, BookingStatus.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(newIntent);
            finish();
        }else {
            super.onBackPressed();
        }
    }
}


//line no. 441, 444, 538