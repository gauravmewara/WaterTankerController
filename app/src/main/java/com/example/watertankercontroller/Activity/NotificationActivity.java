package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.watertankercontroller.Adapter.NotificationAdapter;
import com.example.watertankercontroller.Modal.NotificationModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<NotificationModal> notlist;
    RecyclerView notificationlistview;
    NotificationAdapter adapter;
    ImageView menunotification;
    RelativeLayout menuback;
    TextView pagetitle,nodatadialog;
    ProgressBar notificationprogress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        notificationlistview = (RecyclerView)findViewById(R.id.rv_notification);
        notificationlistview.setVisibility(View.GONE);
        nodatadialog = (TextView)findViewById(R.id.tv_notificationitem_nodata);
        nodatadialog.setVisibility(View.GONE);
        menuback = (RelativeLayout) findViewById(R.id.rl_toolbar2_menu);
        menuback.setOnClickListener(this);
        menunotification = (ImageView) findViewById(R.id.iv_toolabar2_notification);
        pagetitle = (TextView)findViewById(R.id.tv_toolbar2_heading);
        pagetitle.setText(Constants.NOTIFICATION_PAGE_TITLE);
        notificationprogress = (ProgressBar)findViewById(R.id.pg_notification);
        createNotificationData();
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void createNotificationData(){
        NotificationModal not1,not2,not3,not4,not5;
        not1 = new NotificationModal();
        not2 = new NotificationModal();
        not3 = new NotificationModal();
        not4 = new NotificationModal();
        not5 = new NotificationModal();

        not1.setNotifiactionid("1011");
        not1.setNotificationheading("The Standard Lorem Ipsum passage, used since 1500s");
        not1.setNotificationmsg("Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut dolore magna aliqua");

        not2.setNotifiactionid("1012");
        not2.setNotificationheading("The Standard Lorem Ipsum passage, used since 1500s");
        not2.setNotificationmsg("Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut dolore magna aliqua");

        not3.setNotifiactionid("1013");
        not3.setNotificationheading("The Standard Lorem Ipsum passage, used since 1500s");
        not3.setNotificationmsg("Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut dolore magna aliqua");

        not4.setNotifiactionid("1014");
        not4.setNotificationheading("The Standard Lorem Ipsum passage, used since 1500s");
        not4.setNotificationmsg("Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut dolore magna aliqua");

        not5.setNotifiactionid("1015");
        not5.setNotificationheading("The Standard Lorem Ipsum passage, used since 1500s");
        not5.setNotificationmsg("Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut dolore magna aliqua");

        notlist = new ArrayList<>();
        notlist.add(not1);
        notlist.add(not2);
        notlist.add(not3);
        notlist.add(not4);
        notlist.add(not5);

        setRecyclerView();
    }
    public void setRecyclerView(){
        if(notlist==null){
            notificationprogress.setVisibility(View.GONE);
            nodatadialog.setVisibility(View.VISIBLE);
            notificationlistview.setVisibility(View.GONE);
        }else{
            notificationprogress.setVisibility(View.GONE);
            nodatadialog.setVisibility(View.GONE);
            notificationlistview.setVisibility(View.VISIBLE);
            adapter = new NotificationAdapter(NotificationActivity.this, notlist);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            notificationlistview.setLayoutManager(mLayoutManager);
            notificationlistview.setAdapter(adapter);
        }
    }

}
