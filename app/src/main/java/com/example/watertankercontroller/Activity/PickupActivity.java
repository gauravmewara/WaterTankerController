package com.example.watertankercontroller.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.watertankercontroller.Modal.PickupPlaceModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;

import java.util.ArrayList;

public class PickupActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView menuback,menunotification;
    RelativeLayout pickupview;
    ArrayList<PickupPlaceModal> placelist;
    TextView pickuplocation,pickupaddress,pagetitle;
    PickupPlaceModal selectedLocation = null;
    boolean pickupselected =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup);
        if(getIntent().hasExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE)) {
            selectedLocation = getIntent().getParcelableExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
            pickupselected = true;
        }
        menuback = (ImageView)findViewById(R.id.iv_toolbar2_menu);
        menuback.setOnClickListener(this);
        menunotification = (ImageView)findViewById(R.id.iv_toolabar2_notification);
        menunotification.setOnClickListener(this);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pagetitle.setText(Constants.MAP_PAGE_TITLE);
        pickupview = (RelativeLayout)findViewById(R.id.rl_pickup_view);
        pickupview.setOnClickListener(this);
        pickupview.setClickable(false);
        pickuplocation = (TextView)findViewById(R.id.tv_pickup_pickuplocation);
        pickupaddress = (TextView)findViewById(R.id.tv_pickup_pickupaddress);
        if(pickupselected){
            pickuplocation.setText(selectedLocation.getLocationname());
            pickupaddress.setText(selectedLocation.getLocationaddress());
        }
        createPickUpLocations();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.iv_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.iv_toolabar2_notification:
                intent = new Intent(PickupActivity.this,NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_pickup_view:
                intent = new Intent(PickupActivity.this,PickUpLocations.class);
                intent.putParcelableArrayListExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE,placelist);
                startActivityForResult(intent, Constants.PICKUP_ACTIVITY_PICKUP_LOCATION_REQUEST_CODE);
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
            }else{
                if(selectedLocation==null) {
                    pickupselected = false;
                    pickuplocation.setText("");
                    pickupaddress.setText("");
                }
            }
        }
    }

}
