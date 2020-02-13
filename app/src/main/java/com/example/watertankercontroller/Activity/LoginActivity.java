package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.watertankercontroller.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    ConstraintLayout signin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signin = (ConstraintLayout)findViewById(R.id.cl_login_loginbtn);
        signin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.cl_login_loginbtn:
                intent = new Intent(LoginActivity.this,BookingStatus.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
