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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.Adapter.ContractorsAdapter;
import com.example.watertankercontroller.Modal.ContractorModal;
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
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class BookingForm extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    EditText mobile,message,geofence;
    TextView pagetitle;
    ContractorModal contractorModal;
    Spinner spinner;
    TextView pickuplocation,pickupaddress,droplocation,dropaddress,messagelength;
    RelativeLayout create;
    ImageView pickup,drop;
    RelativeLayout menuback;
    ConstraintLayout pickupLayout,dropLayout;
    PickupPlaceModal selectedpickup = null;
    PickupPlaceModal selecteddrop = null;
    ContractorsAdapter cadapter;
    ArrayList<ContractorModal> contractor;
    //ArrayList<String> spinner_contractor_id;
    RelativeLayout toolbar_notification,noticountlayout,messageLayout;
    String pr_name,c_id,c_name;
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
        context = this;
        spinner = (Spinner) findViewById(R.id.spinner_bookingform_contractor);
        spinner.setOnItemSelectedListener(this);
        contractor= new ArrayList<ContractorModal>();
        messageLayout=(RelativeLayout)findViewById(R.id.cl_bookingform_message);
        messageLayout.setOnClickListener(this);
        pickup = (ImageView)findViewById(R.id.iv_bookingform_pickupmap);
        pickupLayout = (ConstraintLayout)findViewById(R.id.cl_bookingform_pickup);
        pickupLayout.setOnClickListener(this);
        drop = (ImageView)findViewById(R.id.iv_bookingform_dropmap);
        dropLayout = (ConstraintLayout)findViewById(R.id.cl_bookingform_drop);
        dropLayout.setOnClickListener(this);
        create = (RelativeLayout)findViewById(R.id.rl_bookingform_create);
        create.setOnClickListener(this);
        mobile = (EditText)findViewById(R.id.et_bookingform_mobile);
        geofence=(EditText)findViewById(R.id.et_bookingform_geofence_in_meters);
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
        getContractorApiCall();

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.cl_bookingform_message:

                message.clearFocus();
                message.requestFocus();
                message.isFocused();
                InputMethodManager openkeyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                openkeyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
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
        }
        else if(mobile.getText().toString().trim().length()<=0){
            message = "Enter Mobile Number";
            valid = false;
        }else if(mobile.getText().toString().trim().length()<10){
            message = "Mobile number should be of 10 digits";
            valid = false;
        }else if (spinner.equals(null)){
            if (spinner.getSelectedItemPosition()==0){
            message="Please select the work order id";
            }
        }
        else if(c_id.equals(null)){
            message = "Please select the work order";
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
            jsonbody.put("contractor_id",c_id);
            jsonbody.put("lat",selecteddrop.getLatitude());
            jsonbody.put("lng",selecteddrop.getLongitude());
            jsonbody.put("location",selecteddrop.getLocationname()+","+selecteddrop.getLocationaddress());
            jsonbody.put("geofence_radius",geofence.getText().toString().trim());
            jsonbody.put("phone",mobile.getText().toString().trim());
            if(message.getText().toString().trim().length()>0){
                jsonbody.put("message",message.getText().toString().trim());
            }
            String url = URLs.BASE_URL+URLs.CREATE_BOOKING;
            Log.i("url", String.valueOf(url));
            String token = SessionManagement.getUserToken(this);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(this,createBookingListener,url,headparam,jsonbody);
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

    private void getContractorApiCall() {
        JSONObject jsonbody = new JSONObject();
        try {
            GETAPIRequest getapiRequest = new GETAPIRequest();
            String url = URLs.BASE_URL+URLs.CONTRACTORS;
            Log.i("url", String.valueOf(url));
            String token = SessionManagement.getUserToken(this);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(this.getApplicationContext(),createContractorListener,url,headparam,jsonbody);
        } catch (JSONException e) {
            e.printStackTrace(); }
    }
    FetchDataListener createContractorListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            try{
                if(response!=null){
                    if(response.getInt("error")==0){
                        JSONArray data = response.getJSONArray("data");
                        if(data.length()!=0) {
                            cadapter = new ContractorsAdapter(BookingForm.this,contractor);

                            contractorModal = new ContractorModal();
//                            contractorModal.setContractorname("");
                            contractorModal.setContracator_id("Select  work order");
                            contractor.add(contractorModal);
                            spinner.setAdapter(cadapter);

                            for (int i = 0; i < data.length(); i++) {
                                ContractorModal cm = new ContractorModal();
                                JSONObject obj = data.getJSONObject(i);
                                Log.i("project_name",obj.getString("project_name"));
                                Log.i("_id",obj.getString("_id"));
                                Log.i("name",obj.getString("name"));
                                cm.setProjectname(obj.getString("project_name"));
                                cm.set_id(obj.getString("_id"));
                                cm.setName(obj.getString("name"));
                                if (obj.has("contractor_id")){

                                Log.i("contractor_id",obj.getString("contractor_id"));
                                cm.setContracator_id(obj.getString("contractor_id"));
                                }
                                String x =cm.getName();

                                contractor.add(cm); }

                            Log.i("done",String.valueOf(contractor.size()));
                            cadapter.notifyDataSetChanged();
                            spinner.setOnItemSelectedListener(BookingForm.this);
                        }
                        else { RequestQueueService.showAlert("Error! No data fetched", BookingForm.this); }
                    }else { RequestQueueService.showAlert("Response Not Fetched", BookingForm.this); }
                }
            }catch (Exception e){
                e.printStackTrace();
                spinner.setClickable(true);
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            RequestQueueService.showAlert(msg, BookingForm.this);
            spinner.setClickable(true);
        }

        @Override
        public void onFetchStart() {

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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        pr_name = contractor.get(position).getProjectname();
        c_id=contractor.get(position).get_id();
        c_name=contractor.get(position).getName();



    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
