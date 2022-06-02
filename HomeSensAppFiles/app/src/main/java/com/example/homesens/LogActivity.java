package com.example.homesens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class LogActivity extends AppCompatActivity{

    private LogAdapter logAd;
    private ArrayList<Log> logs;
    private FirebaseFirestore db;
    private Spinner yearsDropdown, typeDropdown;
    Integer[] years = new Integer[]{2020,2021,2022};
    String[] types = new String[]{"Activate","Disable","Alarm"};


    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this activity show the log events of the system.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        //set the recycle view for the logs data
        recyclerView = findViewById(R.id.log);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set the logs data in adapter for the UI.
        logs = new ArrayList<>();
        logAd = new LogAdapter(logs);
        recyclerView.setAdapter(logAd);

        //set the dropdowns settings.
        yearsDropdown = findViewById(R.id.spinner1);
        typeDropdown = findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,types);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,years);
        yearsDropdown.setAdapter(adapter);
        typeDropdown.setAdapter(adapter2);


        //set the database and read the events.
        db = FirebaseFirestore.getInstance();
        db.collection("log").orderBy("date",Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot doc:list){
                            Log event = doc.toObject(Log.class);
                            logs.add(event);
                        }
                        logAd.notifyDataSetChanged();
                    }
                });

        //set the years dropdown button.
        yearsDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Integer newItem = (Integer) yearsDropdown.getSelectedItem();
                yearsFilter(newItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //set the type dropdown button.
        typeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String newItem = typeDropdown.getSelectedItem().toString();
                typesFilter(newItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    //this method filter the log database by years.
    public void yearsFilter(int year) {
        logs.clear();

        db.collection("log").orderBy("date", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc : list) {
                            Log event = doc.toObject(Log.class);
                            int eventYear = event.getDate().getYear() + 1900;
                            if (eventYear == year) {
                                logs.add(event);
                            }


                        }
                        logAd.notifyDataSetChanged();
                    }
                });
    }

    //this method filter the log database by type.
        public void typesFilter(String type){
            logs.clear();

            db.collection("log").orderBy("date", Query.Direction.DESCENDING).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot doc : list) {
                                Log event = doc.toObject(Log.class);
                                if (event.getType().equals(type)) {
                                    logs.add(event);
                                }


                            }
                            logAd.notifyDataSetChanged();
                        }
                    });


        }



}