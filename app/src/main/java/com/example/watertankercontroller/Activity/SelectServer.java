package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.GETAPIRequest;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelectServer extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    TextView title,proceed;
    Spinner spinner;
    ProgressBar pg;
    ArrayAdapter<String> dataAdapter;
    String selected_ip="";
    ArrayList<String> spinner_server_name;
    ArrayList<String> spinner_server_ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_server);
        pg = (ProgressBar) findViewById(R.id.progress_server);
        pg.setVisibility(View.GONE);
        spinner = (Spinner) findViewById(R.id.spinner_server);
        spinner.setOnItemSelectedListener(this);
        title = (TextView)findViewById(R.id.tb_with_bck_arrow_title1);
        title.setText("Select Server");
        proceed = (TextView)findViewById(R.id.tv_server_proceed);
        spinner_server_name = new ArrayList<>();
        spinner_server_ip = new ArrayList<>();
        spinner_server_name.add("Select Server");
        getServerApiCall();
        proceed.setOnClickListener(this);
    }

    private void getServerApiCall() {
        pg.setVisibility(View.VISIBLE);
        JSONObject jsonBodyObj = new JSONObject();
        try {
            GETAPIRequest getapiRequest = new GETAPIRequest();
            String url = URLs.FETCH_SERVER_URL;
            getapiRequest.request(this, serverCallListener, url);
        } catch (JSONException e) {
            e.printStackTrace();
            pg.setVisibility(View.GONE);
        }
    }
    FetchDataListener serverCallListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject response) {
            try {
                if(response!=null) {
                    JSONArray data = response.getJSONArray("data");
                    if(data.length()!=0){
                        for(int i=0;i<data.length();i++){
                            JSONObject obj = data.getJSONObject(i);
                            spinner_server_name.add(obj.getString("server_name"));
                            spinner_server_ip.add(obj.getString("server_url"));
                        }
                        dataAdapter = new ArrayAdapter<String>(SelectServer.this, android.R.layout.simple_spinner_item,spinner_server_name);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(dataAdapter);
                        pg.setVisibility(View.GONE);
                    } else {
                        onFetchFailure("Server Not Fetched");
                    }
                } else {
                    onFetchFailure("Server Not Fetched");
                }
            }catch (Exception e){
                e.printStackTrace();
                pg.setVisibility(View.GONE);
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            Toast.makeText(SelectServer.this,msg,Toast.LENGTH_LONG).show();
            pg.setVisibility(View.GONE);
        }

        @Override
        public void onFetchStart() {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_server_proceed:
                //pg.setVisibility(View.VISIBLE);
                if(!selected_ip.equals("")){
                    URLs.BASE_URL = selected_ip+"/api/controller/";
                    URLs.SOCKET_URL=selected_ip+"?token=";
                    Intent i = new Intent(SelectServer.this, LoginActivity.class);
                    //pg.setVisibility(View.GONE);
                    startActivity(i);
                    finish();
                }else{
                    //pg.setVisibility(View.GONE);
                    Toast.makeText(SelectServer.this,"Select server from list",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position>0) {
            selected_ip = spinner_server_ip.get(position - 1);
            selected_ip = selected_ip.substring(0,selected_ip.length()-1);
            SharedPrefUtil.setPreferences(SelectServer.this, Constants.SHARED_PREF_LOGIN_TAG,Constants.SERVER_IP,selected_ip);
        }else{
            selected_ip="";
            SharedPrefUtil.removePreferenceKey(SelectServer.this,Constants.SHARED_PREF_LOGIN_TAG,Constants.SERVER_IP);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    @Override
    public void onBackPressed() {
        if(SharedPrefUtil.hasKey(SelectServer.this,Constants.SHARED_PREF_LOGIN_TAG,Constants.SERVER_IP))
            SharedPrefUtil.removePreferenceKey(SelectServer.this,Constants.SHARED_PREF_LOGIN_TAG,Constants.SERVER_IP);
        super.onBackPressed();
    }
}