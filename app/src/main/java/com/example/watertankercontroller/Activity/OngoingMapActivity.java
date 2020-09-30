package com.example.watertankercontroller.Activity;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
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
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
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
import com.example.watertankercontroller.Utils.NetworkUtils;
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
import java.util.List;
import java.util.regex.Pattern;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class OngoingMapActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback{
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
    boolean permissionGranted = false,socketInitialized=false;
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
    BitmapDescriptor smallTankerIcon,tank_icon;
    boolean pathCreated = false;
    ArrayList<LatLng> finalpath = null;
    boolean booking_end = false;
    private BroadcastReceiver networkStateReceiver;
    boolean networkstatechanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_map);
        Bundle b = getIntent().getExtras();
        blmod = b.getParcelable("Bookingdata");
        menuback = (RelativeLayout)findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
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
        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("Broadcast:","Network State");
                networkstatechanged = true;
                int status = NetworkUtils.getConnectivityStatus(getApplicationContext());
                if(status==NetworkUtils.NETWORK_INTERNET||status==NetworkUtils.WIFI_INTERNET){
                    Log.i("Network State:","Internet Available");
                    resetSocket();
                    networkstatechanged = false;
                }else{
                    Log.i("Network State:","No Internet");
                }
            }
        };
        Bitmap bit = BitmapFactory.decodeResource(getResources(), R.drawable.tenklocation_map);
        Bitmap smallTanker = Bitmap.createScaledBitmap(bit, 70, 70, false);
        tank_icon = bitmapDescriptorFromVector(this,R.drawable.ic_truck_icon);
        mapFragment.getMapAsync(OngoingMapActivity.this);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_truck_icon);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        //vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void initSocket(String id){
        try {
            mSocket = IO.socket(URLs.SOCKET_URL + SessionManagement.getUserToken(this));
            mSocket.connect();
            socketInitialized = true;
            Log.i("SOCKET TEST:","socket Initialized");
            mSocket.on("aborted:Booking", onBookingAborted);
            mSocket.on("end:Booking",onBookingEnd);
            mSocket.on("locationUpdate:Booking",onLocationUpdate);
            JSONObject params = new JSONObject();
            params.put("booking_id", id);
            mSocket.emit("subscribe:Booking", params);
        }catch (URISyntaxException e){
            e.printStackTrace();
            socketInitialized = false;
        }catch (JSONException e){
            e.printStackTrace();
            socketInitialized = false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        pickupLatLng = new LatLng(Double.parseDouble(blmod.getFromlatitude()),Double.parseDouble(blmod.getFromlongitude()));
        dropLatLng = new LatLng(Double.parseDouble(blmod.getTolatitude()),Double.parseDouble(blmod.getTolongitude()));
        MarkerOptions pickupop,dropop;
        pickupop = new MarkerOptions()
                .position(pickupLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
        dropop = new MarkerOptions()
                .position(dropLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.droppoint_create));
        pickupMarker = mMap.addMarker(pickupop);
        dropMarker = mMap.addMarker(dropop);
        String pathstring = blmod.getPath();
        if(pathstring!=null){
            if(!pathstring.equals("")){
                ArrayList<LatLng> path = (ArrayList<LatLng>)decodePoly(blmod.getPath());
                PolylineOptions pathop = new PolylineOptions();
                pathop.addAll(path);
                pathop.width(20);
                pathop.color(ContextCompat.getColor(OngoingMapActivity.this,R.color.Green2));
                currentPolyline = mMap.addPolyline(pathop);
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 17));
        isMapReady = true;
        initSocket(blmod.getId());
    }



    private Emitter.Listener onLocationUpdate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("Location Listener:","Location updating");
            OngoingMapActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject response = (JSONObject)args[0];
                    try {
                        String id = response.getString("id");
                        Log.i("listener booking id:",id);
                        if(id.equals(blmod.getId())) {
                            if (!isLocationInProcess) {
                                isLocationInProcess = true;
                                String lat = response.getString("lat");
                                String lng = response.getString("lng");
                                currentlatlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                                if (currentMarker != null)
                                    currentMarker.remove();
                                currentop = new MarkerOptions()
                                        .position(currentlatlng)
                                        .flat(true)
                                        .alpha(.8f)
                                        .anchor(0.5f, 0.0f)
                                        .icon(tank_icon);
                                currentMarker = mMap.addMarker(currentop);
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 22));
                                isLocationInProcess = false;
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
            Log.i("Location Listener:","Location Aborted");
            OngoingMapActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject response = (JSONObject)args[0];
                    try{
                        String id = response.getString("id");
                        Log.i("listener booking id:",id);
                        if(id.equals(blmod.getId())) {
                            closeSocket();
                            abortAlert("Booking Cancelled",OngoingMapActivity.this);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        abortAlert("Booking Cancelled",OngoingMapActivity.this);
                    }catch (Exception e){
                        e.printStackTrace();
                        abortAlert("Booking Cancelled",OngoingMapActivity.this);
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
                        Log.i("listener booking id:",id);
                        if(id.equals(blmod.getId())) {
                            closeSocket();
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
                            currentPolyline = mMap.addPolyline(op);
                            abortAlert("Trip finished",OngoingMapActivity.this);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(OngoingMapActivity.this,"Error In Location End Listener",Toast.LENGTH_LONG);
                        abortAlert("Trip finished",OngoingMapActivity.this);
                    }
                }
            });
        }
    };

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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(networkStateReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (!permissionGranted) {
            return;
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkStateReceiver);
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
        closeSocket();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public void resetSocket(){
        closeSocket();
        initSocket(blmod.getId());
    }

    public void closeSocket(){
        if(mSocket!=null){
            Log.i("Socket:","Disconnecting");
            mSocket.off("aborted:Booking", onBookingAborted);
            mSocket.off("end:Booking",onBookingEnd);
            mSocket.off("locationUpdate:Booking",onLocationUpdate);
            mSocket.disconnect();
            mSocket.close();
            mSocket=null;
        }
    }

    private void abortAlert(final String message, final FragmentActivity context) {
        try {
            new Thread()
            {
                public void run()
                {
                    OngoingMapActivity.this.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            //Do your UI operations like dialog opening or Toast here
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                            builder.setTitle("Alert!");
                            builder.setMessage(message);
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    /*Intent intent = new Intent(OngoingMapActivity.this, OngoingActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();*/
                                    onBackPressed();
                                }
                            });
                            builder.show();
                        }
                    });
                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}