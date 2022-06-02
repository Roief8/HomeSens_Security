package com.example.homesens;

import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.myViewHolder> {

    ArrayList<Sensor> sensors;
    boolean active;

    public SensorAdapter(ArrayList<Sensor> sensors) {
        this.sensors = sensors;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sensoritem, parent, false);

        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        Sensor s = sensors.get(position);

        holder.name.setText(s.getName());
        holder.open.setImageResource(setPhoto(s));
        holder.open.setAnimation(setAnimation(s));
        holder.open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), SensorActivity.class);
                intent.putExtra("Sensor", (Parcelable) s);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    public void setActive(boolean active){
        this.active=active;
    }

    public Animation setAnimation(Sensor s) {
        Animation animation = new AlphaAnimation(1, 0); //to change visibility from visible to invisible
        animation.setDuration(1500); //1 second duration for each animation cycle
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE); //repeating indefinitely
        animation.setRepeatMode(Animation.REVERSE); //animation will start from end point once ended.

        if (s.isOpen()&&active) {
            return animation;
        }
        else return null;

    }


    public int setPhoto(Sensor s){

        if (s.isOpen()){
            return R.drawable.alarm_door;
        }
        else return R.drawable.closedoor;

    }

    @Override
    public int getItemCount() {
        return sensors.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        ImageButton open;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            open = itemView.findViewById(R.id.open);


        }


}

}
