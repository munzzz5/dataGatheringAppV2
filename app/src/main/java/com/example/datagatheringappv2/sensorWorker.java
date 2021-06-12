package com.example.datagatheringappv2;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.SENSOR_SERVICE;
import static android.os.ParcelFileDescriptor.MODE_APPEND;

public class sensorWorker extends Worker implements SensorEventListener {
    SensorManager sensorManager;
    FileOutputStream fout;
    StringBuilder sb;
    public sensorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    long starttime;

    @NonNull

    @Override
    public Result doWork() {
        Log.d("SENSOR DOWORK!!!!!!!!!","REACHED");
        starttime=System.currentTimeMillis();
        //Log.d("ISSTOPPED???",String.valueOf(this.isStopped()));
        sb=new StringBuilder();
                sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("ISSTOPPED???",String.valueOf(this.isStopped()));


        return Result.success();

    }

    @Override
    public void onStopped() {
        Log.d("WORKER THREAD","STOPPING WORKER22222222222");
        //WorkManager.getInstance(getApplicationContext()).cancelWorkById(this.getId());
        sensorManager.unregisterListener(this);
        super.onStopped();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if(System.currentTimeMillis()-starttime<3000 && sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
        {


            Log.d("SENSOR VALUES", getAccelerometer(sensorEvent));
            sb.append(getAccelerometer(sensorEvent));
            Date d= Calendar.getInstance().getTime();
            sb.append("\n--------------"+d.toString()+"---------------------\n");

            Log.d("OUTPUT FOR STORAGE",sb.toString());

        }
        else if(System.currentTimeMillis()-starttime>=3000)
        {
            saveData(sb.toString());

            Log.d("WORKER THREAD","STOPPING WORKER");
            //WorkManager.getInstance(getApplicationContext()).cancelWorkById(this.getId());
            sb.delete(0,sb.length());
            onStopped();


        }
        try {
            Thread.sleep((1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    private void saveData(String accelData) {
        try {
            fout= getApplicationContext().openFileOutput("SensorDataKomplete.txt", Context.MODE_APPEND);

            OutputStreamWriter osw=new OutputStreamWriter(fout);
            osw.write("END OF RESPONSE"+"\n------------------------------\n"+accelData);
            osw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private String getAccelerometer(SensorEvent sensorEvent) {
        float[] values=sensorEvent.values;


        float x=values[0];
        float y=values[1];
        float z=values[2];
        String s="";
        if((x>5||x<-5) && y<2 && z<2){
            s="Screen facing you and horizontal phone";
        }
        else if((y>5||y<-5) && x<2 && z<2)
        {
            s="Screen facing you and verticle phone";
        }
        else if((z>5||z<-5) && y<2 && x<2){
            s="screen bottom//screen top";
        }
        else if(y>5 && z>5 && x<2)
        {
            s="Viewing angle!";
        }
        else if(x>5 && z>5 && y<2)
        {
            s="Movie Angle!";
        }
        s+="\nX="+x+"\nY="+y+"\nZ="+z;




        return s;



    }
}
