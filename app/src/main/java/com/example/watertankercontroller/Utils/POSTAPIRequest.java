package com.example.watertankercontroller.Utils;
import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class POSTAPIRequest {

    public void request(final Context context, final FetchDataListener listener, final String ApiURL, final HeadersUtil headparam ) throws JSONException {

        if (listener != null) {
            listener.onFetchStart();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                ApiURL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Response", String.valueOf(response));
                try {
                    if (response.getInt("error") == 0) {
                        listener.onFetchComplete(response);
                    } else {
                        listener.onFetchFailure(response.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                if (error instanceof NoConnectionError) {
                    listener.onFetchFailure("Network Connectivity Problem");
                } else if (error.networkResponse != null && error.networkResponse.data != null) {
                    VolleyError volley_error = new VolleyError(new String(error.networkResponse.data));
                    String errorMessage = "";
                    try {
                        JSONObject errorJson = new JSONObject(volley_error.getMessage().toString());
                        if (errorJson.has("error")) errorMessage = errorJson.getString("errorCode");
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
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                if(headparam.getAuthorization()!=null)
                    headers.put("Authorization",headparam.getAuthorization());
                return headers;
            }

        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueService.getInstance(context).addToRequestQueue(jsonObjectRequest.setShouldCache(false));
    }


    public void request(final Context context, final FetchDataListener listener, final String ApiURL, final HeadersUtil headparam, JSONObject params ) throws JSONException {

        if (listener != null) {
            listener.onFetchStart();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                ApiURL, params,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Response", String.valueOf(response));
                try {
                    if (response.getInt("error") == 0) {
                        listener.onFetchComplete(response);
                    } else {
                        listener.onFetchFailure(response.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                if (error instanceof NoConnectionError) {
                    listener.onFetchFailure("Network Connectivity Problem");
                } else if (error.networkResponse != null && error.networkResponse.data != null) {
                    VolleyError volley_error = new VolleyError(new String(error.networkResponse.data));
                    String errorMessage = "";
                    try {
                        JSONObject errorJson = new JSONObject(volley_error.getMessage().toString());
                        if (errorJson.has("error")) errorMessage = errorJson.getString("errorCode");
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
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                if(headparam.getAuthorization()!=null)
                    headers.put("Authorization",headparam.getAuthorization());
                return headers;
            }

        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueService.getInstance(context).addToRequestQueue(jsonObjectRequest.setShouldCache(false));
    }

}
