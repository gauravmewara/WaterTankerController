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
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {

    RelativeLayout droplayout,confirmlayout;
    TextView pickuplocation,pickupaddress,droplocation,dropaddress,pagetitle;
    PickupPlaceModal selectedpickuplocation,selecteddroplocation;
    ImageView menuback,menunotification;
    Boolean isDropselected = false,fromMapReady = false;
    List<Place.Field> fields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG);
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    public int previousindex,selectedindex;
    ArrayList<String> allpermissionsrequired;
    boolean permissionGranted = false;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 20000; /* 20 sec */
    private LocationManager locationManager;
    private LatLng latLng;
    private Marker dropMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop);
        Bundle b = getIntent().getExtras();
        selectedpickuplocation = b.getParcelable(Constants.DROP_LOCATION_INTENT_DATA_TITLE);
        menuback = (ImageView)findViewById(R.id.iv_toolbar2_menu);
        menuback.setOnClickListener(this);
        menunotification=(ImageView)findViewById(R.id.iv_toolabar2_notification);
        menunotification.setOnClickListener(this);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        droplayout = (RelativeLayout)findViewById(R.id.rl_dropactivity_drop_view);
        droplayout.setOnClickListener(this);
        confirmlayout = (RelativeLayout)findViewById(R.id.rl_dropactivity_confirm);
        confirmlayout.setOnClickListener(this);
        pickuplocation = (TextView)findViewById(R.id.tv_dropactivity_pickuplocation);
        pickupaddress = (TextView)findViewById(R.id.tv_dropactivity_pickupaddress);
        droplocation = (TextView)findViewById(R.id.tv_dropactivity_droplocation);
        dropaddress = (TextView)findViewById(R.id.tv_dropactivity_dropaddress);
        Places.initialize(getApplicationContext(),Constants.MAP_API_KEY);
        PlacesClient placesClient = Places.createClient(this);
        allpermissionsrequired = new ArrayList<>();
        allpermissionsrequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        allpermissionsrequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        pagetitle.setText(Constants.MAP_PAGE_TITLE);
        pickuplocation.setText(selectedpickuplocation.getLocationname());
        pickupaddress.setText(selectedpickuplocation.getLocationaddress());
        checkAndRequestPermissions(this,allpermissionsrequired);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()){
            case R.id.iv_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.iv_toolabar2_notification:
                intent = new Intent(DropActivity.this,NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_dropactivity_drop_view:
                Toast.makeText(DropActivity.this,"Google Places Api is temporarily disabled",Toast.LENGTH_LONG).show();
                //intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fields).build(DropActivity.this);
                //startActivityForResult(intent,Constants.GOOGLE_AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.rl_dropactivity_confirm:
                if(!isDropselected){
                    Toast.makeText(DropActivity.this,"Select Drop Location",Toast.LENGTH_LONG).show();
                }else{
                    onBackPressed();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.GOOGLE_AUTOCOMPLETE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("Drop Place: ",place.getId()+" - "+ place.getName()+"-"+place.getAddress());
                LatLng droplatLng = place.getLatLng();
                selecteddroplocation = new PickupPlaceModal();
                selecteddroplocation.setLocationname(place.getName());
                selecteddroplocation.setLocationaddress(place.getAddress());
                selecteddroplocation.setLatitude(String.valueOf(droplatLng.latitude));
                selecteddroplocation.setLongitude(String.valueOf(droplatLng.longitude));
                droplocation.setText(place.getName());
                dropaddress.setText(place.getAddress());
                if(dropMarker!=null){
                    dropMarker.remove();
                    dropMarker = null;
                }
                isDropselected = true;
                MarkerOptions dropop = new MarkerOptions()
                        .position(droplatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.droppoint_create))
                        .title("Your Location");
                dropMarker = mMap.addMarker(dropop);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropMarker.getPosition(), 14));
            }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("DropActivity Status:",status.getStatusMessage());
            }else if(resultCode == RESULT_CANCELED){
                droplocation.setText("Click Here");
                dropaddress.setText("");
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!permissionGranted) {
            return;
        }
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

    @Override
    public void onLocationChanged(Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions dropop = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.droppoint_create))
                .title("Your Location");
        dropMarker = mMap.addMarker(dropop);
        setTemporaryDropLocation(latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        if(mGoogleApiClient!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
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
        LatLng place = new LatLng(Double.parseDouble(selectedpickuplocation.getLatitude()),Double.parseDouble(selectedpickuplocation.getLongitude()));
        MarkerOptions markerop = new MarkerOptions()
                .position(place)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickuppoint_hydrents_map))
                .title(selectedpickuplocation.getLocationname());
        googleMap.addMarker(markerop);
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
            checkLocation();
            initializeMap();
        }
    }

    public void initializeMap(){
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fg_drop_map);
        mapFragment.getMapAsync(this);
    }
    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }
    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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
    protected void onStart() {
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

    @Override
    public void onBackPressed() {
        if(isDropselected) {
            Intent intent = new Intent();
            intent.putExtra(Constants.DROP_LOCATION_INTENT_DATA_TITLE, selecteddroplocation);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }else{
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }
        super.onBackPressed();
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void setTemporaryDropLocation(LatLng droplatlang){
        isDropselected = true;
        selecteddroplocation = new PickupPlaceModal();
        selecteddroplocation.setLatitude(String.valueOf(droplatlang.latitude));
        selecteddroplocation.setLongitude(String.valueOf(droplatlang.longitude));
        selecteddroplocation.setLocationname("Current Location");
        selecteddroplocation.setLocationaddress("");
        droplocation.setText(selecteddroplocation.getLocationname());
        dropaddress.setText(selecteddroplocation.getLocationaddress());
    }
}
