package com.example.watertankercontroller.Modal;

public class NotificationModal {
    String notifiactionid,bookingid,tankerid,title,text,notificationtype,isread;

    public String getIsread() {
        return isread;
    }

    public void setIsread(String isread) {
        this.isread = isread;
    }

    public String getNotifiactionid() {
        return notifiactionid;
    }

    public void setNotifiactionid(String notifiactionid) {
        this.notifiactionid = notifiactionid;
    }

    public String getBookingid() {
        return bookingid;
    }

    public void setBookingid(String bookingid) {
        this.bookingid = bookingid;
    }

    public String getTankerid() {
        return tankerid;
    }

    public void setTankerid(String tankerid) {
        this.tankerid = tankerid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNotificationtype() {
        return notificationtype;
    }

    public void setNotificationtype(String notificationtype) {
        this.notificationtype = notificationtype;
    }
}
