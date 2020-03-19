package com.example.watertankercontroller.Modal;

import java.util.HashMap;
import java.util.List;

public class MapDataParserModal {
    List<List<HashMap<String, String>>> routes;
    long duration,distance;

    public List<List<HashMap<String, String>>> getRoutes() {
        return routes;
    }

    public void setRoutes(List<List<HashMap<String, String>>> routes) {
        this.routes = routes;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }
}
