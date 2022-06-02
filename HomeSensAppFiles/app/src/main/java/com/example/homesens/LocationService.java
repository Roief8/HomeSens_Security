package com.example.homesens;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class LocationService extends Service implements LocationListener {
    FirebaseDatabase db;
    DatabaseReference activeDB, locationDB;
    boolean geoTrace, active;
    LocationManager locationManager;
    int i;
    NotificationManager manager;
    NotificationManagerCompat notificationManagerCompat;
    Notification notification;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        db = FirebaseDatabase.getInstance();
        locationDB = db.getReference("Geo-Location").child("geoActive");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        status_check();
        getLocationStatus();
        i =0;

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }


    //this method check the status of the system.
    public void status_check() {

        activeDB = db.getReference("activate");
        activeDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                active = snapshot.getValue(boolean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //this method check the geo-trace status of the system.
    public void getLocationStatus() {

        locationDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                geoTrace = dataSnapshot.getValue(boolean.class);

                Handler handler = new Handler();
                if (geoTrace == false) {
                    handler.removeMessages(0);
                } else
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestLocation();
                            handler.postDelayed(this, 1000 * 60 * 5);
                        }
                    }, 1000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //this method request the current device location.
    public void requestLocation() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 10000, 1000, this);
            }

        }

    }

    //this method check' if the current location has changed.
    @Override
    public void onLocationChanged(@NonNull Location location) {

        if (!geoTrace) {
            locationManager.removeUpdates(this);
            locationManager = null;
        } else {
            locationCheck(location);
            locationManager.removeUpdates(this);
        }
    }


    //this method check the home location as set in the real time database,
    //then compare it to the current location in order to check the distance.
    public void locationCheck(Location location) {


        db.getReference("Geo-Location").child("homeLocation")       // check the home coordinates.
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double homeLat = snapshot.child("Latitude").getValue(Double.class);
                        Double homeLong = snapshot.child("Longitude").getValue(Double.class);

                        Location home = new Location("");
                        home.setLatitude(homeLat);
                        home.setLongitude(homeLong);
                        System.out.println("location : " + location.getLatitude() + ", " + location.getLongitude() + "\n" +
                                "Distance in meters : " + location.distanceTo(home));

                        if (!active && location.distanceTo(home) > 30) {        //if the system is inactive, and the distance getting closer.
                            activeDB.setValue(true);
                            sendNotification();
                            push();
                            Log.d("1", "Distance in meters :" + location.distanceTo(home)+" You are getting far from home! want to Activate System? ");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    //this method send push notification for the user if needed.
    public void sendNotification(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("HomeSens", "Geo-Trace", NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"HomeSens")
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle("Pay Attention!")
                .setContentText("Geo-Trace is on! \n");

        notification = builder.build();
        notificationManagerCompat = NotificationManagerCompat.from(this);

    }

    public void push(){

        notificationManagerCompat.notify(1,notification);
    }

}



