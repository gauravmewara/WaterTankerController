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
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.Modal.PickupPlaceModal;
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
import com.example.watertankercontroller.fcm.Config;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PickupActivity extends AppCompatActivity implements View.OnClickListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
    ImageView droppin;

    RelativeLayout menuback;
    RelativeLayout toolbar_notification,noticountlayout;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    TextView notiCount;
    static String notificationCount;
    static Context context;

    RelativeLayout pickupview,dropview,confirmview;
    ArrayList<PickupPlaceModal> pickupplacelist;
    TextView pickuplocation,pickupaddress,pagetitle,droplocation,dropaddress;
    PickupPlaceModal selectedPickupLocation = null,selectedDropLocation=null;
    boolean pickupselected =false,fromBuildMethod=false,dropselected=false;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    public int previousindex,selectedindex;
    ArrayList<String> allpermissionsrequired;
    boolean permissionGranted = false;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 20000; /* 20 sec */
    private LocationManager locationManager;
    ArrayList<Marker> markerlist;
    private LatLng currentlatlng=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup);
        if(getIntent().hasExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE)) {
            selectedPickupLocation = getIntent().getParcelableExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
            pickupselected = true;
        }
        if(getIntent().hasExtra(Constants.DROP_LOCATION_INTENT_DATA_TITLE)) {
            selectedDropLocation = getIntent().getParcelableExtra(Constants.DROP_LOCATION_INTENT_DATA_TITLE);
            dropselected = true;
        }
        allpermissionsrequired = new ArrayList<>();
        allpermissionsrequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        allpermissionsrequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        markerlist = new ArrayList<>();
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);

        toolbar_notification = (RelativeLayout) findViewById(R.id.rl_toolbar2_notification_view);
        toolbar_notification.setOnClickListener(this);
        noticountlayout = (RelativeLayout)findViewById(R.id.rl_toolbar2_notificationcount);
        notiCount = (TextView)findViewById(R.id.tv_toolbar2_notificationcount);
        context = this;


        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pagetitle.setText(Constants.MAP_PAGE_TITLE);
        pickupview = (RelativeLayout)findViewById(R.id.rl_pickup_view);
        pickupview.setOnClickListener(this);
        pickupview.setClickable(false);
        dropview = (RelativeLayout)findViewById(R.id.rl_pickupactivity_drop_view);
        dropview.setOnClickListener(this);
        dropview.setClickable(false);
        droppin = (ImageView)findViewById(R.id.iv_pickupactivity_drop_pin);
        confirmview = (RelativeLayout)findViewById(R.id.rl_pickupacitvity_confirm);
        confirmview.setOnClickListener(this);
        confirmview.setClickable(false);
        pickuplocation = (TextView)findViewById(R.id.tv_pickup_pickuplocation);
        pickupaddress = (TextView)findViewById(R.id.tv_pickup_pickupaddress);
        droplocation = (TextView)findViewById(R.id.tv_pickupactivity_droplocation);
        dropaddress = (TextView)findViewById(R.id.tv_pickupactivity_dropaddress);
        if(pickupselected){
            pickuplocation.setText(selectedPickupLocation.getLocationname());
            pickupaddress.setText(selectedPickupLocation.getLocationaddress());
        }
        if(dropselected){
            droplocation.setText(selectedDropLocation.getLocationname());
            dropaddress.setText(selectedDropLocation.getLocationaddress());
        }
        if(pickupselected && dropselected){
            confirmview.setClickable(true);
        }
        Places.initialize(getApplicationContext(),Constants.MAP_API_KEY);
        PlacesClient placesClient = Places.createClient(this);
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
                    int count = Integer.parseInt(SessionManagement.getNotificationCount(PickupActivity.this));
                    setNotificationCount(count+1,false);
                }
            }
        };
        checkAndRequestPermissions(this,allpermissionsrequired);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.rl_toolbar2_notification_view:
                intent = new Intent(PickupActivity.this,NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_pickup_view:
                intent = new Intent(PickupActivity.this,PickUpLocations.class);
                intent.putParcelableArrayListExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE,pickupplacelist);
                startActivityForResult(intent, Constants.PICKUP_ACTIVITY_PICKUP_LOCATION_REQUEST_CODE);
                break;
            case R.id.rl_pickupactivity_drop_view:
                Toast.makeText(PickupActivity.this,"Google Places Api is temporarily disabled",Toast.LENGTH_LONG).show();
                droppin.setVisibility(View.VISIBLE);
                mMap.setOnCameraIdleListener(this);
                break;
            case R.id.rl_pickupacitvity_confirm:
                if(!(dropselected && pickupselected)){
                    Toast.makeText(PickupActivity.this,"Select Pickup and Drop Location",Toast.LENGTH_LONG).show();
                }else{
                    onBackPressed();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(pickupselected && dropselected) {
            Intent intent = new Intent();
            intent.putExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE, selectedPickupLocation);
            intent.putExtra(Constants.DROP_LOCATION_INTENT_DATA_TITLE,selectedDropLocation);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }else{
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }
        super.onBackPressed();
    }

    public void createPickUpLocations(){
        try{

            String token = SessionManagement.getUserToken(PickupActivity.this);
            GETAPIRequest pickuppointrequest=new GETAPIRequest();
            String url = URLs.BASE_URL+URLs.NEARBY_PICKUP_POINTS+"?lat="+currentlatlng.latitude+"&lng="+currentlatlng.longitude;
            Log.i("url",String.valueOf(url));
            Log.i("token",String.valueOf(token));
            HeadersUtil headparam = new HeadersUtil(token);
            pickuppointrequest.request(this, pickuppointrequestlistener,url,headparam);
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    FetchDataListener pickuppointrequestlistener=new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            //RequestQueueService.cancelProgressDialog();
            try {
                if (response != null) {
                    if (response.getInt("error")==0) {
                        JSONArray data = response.getJSONArray("data");
                        if(data!=null) {
                            pickupplacelist = new ArrayList<>();
                            for(int i=0;i<data.length();i++){
                                JSONObject tempjson = data.getJSONObject(i);
                                PickupPlaceModal mod = new PickupPlaceModal();
                                mod.setPlaceid(tempjson.getString("_id"));
                                JSONObject location = tempjson.getJSONObject("point");
                                String address = (location.getString("location"));
                                JSONObject geometry = location.getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");
                                mod.setLatitude(coordinates.getString(1));
                                mod.setLongitude(coordinates.getString(0));
                                //String address = mod.getLocationaddress();
                                int count = (address.split(Pattern.quote(","),-1).length)-1;
                                int splitindex;
                                if(count>0) {
                                    splitindex = count > 3 ? address.indexOf(',', address.indexOf(',') + 1) : address.indexOf(',');
                                    mod.setLocationname(address.substring(0, splitindex - 1));
                                    mod.setLocationaddress(address.substring(splitindex + 1).trim());
                                }else{
                                    mod.setLocationname(address);
                                    mod.setLocationaddress("");
                                }
                                pickupplacelist.add(mod);
                            }
                            mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fg_pickup_map);
                            mapFragment.getMapAsync(PickupActivity.this);
                        }
                        else {
                            RequestQueueService.showAlert("Error! No data fetched", PickupActivity.this);
                        }
                    }
                } else {
                    RequestQueueService.showAlert("Error! No data fetched", PickupActivity.this);
                }
            }catch (Exception e){
                RequestQueueService.showAlert("Something went wrong", PickupActivity.this);
                e.printStackTrace();
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            //RequestQueueService.cancelProgressDialog();
            RequestQueueService.showAlert(msg,PickupActivity.this);
        }

        @Override
        public void onFetchStart() {
            //RequestQueueService.showProgressDialog(Login.this);
        }
    };

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case Constants.PICKUP_ACTIVITY_PICKUP_LOCATION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedPickupLocation = data.getParcelableExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
                    int tempindex = -1;
                    LatLng newposition;
                    for (int i = 0; i < pickupplacelist.size(); i++) {
                        if (pickupplacelist.get(i).isLocationSame(selectedPickupLocation)) {
                            tempindex = i;
                            break;
                        }
                    }
                    if (pickupselected) {
                        if (tempindex != selectedindex && tempindex != -1) {
                            previousindex = selectedindex;
                            selectedindex = tempindex;
                            Marker prevMarker = markerlist.get(previousindex);
                            Marker newMarker = markerlist.get(selectedindex);
                            prevMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hydrents_map));
                            newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
                            markerlist.remove(previousindex);
                            markerlist.add(previousindex, prevMarker);
                            markerlist.remove(selectedindex);
                            markerlist.add(selectedindex, newMarker);
                            newposition = newMarker.getPosition();
                            pickuplocation.setText(selectedPickupLocation.getLocationname());
                            pickupaddress.setText(selectedPickupLocation.getLocationaddress());
                            pickupselected = true;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newposition, 14));
                        }
                    } else {
                        selectedindex = tempindex;
                        selectedPickupLocation = pickupplacelist.get(selectedindex);
                        Marker newMarker = markerlist.get(selectedindex);
                        newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
                        markerlist.remove(selectedindex);
                        markerlist.add(selectedindex, newMarker);
                        newposition = newMarker.getPosition();
                        pickuplocation.setText(selectedPickupLocation.getLocationname());
                        pickupaddress.setText(selectedPickupLocation.getLocationaddress());
                        pickupselected = true;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newposition, 14));
                    }
                }
                break;
            case Constants.GOOGLE_AUTOCOMPLETE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.i("Drop Place: ",place.getId()+" - "+ place.getName()+"-"+place.getAddress());
                    LatLng droplatLng = place.getLatLng();
                    selectedDropLocation = new PickupPlaceModal();
                    selectedDropLocation.setLocationname(place.getName());
                    selectedDropLocation.setLocationaddress(place.getAddress());
                    selectedDropLocation.setLatitude(String.valueOf(droplatLng.latitude));
                    selectedDropLocation.setLongitude(String.valueOf(droplatLng.longitude));
                    droplocation.setText(place.getName());
                    dropaddress.setText(place.getAddress());
                    dropselected = true;
                    droppin.setVisibility(View.VISIBLE);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(droplatLng, 14));
                    mMap.setOnCameraIdleListener(this);
                }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.i("DropActivity Status:",status.getStatusMessage());
                }else if(resultCode == RESULT_CANCELED){
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(permissionGranted){
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMyLocationEnabled(true);
                mMap.setTrafficEnabled(false);
                mMap.setIndoorEnabled(false);
                mMap.setBuildingsEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
            }
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(false);
            mMap.setIndoorEnabled(false);
            mMap.setBuildingsEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
        for (int i = 0; i < pickupplacelist.size(); i++) {
            LatLng place = new LatLng(Double.parseDouble(pickupplacelist.get(i).getLatitude()), Double.parseDouble(pickupplacelist.get(i).getLongitude()));
            //googleMap.addMarker(new MarkerOptions().position(place).title(placelist.get(i).getLocationname()));
            MarkerOptions markerop;
            if (pickupselected) {
                if (pickupplacelist.get(i).isLocationSame(selectedPickupLocation)) {
                    selectedindex = i;
                    markerop = new MarkerOptions()
                            .position(place)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
                } else {
                    markerop = new MarkerOptions()
                            .position(place)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.hydrents_map));
                }
            } else {
                markerop = new MarkerOptions()
                        .position(place)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hydrents_map));
            }
            Marker marker = googleMap.addMarker(markerop);
            marker.setTag(i);
            markerlist.add(marker);
        }
        dropview.setClickable(true);
        pickupview.setClickable(true);
        confirmview.setClickable(true);
        if(dropselected){
            LatLng drop = new LatLng(Double.parseDouble(selectedDropLocation.getLatitude()),Double.parseDouble(selectedDropLocation.getLongitude()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(drop, 12));
        }else if(pickupselected){
            LatLng pickup = new LatLng(Double.parseDouble(selectedPickupLocation.getLatitude()),Double.parseDouble(selectedPickupLocation.getLongitude()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12));
        }else{
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 12));
        }

    }


     // any code you want.

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
            checkLocation();
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
                        checkLocation();
                        buildGoogleApiClient();
                        //createPickUpLocations();
                    }else{
                        checkAndRequestPermissions(PickupActivity.this,allpermissionsrequired);
                    }
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public float distanceBetweenLocations(Location location1,Location location2){
        return location1.distanceTo(location2);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentlatlng = new LatLng(location.getLatitude(), location.getLongitude());
        if(currentlatlng!=null)
            createPickUpLocations();
        else
            Toast.makeText(PickupActivity.this,"Current location not fetched",Toast.LENGTH_LONG).show();
        if(mGoogleApiClient!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!permissionGranted) {
            return;
        }
        if(fromBuildMethod){
            startLocationUpdates();
            fromBuildMethod = false;
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

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
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

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (!permissionGranted) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onCameraIdle() {

        LatLng latLng = mMap.getCameraPosition().target;
        Geocoder geocoder = new Geocoder(PickupActivity.this);

        pickupview.setVisibility(View.VISIBLE);
        dropview.setVisibility(View.VISIBLE);
        confirmview.setVisibility(View.VISIBLE);
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                String locality = addressList.get(0).getAddressLine(0);
                String country = addressList.get(0).getCountryName();
                selectedDropLocation = new PickupPlaceModal();
                int count = (locality.split(Pattern.quote(","),-1).length)-1;
                int splitindex = count>3?locality.indexOf(',',locality.indexOf(',')+1):locality.indexOf(',');
                selectedDropLocation.setLocationname(locality.substring(0,splitindex-1).trim());
                selectedDropLocation.setLocationaddress(locality.substring(splitindex+1).trim());
                selectedDropLocation.setLatitude(String.valueOf(latLng.latitude));
                selectedDropLocation.setLongitude(String.valueOf(latLng.longitude));
                dropselected = true;
                droplocation.setText(selectedDropLocation.getLocationname());
                dropaddress.setText(selectedDropLocation.getLocationaddress());
                //if (!locality.isEmpty() && !country.isEmpty())
                    //resutText.setText(locality + "  " + country);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if(i==REASON_GESTURE) {
            pickupview.setVisibility(View.GONE);
            dropview.setVisibility(View.GONE);
            confirmview.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int temp = (int)marker.getTag();
        LatLng newposition = null;
        if(pickupselected) {
            if(temp!= selectedindex) {
                previousindex = selectedindex;
                selectedindex = (int) marker.getTag();
                selectedPickupLocation = pickupplacelist.get(selectedindex);
                Marker prevMarker = markerlist.get(previousindex);
                Marker newMarker = markerlist.get(selectedindex);

                prevMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hydrents_map));
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
                markerlist.remove(previousindex);
                markerlist.add(previousindex,prevMarker);
                markerlist.remove(selectedindex);
                markerlist.add(selectedindex,newMarker);
                newposition = newMarker.getPosition();
                //selectedLocation = data.getParcelableExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
                pickuplocation.setText(selectedPickupLocation.getLocationname());
                pickupaddress.setText(selectedPickupLocation.getLocationaddress());
                pickupselected = true;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newposition, 14));
            }

        }else{
            selectedindex = (int) marker.getTag();
            selectedPickupLocation = pickupplacelist.get(selectedindex);
            Marker newMarker = markerlist.get(selectedindex);
            newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create));
            markerlist.remove(selectedindex);
            markerlist.add(selectedindex,newMarker);
            newposition = newMarker.getPosition();
            pickuplocation.setText(selectedPickupLocation.getLocationname());
            pickupaddress.setText(selectedPickupLocation.getLocationaddress());
            pickupselected = true;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newposition, 14));
        }

        return false;
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
