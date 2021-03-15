package com.example.watertankercontroller.Utils;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.watertankercontroller.Activity.CompletedActivity;
import com.example.watertankercontroller.Activity.SelectServer;
import com.example.watertankercontroller.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class GETAPIRequest {

    public void request(final Context context, final FetchDataListener listener, final String ApiURL) throws JSONException {
        if (listener != null) {
            listener.onFetchStart();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, ApiURL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("Response",response.toString());
                            if (listener != null) {
                                if(response.has("error")) {
                                    if (response.getInt("error") != 1) {
                                        listener.onFetchComplete(response);
                                    } else {
                                        listener.onFetchFailure(response.getString("errorCode"));
                                    }
                                }else{
                                    listener.onFetchComplete(response);
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    listener.onFetchFailure("Network Connectivity Problem");
                }else if(error instanceof TimeoutError){
                    listener.onFetchFailure("Request Timed Out");
                } else if (error instanceof AuthFailureError) {
                    showAlert(context);
                }else if (error.networkResponse != null && error.networkResponse.data != null) {
                    VolleyError volley_error = new VolleyError(new String(error.networkResponse.data));
                    String errorMessage      = "";
                    try {
                        JSONObject errorJson = new JSONObject(volley_error.getMessage().toString());
                        if(errorJson.has("error")) errorMessage = errorJson.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (errorMessage.isEmpty()) {
                        errorMessage = volley_error.getMessage();
                    }

                    if (listener != null) listener.onFetchFailure(errorMessage);
                } else {
                    listener.onFetchFailure("Something went wrong. Please try again later");
                    error.printStackTrace();
                }

            }
        });
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueService.getInstance(context).addToRequestQueue(postRequest.setShouldCache(false));
    }



    public void request(final Context context, final FetchDataListener listener, final String ApiURL,final HeadersUtil headparam) throws JSONException {
        if (listener != null) {
            listener.onFetchStart();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, ApiURL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("Response",response.toString());
                            if (listener != null) {
                                if(response.getInt("error")!= 1) {
                                    listener.onFetchComplete(response);
                                }else {
                                    listener.onFetchFailure(response.getString("error"));
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    listener.onFetchFailure("Network Connectivity Problem");
                }else if(error instanceof TimeoutError){
                    listener.onFetchFailure("Request Timed Out");
                }else if (error instanceof AuthFailureError) {
                   showAlert(context);
                }
                else if (error.networkResponse != null && error.networkResponse.data != null) {
                    VolleyError volley_error = new VolleyError(new String(error.networkResponse.data));
                    String errorMessage      = "";
                    try {
                        JSONObject errorJson = new JSONObject(volley_error.getMessage().toString());
                        if(errorJson.has("error")) errorMessage = errorJson.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (errorMessage.isEmpty()) {
                        errorMessage = volley_error.getMessage();
                    }

                    if (listener != null) listener.onFetchFailure(errorMessage);
                } else {
                    listener.onFetchFailure("Something went wrong. Please try again later");
                    error.printStackTrace();
                }

            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",headparam.getAuthorization());
                return headers;
            }

        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueService.getInstance(context).addToRequestQueue(postRequest.setShouldCache(false));
    }



    public void request(final Context context, final FetchDataListener listener, final String ApiURL, final HeadersUtil headparam, JSONObject params) throws JSONException {
        final String requestBody = params.toString();
        if (listener != null) {
            listener.onFetchStart();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, ApiURL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("Response",response.toString());
                            if (listener != null) {
                                if(response.getInt("error")!= 1) {
                                    listener.onFetchComplete(response);
                                }else {
                                    listener.onFetchFailure(response.getString("error"));
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    listener.onFetchFailure("Network Connectivity Problem");
                }else if(error instanceof TimeoutError){
                    listener.onFetchFailure("Request Timed Out");
                } else if (error instanceof AuthFailureError) {
                    showAlert(context);
                }else if (error.networkResponse != null && error.networkResponse.data != null) {
                    VolleyError volley_error = new VolleyError(new String(error.networkResponse.data));
                    String errorMessage      = "";
                    try {
                        JSONObject errorJson = new JSONObject(volley_error.getMessage().toString());
                        if(errorJson.has("error")) errorMessage = errorJson.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (errorMessage.isEmpty()) {
                        errorMessage = volley_error.getMessage();
                    }

                    if (listener != null) listener.onFetchFailure(errorMessage);
                } else {
                    listener.onFetchFailure("Something went wrong. Please try again later");
                    error.printStackTrace();
                }

            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                if(headparam.getAuthorization()!=null)
                    headers.put("Authorization",headparam.getAuthorization());
                return headers;
            }


            @Override
            public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody, "utf-8");
                    return null;
                }
            }

        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueService.getInstance(context).addToRequestQueue(postRequest.setShouldCache(false));
    }







    public void requestString(final Context context, final FetchDataListener listener, final String ApiURL,final HeadersUtil headparam) throws JSONException {
        if (listener != null) {
            listener.onFetchStart();
        }

        StringRequest postRequest = new StringRequest(Request.Method.GET, ApiURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("Response",response.toString());
                            /*if (listener != null) {
                                if(response.getInt("error")!= 1) {
                                    listener.onFetchComplete(response);
                                }else {
                                    listener.onFetchFailure(response.getString("error"));
                                }
                            }*/
                            JSONObject json = new JSONObject();
                            json.put("error",0);
                            listener.onFetchComplete(json);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    listener.onFetchFailure("Network Connectivity Problem");
                }else if(error instanceof TimeoutError){
                    listener.onFetchFailure("Request Timed Out");
                } else if (error instanceof AuthFailureError) {
                    showAlert(context);
                }else if (error.networkResponse != null && error.networkResponse.data != null) {
                    VolleyError volley_error = new VolleyError(new String(error.networkResponse.data));
                    String errorMessage      = "";
                    try {
                        JSONObject errorJson = new JSONObject(volley_error.getMessage().toString());
                        if(errorJson.has("error")) errorMessage = errorJson.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (errorMessage.isEmpty()) {
                        errorMessage = volley_error.getMessage();
                    }

                    if (listener != null) listener.onFetchFailure(errorMessage);
                } else {
                    listener.onFetchFailure("Something went wrong. Please try again later");
                    error.printStackTrace();
                }

            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",headparam.getAuthorization());
                return headers;
            }

        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueService.getInstance(context).addToRequestQueue(postRequest.setShouldCache(false));
    }


    public void  showAlert(final Context context){
        final DialogInterface.OnClickListener listner = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FirebaseAuth.getInstance().signOut();
                SharedPrefUtil.removePreferenceKey(context,Constants.SHARED_PREF_LOGIN_TAG,Constants.SERVER_IP);
                SharedPrefUtil.deletePreference(context, Constants.SHARED_PREF_LOGIN_TAG);
                SharedPrefUtil.deletePreference(context, Constants.SHARED_PREF_NOTICATION_TAG);


                Intent i = new Intent(context, SelectServer.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(i);
                Toast.makeText(context, "Due to unauthorized activity ,You are now logout", Toast.LENGTH_SHORT).show();
            }
        };
        final DialogInterface.OnDismissListener disListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                FirebaseAuth.getInstance().signOut();
                SharedPrefUtil.removePreferenceKey(context,Constants.SHARED_PREF_LOGIN_TAG,Constants.SERVER_IP);
                SharedPrefUtil.deletePreference(context, Constants.SHARED_PREF_LOGIN_TAG);
                SharedPrefUtil.deletePreference(context, Constants.SHARED_PREF_NOTICATION_TAG);

                Intent i = new Intent(context, SelectServer.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(i);
                Toast.makeText(context, "Due to unauthorized activity ,You are now logout", Toast.LENGTH_SHORT).show();
            }
        };
        RequestQueueService.showAlert("UnAuthorized Activity found", "Due to unauthorized activity ,You are now logout", context, listner, disListener);
    }
}
