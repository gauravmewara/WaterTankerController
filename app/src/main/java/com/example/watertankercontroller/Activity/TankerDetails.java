package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.watertankercontroller.Adapter.TankerAdapter;
import com.example.watertankercontroller.Modal.TankerModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;

import java.util.ArrayList;

public class TankerDetails extends AppCompatActivity implements View.OnClickListener {
    ArrayList<TankerModal>tankerlist;
    ImageView menunotification;
    RelativeLayout menuback;
    TextView pagetitle,nodata;
    RecyclerView tankerlistview;
    ProgressBar tankerprogress;
    TankerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tanker_details);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pagetitle.setText(Constants.TANKERDETAIL_PAGE_TITLE);
        nodata = (TextView)findViewById(R.id.tv_tankerdetail_nodata);
        nodata.setVisibility(View.GONE);
        tankerlistview = (RecyclerView)findViewById(R.id.rv_tankerdetails);
        tankerlistview.setVisibility(View.GONE);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        menunotification = (ImageView)findViewById(R.id.iv_toolabar2_notification);
        menunotification.setOnClickListener(this);
        tankerprogress = (ProgressBar)findViewById(R.id.pg_tankerdetails);
        createTankerData();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.cl_tankeritem:
                break;
            case R.id.rl_toolbar2_menu:
                onBackPressed();
                break;
            case R.id.iv_toolabar2_notification:
                Intent intent;
                intent = new Intent(TankerDetails.this,NotificationActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void createTankerData(){
        TankerModal t1,t2,t3,t4;

        t1= new TankerModal();
        t1.setDrivername("James Smith");
        t1.setDrivermobile("1234567890");
        t1.setContractorname("Smith James");
        t1.setTankerModelNo("RJ12CA0001");
        t1.setTankercapacity("500 LTR");
        t1.setOngoing("0");

        t2= new TankerModal();
        t2.setDrivername("James Smith");
        t2.setDrivermobile("1234567890");
        t2.setContractorname("Smith James");
        t2.setTankerModelNo("RJ12CA0001");
        t2.setTankercapacity("500 LTR");
        t2.setOngoing("1");

        t3= new TankerModal();
        t3.setDrivername("James Smith");
        t3.setDrivermobile("1234567890");
        t3.setContractorname("Smith James");
        t3.setTankerModelNo("RJ12CA0001");
        t3.setTankercapacity("500 LTR");
        t3.setOngoing("0");

        t4= new TankerModal();
        t4.setDrivername("James Smith");
        t4.setDrivermobile("1234567890");
        t4.setContractorname("Smith James");
        t4.setTankerModelNo("RJ12CA0001");
        t4.setTankercapacity("500 LTR");
        t4.setOngoing("0");

        tankerlist = new ArrayList<>();
        tankerlist.add(t1);
        tankerlist.add(t2);
        tankerlist.add(t3);
        tankerlist.add(t4);
        setRecyclerView();
    }

    public void setRecyclerView(){
        if(tankerlist==null){
            tankerprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.VISIBLE);
            tankerlistview.setVisibility(View.GONE);
        }else{
            tankerprogress.setVisibility(View.GONE);
            nodata.setVisibility(View.GONE);
            tankerlistview.setVisibility(View.VISIBLE);
            adapter = new TankerAdapter(TankerDetails.this, tankerlist);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            tankerlistview.setLayoutManager(mLayoutManager);
            tankerlistview.setAdapter(adapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
