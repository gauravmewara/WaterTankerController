package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.POSTAPIRequest;
import com.example.watertankercontroller.Utils.RequestQueueService;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.SharedPrefUtil;
import com.example.watertankercontroller.Utils.URLs;
import com.google.firebase.iid.FirebaseInstanceId;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    ConstraintLayout signin;
    EditText username,pwd;
    RelativeLayout rl_progressbar;
    String usernamestring, password;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signin = (ConstraintLayout)findViewById(R.id.cl_login_loginbtn);
        signin.setOnClickListener(this);
        username = (EditText)findViewById(R.id.et_login_username);
        pwd = (EditText)findViewById(R.id.et_login_pwd);
        rl_progressbar=(RelativeLayout)findViewById(R.id.rl_progress);
        progressBar=(ProgressBar)findViewById(R.id.progress_login);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.cl_login_loginbtn:
                signin.setClickable(false);
                progressBar.setVisibility(View.VISIBLE);
                if (validate()){
                    loginApiCall();
                }
                break;
        }
    }

    private boolean validate(){
        usernamestring = username.getText().toString();
        password = pwd.getText().toString();
        if (username.getText().toString().equals("")) {
            progressBar.setVisibility(View.GONE);
            RequestQueueService.showAlert("Enter Username",LoginActivity.this);
            signin.setClickable(true);
            return false;

        }
        if (pwd.getText().toString().equals("")) {
            progressBar.setVisibility(View.GONE);
            RequestQueueService.showAlert("Enter Password",LoginActivity.this);
            signin.setClickable(true);
            return false;
        }
        if (pwd.getText().toString().length()<8){
            progressBar.setVisibility(View.GONE);
            RequestQueueService.showAlert("Password should be of atleast 8 letters long ",LoginActivity.this);
            signin.setClickable(true);
            return false;
        }

        return true;
    }


    private void loginApiCall(){
        JSONObject jsonBodyObj = new JSONObject();
        try{
            jsonBodyObj.put("username", username.getText().toString().trim());
            jsonBodyObj.put("password", pwd.getText().toString().trim());
            jsonBodyObj.put("device_type", "a");
            String token = FirebaseInstanceId.getInstance().getToken();
            jsonBodyObj.put("device_token", token);
            POSTAPIRequest postapiRequest=new POSTAPIRequest();
            String url = URLs.BASE_URL+URLs.SIGN_IN_URL;
            Log.i("url",String.valueOf(url));
            Log.i("token",String.valueOf(token));
            Log.i("Request",username.getText().toString()+", "+pwd.getText().toString());
            HeadersUtil headparam = new HeadersUtil();
            postapiRequest.request(this, loginApiListener,url,headparam,jsonBodyObj);
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    FetchDataListener loginApiListener=new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject data) {
            //RequestQueueService.cancelProgressDialog();
            try {
                if (data != null) {
                    if (data.getInt("error")==0) {
                        Log.i("Login", "Login Successfull");
                        JSONObject userdetail = data.getJSONObject("data");
                        if(userdetail!=null) {
                            SessionManagement.createLoginSession(LoginActivity.this,
                                    true, userdetail.getString("_id"),
                                    userdetail.getString("phone_country_code"),
                                    userdetail.getString("phone"),
                                    userdetail.getString("name"),
                                    userdetail.getString("token"),
                                    userdetail.getJSONObject("settings").getString("language"),
                                    userdetail.getString("location"),userdetail.getString("notification_count"));
                            Intent i = new Intent(LoginActivity.this, BookingStatus.class);
                            progressBar.setVisibility(View.GONE);
                            signin.setClickable(true);

                            startActivity(i);
                            finish();
                        }
                        else {
                            RequestQueueService.showAlert("Error! No data fetched", LoginActivity.this);
                            progressBar.setVisibility(View.GONE);
                            signin.setClickable(true);

                        }
                    }
                } else {
                    RequestQueueService.showAlert("Error! No data fetched", LoginActivity.this);
                    progressBar.setVisibility(View.GONE);
                    signin.setClickable(true);
                    }
            }catch (Exception e){
                RequestQueueService.showAlert("Something went wrong", LoginActivity.this);
                progressBar.setVisibility(View.GONE);
                signin.setClickable(true);
                e.printStackTrace();
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            //RequestQueueService.cancelProgressDialog();
            RequestQueueService.showAlert(msg,LoginActivity.this);
            progressBar.setVisibility(View.GONE);
            signin.setClickable(true);
        }

        @Override
        public void onFetchStart() {
            //RequestQueueService.showProgressDialog(Login.this);
        }
    };
}
