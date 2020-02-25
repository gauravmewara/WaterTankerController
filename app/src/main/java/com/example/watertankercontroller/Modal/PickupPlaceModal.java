package com.example.watertankercontroller.Modal;

import android.os.Parcel;
import android.os.Parcelable;

public class PickupPlaceModal implements Parcelable {
    String placeid,locationname,locationaddress,latitude,longitude;
    public PickupPlaceModal(){}

    public String getPlaceid() {
        return placeid;
    }

    public void setPlaceid(String placeid) {
        this.placeid = placeid;
    }

    public String getLocationname() {
        return locationname;
    }

    public void setLocationname(String locationname) {
        this.locationname = locationname;
    }

    public String getLocationaddress() {
        return locationaddress;
    }

    public void setLocationaddress(String locationaddress) {
        this.locationaddress = locationaddress;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(placeid);
        parcel.writeString(locationname);
        parcel.writeString(locationaddress);
        parcel.writeString(latitude);
        parcel.writeString(longitude);
    }

    protected PickupPlaceModal(Parcel in) {
        placeid = in.readString();
        locationname = in.readString();
        locationaddress = in.readString();
        latitude = in.readString();
        longitude = in.readString();
    }

    public static final Creator<PickupPlaceModal> CREATOR = new Creator<PickupPlaceModal>() {
        @Override
        public PickupPlaceModal createFromParcel(Parcel in) {
            return new PickupPlaceModal(in);
        }

        @Override
        public PickupPlaceModal[] newArray(int size) {
            return new PickupPlaceModal[size];
        }
    };

    public boolean isLocationSame(PickupPlaceModal place2){
        if(this.latitude.equals(place2.getLatitude())&& this.longitude.equals(place2.getLongitude())){
            return true;
        }
        return false;
    }
}
