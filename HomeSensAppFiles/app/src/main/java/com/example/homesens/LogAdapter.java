package com.example.homesens;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class LogAdapter extends RecyclerView.Adapter<LogAdapter.myViewHolder>{

    ArrayList<Log> logs;

    public LogAdapter(ArrayList<Log> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.logitem,parent,false);

        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        String time = new java.text.SimpleDateFormat("dd/MM/yyyy \n HH:mm:ss").format(logs.get(position).getDate());
        holder.date.setText(time);
        holder.sensor.setText(logs.get(position).getSensor());
        holder.type.setTextColor(setColors(logs.get(position).getType()));
        holder.type.setText(logs.get(position).getType());
    }

    public int setColors(String type){

        if (type.equals("Activate")){
            return Color.GREEN;
        }
        else return Color.RED;

    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder{
        TextView date, sensor, type;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            sensor = itemView.findViewById(R.id.sName);
            type = itemView.findViewById(R.id.type);
        }
    }



}