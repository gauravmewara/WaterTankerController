package com.example.watertankercontroller.Utils;

import org.json.JSONObject;

public interface FetchDataListener {
    void onFetchComplete(JSONObject data);

    void onFetchFailure(String msg);

    void onFetchStart();
}
