package com.example.homesens;

import java.util.Date;

public class Log {
    private String sensor, type;
    private Date date;

    public Log() {}

    public Log(String sensor, String type, Date date) {
        this.sensor = sensor;
        this.type = type;
        this.date = date;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }


    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Log{" +
                "sensor='" + sensor + '\'' +
                ", type='" + type + '\'' +
                ", date=" + date +
                '}';
    }
}
