package com.example.watertankercontroller.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.Adapter.TankerAdapter;
import com.example.watertankercontroller.Modal.TankerModal;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TankerDetails extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
    ArrayList<TankerModal>tankerlist;
    RelativeLayout menuback;
    RelativeLayout toolbar_notification,noticountlayout;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    TextView notiCount;
    static String notificationCount;
    static Context context;
    TextView pagetitle,nodata;
    RecyclerView tankerlistview;
    ProgressBar tankerprogress;
    TankerAdapter adapter;
    boolean fromBuildMethod=false;
    ArrayList<String> allpermissionsrequired;
    boolean permissionGranted = false;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 20000; /* 20 sec */
    private LocationManager locationManager;
    private LatLng currentlatlng=null;
    boolean isListNull = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tanker_details);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pagetitle.setText(Constants.TANKERDETAIL_PAGE_TITLE);
        nodata = (TextView)findViewById(R.id.tv_tankerdetail_nodata);
        nodata.setVisibility(View.GONE);
        allpermissionsrequired = new ArrayList<>();
        allpermissionsrequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        allpermissionsrequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        tankerlistview = (RecyclerView)findViewById(R.id.rv_tankerdetails);
        tankerlistview.setVisibility(View.GONE);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        context = this;


        tankerprogress = (ProgressBar)findViewById(R.id.pg_tankerdetails);


        //createTankerData();
        checkAndRequestPermissions(this,allpermissionsrequired);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.cl_tankeritem:
                break;
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;

        }
    }

    public void createTankerData(){
        try{
            String token = SessionManagement.getUserToken(TankerDetails.this);
            GETAPIRequest pickuppointrequest=new GETAPIRequest();
            String url = URLs.BASE_URL+URLs.NEARBY_TANKERS+"?lat="+currentlatlng.latitude+"&lng="+currentlatlng.longitude;
            Log.i("url",String.valueOf(url));
            Log.i("token",String.valueOf(token));
            HeadersUtil headparam = new HeadersUtil(token);
            pickuppointrequest.request(this, getTankerListener,url,headparam);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    FetchDataListener getTankerListener=new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            //RequestQueueService.cancelProgressDialog();
            try {
                if (response != null) {
                    if (response.getInt("error")==0) {
                        JSONArray data = response.getJSONArray("data");
                        if(data!=null) {
                            if(data.length()!=0) {
                                tankerlist = new ArrayList<>();
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject tempjson = data.getJSONObject(i);
                                    TankerModal mod = new TankerModal();
                                    mod.setId(tempjson.getString("_id"));
                                    mod.setLanguage(tempjson.getJSONObject("settings").getString("language"));
                                    mod.setPhonecode(tempjson.getString("phone_country_code"));
                                    mod.setRegistrationno(tempjson.getString("registration_number"));
                                    mod.setDrivername(tempjson.getString("driver_name"));
                                    mod.setDriverlicense(tempjson.getString("driver_license_number"));
                                    mod.setTankerModelNo(tempjson.getString("tanker_model_number"));
                                    mod.setTankercapacity(tempjson.getString("tanker_capacity"));
                                    mod.setLocation(tempjson.getString("location"));
                                    mod.setOngoing(tempjson.getString("activity_status"));
                                    //mod.setTankerid(tempjson.getString("id"));
                                    mod.setContractorid(tempjson.getString("contractor_id"));
                                    mod.setDrivermobile(tempjson.getString("phone"));
                                    mod.setFitnessdate(tempjson.getString("fitness_date"));
                                    mod.setInsurancedate(tempjson.getString("insurance_date"));
                                    JSONArray tankerimgs = tempjson.getJSONArray("images");
                                    if(tankerimgs.length()!=0) {
                                        mod.setTankerimage(tempjson.getJSONArray("images").getString(0));
                                    }
                                    mod.setLongitude(tempjson.getJSONObject("geometry").getJSONArray("coordinates").getString(0));
                                    mod.setLatitude(tempjson.getJSONObject("geometry").getJSONArray("coordinates").getString(1));
                                    mod.setContractorname(tempjson.getString("contractor_name"));
                                    tankerlist.add(mod);
                                }
                                isListNull = false;
                            }
                            setRecyclerView();
                        }
                        else {
                            RequestQueueService.showAlert("Error! No data fetched", TankerDetails.this);
                            setRecyclerView();
                        }
                    }
                } else {
                    RequestQueueService.showAlert("Error! No data fetched", TankerDetails.this);
                    setRecyclerView();
                }
            }catch (Exception e){
                RequestQueueService.showAlert("Something went wrong", TankerDetails.this);
                setRecyclerView();
                e.printStackTrace();
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            //RequestQueueService.cancelProgressDialog();
            RequestQueueService.showAlert(msg,TankerDetails.this);
        }

        @Override
        public void onFetchStart() {
            //RequestQueueService.showProgressDialog(Login.this);
        }
    };


    public void setRecyclerView(){
        if(isListNull){
            tankerprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.VISIBLE);
            tankerprogress.setVisibility(View.GONE);
        }else{
            tankerprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.GONE);
            tankerlistview.setVisibility(View.VISIBLE);
            adapter = new TankerAdapter(this, tankerlist);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            tankerlistview.setLayoutManager(mLayoutManager);
            tankerlistview.setAdapter(adapter);
        }
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
                        checkAndRequestPermissions(TankerDetails.this,allpermissionsrequired);
                    }
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean checkLocation() {
        boolean status = isLocationEnabled();
        if (!status)
            showAlert();
        return status;
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
        if (!permissionGranted) {
            return;
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
    public void onBackPressed() {
        super.onBackPressed();
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
        Log.i("Tanker ACTIVITY:", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("TANKER ACTIVITY:", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        currentlatlng = new LatLng(location.getLatitude(), location.getLongitude());
        if(currentlatlng!=null)
            //createPickUpLocations();
            createTankerData();
        else
            Toast.makeText(TankerDetails.this,"Current location not fetched",Toast.LENGTH_LONG).show();
        if(mGoogleApiClient!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
