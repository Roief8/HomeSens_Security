 package com.example.homesens;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 public class MainActivity extends AppCompatActivity implements LocationListener {

    boolean active,geoTrace;
    TextView statusTV, greetingTV;
    ImageButton activateBTN, locationBTN, logBTN, logOutBtn;
    RecyclerView recyclerView;
    ArrayList<Sensor> sensors;
    SensorAdapter sensorAD;
    final static String[] PERMISSIONS = {Manifest.permission.INTERNET,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.FOREGROUND_SERVICE};
    LocationManager locationManager;
    FirebaseDatabase db;
    DatabaseReference activeDB, locationDB, smsDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the Layout and Adapter for the Sensors.
        recyclerView = findViewById(R.id.RCV);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.hasFixedSize();
        sensors = new ArrayList<>();
        sensorAD = new SensorAdapter(sensors);
        recyclerView.setAdapter(sensorAD);

        //set the screen views
        greetingTV = findViewById(R.id.helloUser);
        statusTV = findViewById(R.id.systemStatus);
        activateBTN = findViewById(R.id.activateBtn);
        logBTN = findViewById(R.id.logBtn);
        locationBTN = findViewById(R.id.locationBTN);
        logOutBtn = findViewById(R.id.LogoutBtn);

        //load the database and sensors from the database.
        db = FirebaseDatabase.getInstance();
        setSensors();

        //method to greet the user with its name.
        greeting();

        //load the system status from database.
        activeDB = db.getReference("activate");
        locationDB = db.getReference("Geo-Location").child("geoActive");
        getLocationStatus(locationDB);
        statusCheck();

        //set the activate button for the database.
        activateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (active) {
                    activeDB.setValue(false);
                    writeToLog("Disable");

                } else {
                        activeDB.setValue(true);
                        writeToLog("Activate");

                }
            }
        });

        //set the log button.
        logBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                startActivity(intent);

            }
        });

        //set the location button and location services.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (geoTrace){
                    locationDB.setValue(false);

                }
                else {
                    locationDB.setValue(true);

                }

            }
        });

        //set the Log Out button.
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Thanks for using our app!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));

            }
        });

    }

    //this method get the user Name from the database, and present it on the app.
    protected void greeting() {

        db.getReference("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                greetingTV.setText("Hello, " + name + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //this method check the system's "active" status on real time database.
    protected void statusCheck() {

        activeDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                active = dataSnapshot.getValue(boolean.class);
                sensorAD.setActive(active);

                //set the activate button UI state.
                if (active) {
                    checkOpen();
                    statusTV.setText("Active");
                    statusTV.setTextColor(Color.GREEN);
                    activateBTN.setImageResource(R.drawable.offbtn);

                } else {
                    statusTV.setText("Deactivated");
                    statusTV.setTextColor(Color.RED);
                    activateBTN.setImageResource(R.drawable.onbtn);

                }
                sensorAD.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }

        });

    }

    //this method read the user sensor's data from the real time database,
    //turn the data into Sensor object and present it on the app UI.
    protected void setSensors() {

        db.getReference("Sensors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sensors.clear();
                sensorAD.setActive(active);
                for (DataSnapshot data : snapshot.getChildren()) {

                    int num = Integer.valueOf(data.child("num").getValue().toString());
                    String sName = data.child("name").getValue().toString();
                    String sID = data.child("sID").getValue().toString();
                    boolean sOpen = data.child("open").getValue(boolean.class);
                    Sensor s = new Sensor(num, sName, sID, sOpen);

                    sensors.add(s);

                }
                sensorAD.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //this method check if there is an active sensor, and pop up warning for the user.
    public void checkOpen(){
        List<String> openDoor = new ArrayList<>();

        for (Sensor s : sensors){
            smsDB = db.getReference("Sensors").child(s.getsID()).child("sms");
            if (s.isOpen()){
                openDoor.add(s.getName());
                smsDB.setValue(true);
            }
            else smsDB.setValue(false);
        }
        if (openDoor.size()>0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Pay Attention!");

            String doorNames = openDoor.toString().replace("[","").replace("]","");
            builder.setMessage("the following doors are open:\n" + doorNames + "\n" +
                    "please notice that the you will not receive messages on those doors. ");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            builder.create().show();

        }



    }

    //this method read the events log from the database,
    //turn it into Event object, and present on the app UI.
    public void writeToLog(String type) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new event
        Map<String, Object> event = new HashMap<>();
        Date currentTime = Calendar.getInstance().getTime();
        event.put("date", currentTime);
        event.put("sensor", "System");
        event.put("type", type);

        // Add a new document with a generated ID
        db.collection("log")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                    }
                });


    }


    //this method check the if the Geo Trace is active in the real time database.
    public void getLocationStatus(DatabaseReference locationDB) {

        locationDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                geoTrace = dataSnapshot.getValue(boolean.class);


                //set the activate button UI state.
                if (geoTrace) {
                    //check if permission needed.
                    if (Build.VERSION.SDK_INT >= 23){
                        requestPermissions(PERMISSIONS,1);
                        locationBTN.setImageResource(R.drawable.location_on);
                        startService(new Intent(getBaseContext(), ConnectionService.class));
                        startService(new Intent(getApplicationContext(), LocationService.class));

                        Toast.makeText(MainActivity.this,"Geo-Trace is on!",Toast.LENGTH_SHORT).show();

                                     }
                } else {
                    locationBTN.setImageResource(R.drawable.location_off);
                    Toast.makeText(MainActivity.this,"Geo-Trace is off!",Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }


    //if permission granted, calls for current location request method every 2 minutes.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //request location

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //startForegroundService(new Intent(getBaseContext(),ConnectionService.class));
                //startForegroundService(new Intent(getBaseContext(),LocationService.class));


            }

        }
    }


    //this method check' if the current location has changed.
    @Override
    public void onLocationChanged(@NonNull Location location) {

        if (!geoTrace){
            locationManager.removeUpdates(this);
            locationManager=null;
    }
        else {

            //locationCheck(location);
            locationManager.removeUpdates(this);
        }
    }



}