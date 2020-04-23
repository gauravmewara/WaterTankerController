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
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class OngoingMapActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TaskLoadedCallback {
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
    public static LatLng currentlatlng=null;
    boolean fromBuildMethod = false;
    TextView pagetitle,pickuplocation,pickupaddress,droplocation,dropaddress;
    BookingModal blmod;
    Marker pickupMarker,dropMarker,currentMarker=null;
    MarkerOptions currentop;
    private Socket mSocket;
    long distance=0,duration=0;
    ArrayList<LatLng> mapRoute=null;

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
        initSocket();
        checkAndRequestPermissions(this,allpermissionsrequired);
    }

    public void initSocket(){
        try{
            mSocket = IO.socket(URLs.SOCKET_URL+SessionManagement.getUserToken(OngoingMapActivity.this));
        }catch (URISyntaxException e){
            e.printStackTrace();
        }
        mSocket.on("locationUpdate:Booking",onLocationUpdate);
        mSocket.on("aborted:Booking",onBookingAborted);
        mSocket.connect();
    }
    public void checkAndRequestPermissions(Activity activity, ArrayList<String> permissions) {
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
    }

    @Override
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
    }




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

    @Override
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
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
        }
        pickupLatLng = new LatLng(Double.parseDouble(blmod.getFromlatitude()),Double.parseDouble(blmod.getFromlongitude()));
        dropLatLng = new LatLng(Double.parseDouble(blmod.getTolatitude()),Double.parseDouble(blmod.getTolongitude()));
        MarkerOptions pickupop,dropop,currentop;
        Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.tenklocation_map);
        Bitmap smallTanker = Bitmap.createScaledBitmap(b,5,5,false);
        BitmapDescriptor smallTankerIcon = BitmapDescriptorFactory.fromBitmap(smallTanker);
        pickupop = new MarkerOptions()
                .position(pickupLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
        dropop = new MarkerOptions()
                .position(dropLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.droppoint_create));
        currentop = new MarkerOptions()
                .position(pickupLatLng)
                .flat(true)
                .alpha(.6f)
                .anchor(0.5f,0.5f)
                .rotation(90)
                .icon(smallTankerIcon);
        pickupMarker = mMap.addMarker(pickupop);
        dropMarker = mMap.addMarker(dropop);
        currentMarker = mMap.addMarker(currentop);
        JSONObject emitparam = new JSONObject();
        JSONObject refreshParam = new JSONObject();
        try {
            emitparam.put("booking_id", blmod.getBookingid());
            refreshParam.put("id", blmod.getBookingid());
        }catch (JSONException e){
            e.printStackTrace();
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 17));
        //new FetchURL(OngoingMapActivity.this).execute(getUrl(pickupLatLng, dropLatLng, "driving"), "driving");
        mSocket.emit("subscribe:Booking",emitparam);
        mSocket.emit("refreshLocation:Booking",refreshParam);
    }

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

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        fromBuildMethod = true;
        mGoogleApiClient.connect();
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
    protected void onStart() {
        super.onStart();
        if (!permissionGranted) {
            return;
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient!=null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
        super.onStop();
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
                            String lat = response.getString("lat");
                            String lng = response.getString("lng");
                            LatLng temp = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                            Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.tenklocation_map);
                            Bitmap smallTanker = Bitmap.createScaledBitmap(b,70,70,false);
                            BitmapDescriptor smallTankerIcon = BitmapDescriptorFactory.fromBitmap(smallTanker);
                            boolean locationChanged = false;
                            if (currentlatlng == null) {
                                locationChanged = true;
                            } else {
                                if(isCurrentLocationSame(temp))
                                    locationChanged = false;
                                else
                                    locationChanged = true;
                            }
                            if (locationChanged) {
                                currentlatlng = temp;
                                if (isCurrentLocationSame(dropLatLng)) {
                                    mSocket.off("locationUpdate:Booking", onLocationUpdate);
                                }
                                currentop = new MarkerOptions()
                                        .position(pickupLatLng)
                                        .flat(true)
                                        .alpha(.8f)
                                        .anchor(0.5f,0.5f)
                                        .rotation(90)
                                        .icon(smallTankerIcon);
                                /*currentop = new MarkerOptions()
                                        .position(currentlatlng)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tenklocation_map));*/
                                if (currentMarker != null)
                                    currentMarker.remove();
                                currentMarker = mMap.addMarker(currentop);

                                if (mapRoute == null) {
                                    new FetchURL(OngoingMapActivity.this).execute(getUrl(pickupLatLng, dropLatLng, "driving"), "driving");

                                } else if (!PolyUtil.isLocationOnPath(currentlatlng, mapRoute, true, 10)) {
                                    if (waypoints == null)
                                        waypoints = new ArrayList<>();
                                    if (waypoints.size() >= 10)
                                        waypoints.remove(0);
                                    double lt = Double.parseDouble(String.format("%.4f", currentlatlng.latitude));
                                    double lg = Double.parseDouble(String.format("%.4f", currentlatlng.longitude));
                                    LatLng t = new LatLng(lt, lg);
                                    waypoints.add(t);
                                    new FetchURL(OngoingMapActivity.this).execute(getUrl(pickupLatLng, dropLatLng, "driving"), "driving");

                                }
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private Emitter.Listener onBookingAborted = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            OngoingMapActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    };

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;

        String waypoint = "&waypoints=";
        String parameters = "";
        try {
            if(waypoints!=null) {
                for (int i = 0; i < waypoints.size(); i++) {
                    LatLng temp = waypoints.get(i);
                    if (i == 0) {
                        waypoint = waypoint + "via:-" + temp.latitude + "%2C" + temp.longitude;
                    } else {
                        waypoint = waypoint + "%7Cvia:-" + temp.latitude + "%2C" + temp.longitude;
                    }
                }
                if (waypoints.size() <= 0) {
                    parameters = str_origin + "&" + str_dest + "&" + mode;
                } else {
                    parameters = str_origin + "&" + str_dest + "&" + waypoint + "&" + mode;
                }
            }else{
                parameters = str_origin + "&" + str_dest + "&" + mode;
            }
            // Building the parameters to the web service

        }catch (Exception e){
            e.printStackTrace();
            parameters = str_origin + "&" + str_dest + "&" + mode;
        }
        //String parameters = str_origin + "&" + str_dest + "&" + waypoint + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    public boolean isCurrentLocationSame(LatLng newpos){
        if(currentlatlng.latitude == newpos.latitude && currentlatlng.longitude==newpos.longitude){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyline!=null)
            currentPolyline.remove();
        //int size = values.length;
        currentPolyline = mMap.addPolyline((PolylineOptions)values[0]);
        distance = (long)values[1];
        duration = (long)values[2];
        mapRoute = (ArrayList<LatLng>) values[3];

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("locationUpdate:Booking",onLocationUpdate);
        mSocket.off("aborted:Booking",onBookingAborted);
    }
}
