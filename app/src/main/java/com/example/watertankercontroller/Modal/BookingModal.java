package com.example.watertankercontroller.Modal;

import android.os.Parcel;
import android.os.Parcelable;

public class BookingModal implements Parcelable {
    String bookingid,distance,fromlocation,tolocation,fromtime,totime;
    String fromlatitude,fromlongitude,tolatitude,tolongitude,bookingtype;
    String phonecode,phone,pickuppointid,controllerid,bookedby,message,drivername,controller_name;
    String controllerBooking_id;







    public BookingModal(){}

    public String getController_name() {
        return controller_name;
    }

    public void setController_name(String controller_name) {
        this.controller_name = controller_name;
    }

    public String getDrivername() {
        return drivername;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhonecode() {
        return phonecode;
    }

    public void setPhonecode(String phonecode) {
        this.phonecode = phonecode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPickuppointid() {
        return pickuppointid;
    }

    public void setPickuppointid(String pickuppointid) {
        this.pickuppointid = pickuppointid;
    }

    public String getControllerid() {
        return controllerid;
    }

    public void setControllerid(String controllerid) {
        this.controllerid = controllerid;
    }

    public String getBookedby() {
        return bookedby;
    }

    public void setBookedby(String bookedby) {
        this.bookedby = bookedby;
    }

    public String getBookingtype() {
        return bookingtype;
    }

    public void setBookingtype(String bookingtype) {
        this.bookingtype = bookingtype;
    }

    public String getFromlatitude() {
        return fromlatitude;
    }

    public void setFromlatitude(String fromlatitude) {
        this.fromlatitude = fromlatitude;
    }

    public String getFromlongitude() {
        return fromlongitude;
    }

    public void setFromlongitude(String fromlongitude) {
        this.fromlongitude = fromlongitude;
    }

    public String getTolatitude() {
        return tolatitude;
    }

    public void setTolatitude(String tolatitude) {
        this.tolatitude = tolatitude;
    }

    public String getTolongitude() {
        return tolongitude;
    }

    public void setTolongitude(String tolongitude) {
        this.tolongitude = tolongitude;
    }

    public String getBookingid() {
        return bookingid;
    }

    public void setBookingid(String bookingid) {
        this.bookingid = bookingid;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getFromlocation() {
        return fromlocation;
    }

    public void setFromlocation(String fromlocation) {
        this.fromlocation = fromlocation;
    }

    public String getTolocation() {
        return tolocation;
    }

    public void setTolocation(String tolocation) {
        this.tolocation = tolocation;
    }

    public String getFromtime() {
        return fromtime;
    }

    public void setFromtime(String fromtime) {
        this.fromtime = fromtime;
    }

    public String getTotime() {
        return totime;
    }

    public void setTotime(String totime) {
        this.totime = totime;
    }

    public String getControllerBooking_id() { return controllerBooking_id; }
    public void setControllerBooking_id(String controllerBooking_id) { this.controllerBooking_id = controllerBooking_id; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bookingid);
        parcel.writeString(distance);
        parcel.writeString(fromlocation);
        parcel.writeString(tolocation);
        parcel.writeString(fromtime);
        parcel.writeString(totime);
        parcel.writeString(fromlatitude);
        parcel.writeString(fromlongitude);
        parcel.writeString(tolatitude);
        parcel.writeString(tolongitude);
        parcel.writeString(bookingtype);
        parcel.writeString(phonecode);
        parcel.writeString(phone);
        parcel.writeString(pickuppointid);
        parcel.writeString(controllerid);
        parcel.writeString(bookedby);
        parcel.writeString(message);
        parcel.writeString(drivername);
        parcel.writeString(controller_name);
    }

    protected BookingModal(Parcel in){
        bookingid = in.readString();
        distance = in.readString();
        fromlocation = in.readString();
        tolocation = in.readString();
        fromtime = in.readString();
        totime = in.readString();
        fromlatitude = in.readString();
        fromlongitude = in.readString();
        tolatitude = in.readString();
        tolongitude = in.readString();
        bookingtype = in.readString();
        phonecode = in.readString();
        phone = in.readString();
        pickuppointid = in.readString();
        controllerid = in.readString();
        bookedby = in.readString();
        message = in.readString();
        drivername = in.readString();
        controller_name = in.readString();
    }

    public static final Creator<BookingModal> CREATOR = new Creator<BookingModal>() {
        @Override
        public BookingModal createFromParcel(Parcel in) {
            return new BookingModal(in);
        }

        @Override
        public BookingModal[] newArray(int size) {
            return new BookingModal[size];
        }
    };
}
