package com.example.watertankercontroller.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.watertankercontroller.Modal.BookingModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.NetworkUtils;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketTest extends AppCompatActivity {
    BookingModal blmod;
    Socket socket;
    boolean socketInitialized;
    TextView socket_tv,tv_sock_con,tv_sock_listen;
    Button chk_status,resetstatus;
    int UpdateCount=0;
    boolean networkstatechanged = false;
    private BroadcastReceiver networkStateReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_test);
        Bundle b = getIntent().getExtras();
        blmod = b.getParcelable("Bookingdata");
        socket_tv =(TextView) findViewById(R.id.tv_bookingstatus);
        tv_sock_con = (TextView)findViewById(R.id.tv_sock_con);
        tv_sock_listen = (TextView)findViewById(R.id.tv_sock_listen);
        chk_status = (Button)findViewById(R.id.btn_chk_status);
        resetstatus = (Button)findViewById(R.id.btn_reset_socket);
        socket_tv.setText("Location not Updated");
        tv_sock_con.setText("Not Connected");
        tv_sock_listen.setText("Not Listening");
        chk_status.setClickable(false);
        resetstatus.setClickable(false);
        chk_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(socket!=null){
                    if(socket.connected()){
                        tv_sock_con.setText("Connected");
                    }else{
                        tv_sock_con.setText("Not Connected");
                    }
                    if(socket.hasListeners("aborted:Booking")){
                        tv_sock_listen.setText("Listening Abort");
                    }else{
                        tv_sock_listen.setText("Not Listening Abort");
                    }
                    if(socket.hasListeners("locationUpdate:Booking")){
                        socket_tv.setText("Listening Location");
                    }else{
                        socket_tv.setText("Not Listening Location");
                    }
                }else{
                    tv_sock_con.setText("Socket is Null");
                }
            }
        });
        resetstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSocket();
            }
        });
        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("Broadcast:","Network State");
                networkstatechanged = true;
                int status = NetworkUtils.getConnectivityStatus(getApplicationContext());
                if(status==NetworkUtils.NETWORK_INTERNET||status==NetworkUtils.WIFI_INTERNET){
                    Log.i("Network State:","Internet Available");
                    resetSocket();
                    networkstatechanged = false;
                }else{
                    Log.i("Network State:","No Internet");
                }
            }
        };
        initSocket(blmod.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }

    public void initSocket(String id){
        try {
            socket = IO.socket(URLs.SOCKET_URL + SessionManagement.getUserToken(this));
            socket.connect();
            socketInitialized = true;
            Log.i("SOCKET TEST:","socket Initialized");
            socket.on("aborted:Booking", onBookingAborted);
            socket.on("end:Booking",onBookingEnd);
            socket.on("locationUpdate:Booking",onLocationUpdate);
            JSONObject params = new JSONObject();
            params.put("booking_id", id);
            socket.emit("subscribe:Booking", params);
            chk_status.setClickable(true);
            resetstatus.setClickable(true);
        }catch (URISyntaxException e){
            e.printStackTrace();
            socketInitialized = false;
        }catch (JSONException e){
            e.printStackTrace();
            socketInitialized = false;
        }
    }

    private Emitter.Listener onBookingAborted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("OnAbortListener","Booking aborted");
            SocketTest.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socket_tv.setText("Booking Aborted");
                }
            });

        }
    };

    private Emitter.Listener onLocationUpdate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("OnLocationListener","Location Updating");
            SocketTest.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateCount++;
                    socket_tv.setText("Location Updated: "+UpdateCount);
                }
            });

        }
    };

    private Emitter.Listener onBookingEnd = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            SocketTest.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socket_tv.setText("Booking Ended");
                }
            } );
        }
    };
    public void resetSocket(){
        if(socket!=null){
            Log.i("Reset:","Disconnecting socket");
            socket.off("aborted:Booking", onBookingAborted);
            socket.off("end:Booking",onBookingEnd);
            socket.off("locationUpdate:Booking",onLocationUpdate);
            socket.disconnect();
            socket.close();
            socket=null;
        }
        initSocket(blmod.getId());
    }
}
