package com.example.homesens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SensorActivity extends AppCompatActivity {

    private TextView nameTV, statusTV;
    private RecyclerView recyclerView;
    private LogAdapter logAd;
    private ArrayList<Log> logs;
    FirebaseFirestore db;
    ImageView door;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        //sensor load
        Sensor sens = (Sensor) getIntent().getParcelableExtra("Sensor");

        //database load
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference active = database.getReference("activate");

        //set the views for the UI.
        nameTV = findViewById(R.id.sName);
        statusTV = findViewById(R.id.sensor_status);
        nameTV.setText(sens.getName().toString());
        door = findViewById(R.id.door);



        dataCheck(sens,database);


        //set the log for the sensor
        recyclerView = findViewById(R.id.sensorlog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set the log adapter for the UI.
        logs = new ArrayList<>();
        logAd = new LogAdapter(logs);
        recyclerView.setAdapter(logAd);

        //read the sensor log data from the database.
        db = FirebaseFirestore.getInstance();
        db.collection("log").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot doc:list) {
                            Log event = doc.toObject(Log.class);
                            if (event.getSensor() != null) {
                                if (event.getSensor().equals(sens.getName())) {
                                    event.setSensor(sens.getName());
                                    logs.add(event);
                                }
                            }
                        }
                        logAd.notifyDataSetChanged();
                    }
                });




    }


    //this method check the status of the system
    protected void statusCheck(boolean open){

        if (open){
            statusTV.setText("Open");
            statusTV.setTextColor(Color.GREEN);
            door.setImageResource(R.drawable.alarm_door);

        }
        else {
            statusTV.setText("Closed");
            statusTV.setTextColor(Color.RED);
            door.setImageResource(R.drawable.closedoor);
        }

    }

    //this method is set up alarm if the sensor is open and the system is active.
    protected void alarm(FirebaseDatabase db,boolean open){

        DatabaseReference active=db.getReference("activate");
        Animation animation = new AlphaAnimation(1, 0); //to change visibility from visible to invisible
        animation.setDuration(1500); //1 second duration for each animation cycle
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE); //repeating indefinitely
        animation.setRepeatMode(Animation.REVERSE);

        active.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean active = snapshot.getValue(boolean.class);
                if (active &&open){
                    door.startAnimation(animation); //to start animation
                    statusTV.setText("Warning! \n"+nameTV.getText()+" is open.");
                    statusTV.setTextColor(Color.parseColor("#FE2E2E"));
                    statusTV.startAnimation(animation);
                }
                else {door.clearAnimation();
                     statusTV.clearAnimation();
                     statusCheck(open);}


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

    }


    //this method check the data of the presented sensor on the screen.
    protected void dataCheck(Sensor sens, FirebaseDatabase database ){

        database.getReference("Sensors").
            child(sens.getsID()).child("open" +
                "").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                boolean value = dataSnapshot.getValue(boolean.class);
                statusCheck(value);
                alarm(database,value);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });



    }

}

