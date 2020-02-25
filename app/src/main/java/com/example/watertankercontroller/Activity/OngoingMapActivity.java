package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.watertankercontroller.R;

public class OngoingMapActivity extends AppCompatActivity {
    ImageView menuback,menunotification;
    TextView pagetitle,pickuplocation,pickupaddress,droplocation,dropaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_map);
    }
}
