package com.example.appointmentscheduler.model;

public class QRLoginRequest {
    public String token;

    public QRLoginRequest() {
    }

    public QRLoginRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
