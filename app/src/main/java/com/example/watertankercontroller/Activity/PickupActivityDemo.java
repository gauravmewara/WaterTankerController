package com.example.watertankercontroller.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PickupActivityDemo extends AppCompatActivity implements View.OnClickListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
    ImageView menuback,menunotification,droppin;
    RelativeLayout pickupview,dropview,confirmview;
    ArrayList<PickupPlaceModal> placelist;
    TextView pickuplocation,pickupaddress,pagetitle,droplocation,dropaddress;
    PickupPlaceModal selectedLocation = null;
    boolean pickupselected =false,fromMapReady=false;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup);
        if(getIntent().hasExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE)) {
            selectedLocation = getIntent().getParcelableExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
            pickupselected = true;
        }
        allpermissionsrequired = new ArrayList<>();
        allpermissionsrequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        allpermissionsrequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        markerlist = new ArrayList<>();
        menuback = (ImageView)findViewById(R.id.iv_toolbar2_menu);
        menuback.setOnClickListener(this);
        menunotification = (ImageView)findViewById(R.id.iv_toolabar2_notification);
        menunotification.setOnClickListener(this);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pagetitle.setText(Constants.MAP_PAGE_TITLE);
        pickupview = (RelativeLayout)findViewById(R.id.rl_pickup_view);
        pickupview.setOnClickListener(this);
        dropview = (RelativeLayout)findViewById(R.id.rl_pickupactivity_drop_view);
        dropview.setOnClickListener(this);
        droppin = (ImageView)findViewById(R.id.iv_pickupactivity_drop_pin);
        confirmview = (RelativeLayout)findViewById(R.id.rl_pickupacitvity_confirm);
        confirmview.setOnClickListener(this);
        pickupview.setClickable(false);
        pickuplocation = (TextView)findViewById(R.id.tv_pickup_pickuplocation);
        pickupaddress = (TextView)findViewById(R.id.tv_pickup_pickupaddress);
        droplocation = (TextView)findViewById(R.id.tv_pickupactivity_droplocation);
        dropaddress = (TextView)findViewById(R.id.tv_pickupactivity_dropaddress);
        if(pickupselected){
            pickuplocation.setText(selectedLocation.getLocationname());
            pickupaddress.setText(selectedLocation.getLocationaddress());
        }
        checkAndRequestPermissions(this,allpermissionsrequired);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.iv_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.iv_toolabar2_notification:
                intent = new Intent(PickupActivityDemo.this,NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_pickup_view:
                intent = new Intent(PickupActivityDemo.this,PickUpLocations.class);
                intent.putParcelableArrayListExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE,placelist);
                startActivityForResult(intent, Constants.PICKUP_ACTIVITY_PICKUP_LOCATION_REQUEST_CODE);
                break;
            case R.id.rl_pickupactivity_drop_view:
                droppin.setVisibility(View.VISIBLE);
                break;
            case R.id.rl_pickupacitvity_confirm:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(pickupselected) {
            Intent intent = new Intent();
            intent.putExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE, selectedLocation);
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
        PickupPlaceModal place1,place2,place3,place4,place5;
        place1 = new PickupPlaceModal();
        place1.setLocationname("Boranada");
        place1.setLocationaddress("sojati gate, Jodhpur");
        place1.setLatitude("26.287951");
        place1.setLongitude("73.025908");

        place2 = new PickupPlaceModal();
        place2.setLocationname("Koranada");
        place2.setLocationaddress("jalori gate, Jodhpur");
        place2.setLatitude("26.283950");
        place2.setLongitude("73.016038");

        place3 = new PickupPlaceModal();
        place3.setLocationname("Moranada");
        place3.setLocationaddress("siwanchi gate, Jodhpur");
        place3.setLatitude("26.285797");
        place3.setLongitude("73.011060");

        place4 = new PickupPlaceModal();
        place4.setLocationname("Noranada");
        place4.setLocationaddress("merti gate, Jodhpur");
        place4.setLatitude("26.294377");
        place4.setLongitude("73.029674");

        place5 = new PickupPlaceModal();
        place5.setLocationname("Poranada");
        place5.setLocationaddress("DPS, Jodhpur");
        place5.setLatitude("26.263035");
        place5.setLongitude("72.949058");

        placelist = new ArrayList<>();
        placelist.add(place1);
        placelist.add(place2);
        placelist.add(place3);
        placelist.add(place4);
        placelist.add(place5);
        pickupview.setClickable(true);

        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fg_pickup_map);
        mapFragment.getMapAsync(this);
        //mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        checkLocation();
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constants.PICKUP_ACTIVITY_PICKUP_LOCATION_REQUEST_CODE){
            if(resultCode== Activity.RESULT_OK){
                selectedLocation = data.getParcelableExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
                pickuplocation.setText(selectedLocation.getLocationname());
                pickupaddress.setText(selectedLocation.getLocationaddress());
                pickupselected = true;
                for(int i=0;i<placelist.size();i++){
                    if(placelist.get(i).isLocationSame(selectedLocation)){
                        selectedindex = i;
                        break;
                    }
                }
                LatLng place = null;
                Marker m2;
                //int selectedIndex=placelist.indexOf(selectedLocation);
                if(selectedindex!=previousindex) {
                    Marker oldselexted = markerlist.get(previousindex);
                    oldselexted.remove();
                    LatLng oldplace = new LatLng(Double.parseDouble(placelist.get(previousindex).getLatitude()), Double.parseDouble(placelist.get(previousindex).getLongitude()));
                    MarkerOptions oldmarkerop = new MarkerOptions()
                            .position(oldplace)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.hydrents_map))
                            .title(placelist.get(previousindex).getLocationname());
                    Marker m1 =  mMap.addMarker(oldmarkerop);
                    markerlist.remove(previousindex);
                    markerlist.add(previousindex,m1);

                    Marker selexted = markerlist.get(selectedindex);
                    selexted.remove();
                    place = new LatLng(Double.parseDouble(selectedLocation.getLatitude()), Double.parseDouble(selectedLocation.getLongitude()));
                    MarkerOptions markerop = new MarkerOptions()
                            .position(place)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create))
                            .title(selectedLocation.getLocationname());
                    m2 = mMap.addMarker(markerop);
                    markerlist.remove(selectedindex);
                    markerlist.add(selectedindex,m2);
                }else{
                    Marker selexted = markerlist.get(selectedindex);
                    selexted.remove();
                    place = new LatLng(Double.parseDouble(selectedLocation.getLatitude()), Double.parseDouble(selectedLocation.getLongitude()));
                    MarkerOptions markerop = new MarkerOptions()
                            .position(place)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create))
                            .title(selectedLocation.getLocationname());
                    m2 = mMap.addMarker(markerop);
                    markerlist.remove(selectedindex);
                    markerlist.add(selectedindex,m2);
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m2.getPosition(), 14));
            }else{
                if(selectedLocation==null) {
                    pickupselected = false;
                    pickuplocation.setText("Select Pickup Location");
                    pickupaddress.setText("Click Here");
                }
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fromMapReady = true;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(permissionGranted){
                buildGoogleApiClient();
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMyLocationEnabled(true);
                mMap.setTrafficEnabled(false);
                mMap.setIndoorEnabled(false);
                mMap.setBuildingsEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
            }
        }else{
            buildGoogleApiClient();
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(false);
            mMap.setIndoorEnabled(false);
            mMap.setBuildingsEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
        for (int i = 0; i < placelist.size(); i++) {
            LatLng place = new LatLng(Double.parseDouble(placelist.get(i).getLatitude()), Double.parseDouble(placelist.get(i).getLongitude()));
            //googleMap.addMarker(new MarkerOptions().position(place).title(placelist.get(i).getLocationname()));
            MarkerOptions markerop;
            if (pickupselected) {
                if (placelist.get(i).isLocationSame(selectedLocation)) {
                    selectedindex = i;
                    markerop = new MarkerOptions()
                            .position(place)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_create))
                            .title(placelist.get(i).getLocationname());
                } else {
                    markerop = new MarkerOptions()
                            .position(place)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.hydrents_map))
                            .title(placelist.get(i).getLocationname());
                }
            } else {
                markerop = new MarkerOptions()
                        .position(place)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hydrents_map))
                        .title(placelist.get(i).getLocationname());
            }
            Marker marker = googleMap.addMarker(markerop);
            markerlist.add(marker);
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
            createPickUpLocations();
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
                        createPickUpLocations();
                    }else{
                        checkAndRequestPermissions(PickupActivityDemo.this,allpermissionsrequired);
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
        //Commented Code will be required to put custom marker at current location

        /*if(mCurrLocationMarker!=null){
            mCurrLocationMarker.remove();
        }*/
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        /*MarkerOptions markeroptions = new MarkerOptions();
        markeroptions.position(latLng);
        markeroptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.userlocation_map));
        markeroptions.title("Your Location");
        mCurrLocationMarker = mMap.addMarker(markeroptions);*/
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        if(mGoogleApiClient!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!permissionGranted) {
            return;
        }
        //mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraIdleListener(this);
        if(fromMapReady){
            startLocationUpdates();
            fromMapReady = false;
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
    protected void onStart() {if (!permissionGranted) {
        return;
    }
        if(fromMapReady){
            startLocationUpdates();
            fromMapReady = false;
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {

        if(mGoogleApiClient!=null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
        previousindex = selectedindex;
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
        Geocoder geocoder = new Geocoder(PickupActivityDemo.this);

        pickupview.setVisibility(View.VISIBLE);
        dropview.setVisibility(View.VISIBLE);
        confirmview.setVisibility(View.VISIBLE);
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                String locality = addressList.get(0).getAddressLine(0);
                String country = addressList.get(0).getCountryName();
                droplocation.setText(locality);
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
        return false;
    }
}
