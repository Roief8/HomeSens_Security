package com.example.homesens;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class Sensor implements Parcelable, Serializable {
    private int num;
    private String name, sID;
    private boolean open;


    public Sensor(int num,String name, String sID, boolean open) {
        this.num=num;
        this.name = name;
        this.sID = sID;
        this.open = open;



    }


    protected Sensor(Parcel in) {
        num = in.readInt();
        name = in.readString();
        sID = in.readString();
        open = in.readByte() != 0;

    }

    public static final Creator<Sensor> CREATOR = new Creator<Sensor>() {
        @Override
        public Sensor createFromParcel(Parcel in) {
            return new Sensor(in);
        }

        @Override
        public Sensor[] newArray(int size) {
            return new Sensor[size];
        }
    };


    public String getsID() {
        return sID;
    }

    public void setsID(String sID) {
        this.sID = sID;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(num);
        parcel.writeString(name);
        parcel.writeString(sID);
        parcel.writeByte((byte) (open ? 1 : 0));
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "num=" + num +
                ", sID=" + sID +
                ", name='" + name + '\'' +
                ", open=" + open +
                '}';
    }


}
