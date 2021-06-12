package com.example.datagatheringappv2;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class foregroundService extends Service {
    static boolean startedOnce=false;
    PeriodicWorkRequest periodicWorkRequest;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intent1 = new Intent(this, sensorWorker.class);
        Intent intentNotif = new Intent(this, MainActivity.class);
        PendingIntent piNotif = PendingIntent.getActivity(this, 0, intentNotif, 0);

        Log.d("OnStartCommand:", "Reached");

        PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, intent1, 0);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "MY SERVICE APP")
                .setContentTitle("Foreground Service")
                .setContentText("MY TEXT NOTIF")
                .setContentIntent(piNotif)
                .build();

        startForeground(1, notification);


        long mills = timeSettingForAlarm(15, 0, 0);
        WorkManager workManager=WorkManager.getInstance(getApplicationContext());
        WorkInfo.State currentWorkState=getStateOfWork();
        startServerWork(mills);


        boolean oneTime=false;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//
//
//            periodicWorkRequest = new PeriodicWorkRequest.Builder(sensorWorker.class, 24, TimeUnit.HOURS)
//                    .setInitialDelay(mills-System.currentTimeMillis(),TimeUnit.MILLISECONDS)
//                    .addTag("StartingPeriodic")
//                    .build();
//
//            workManager.enqueueUniquePeriodicWork("StartingPeriodic", ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest);
//            startedOnce=true;
//        }
//        else if (startedOnce)
//        {
//            startedOnce=false;
//            workManager.cancelWorkById(periodicWorkRequest.getId());
//            periodicWorkRequest=new PeriodicWorkRequest.Builder(sensorWorker.class, 24, TimeUnit.HOURS)
//                    .setInitialDelay(mills-System.currentTimeMillis(),TimeUnit.MILLISECONDS)
//                    .addTag("StartingPeriodic")
//                    .build();
//            workManager.enqueueUniquePeriodicWork("StartingPeriodic", ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest);
//        }
//        Log.d("OnStartCommand:", "Reached22222");




        return START_STICKY;

    }
    private void createWorkRequest(long mills) {
        Log.d("OnStartCommand:", "Reached22222");
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder
                (sensorWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInitialDelay(mills-System.currentTimeMillis(),TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("sendLocation", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
    }

    // Then i'm learning the state of Work
    private WorkInfo.State getStateOfWork() {
        try {
            if (WorkManager.getInstance(this).getWorkInfosForUniqueWork("StartingPeriodic").get().size() > 0) {
                return WorkManager.getInstance(this).getWorkInfosForUniqueWork("StartingPeriodic")
                        .get().get(0).getState();
                // this can return WorkInfo.State.ENQUEUED or WorkInfo.State.RUNNING
                // you can check all of them in WorkInfo class.
            } else {
                return WorkInfo.State.CANCELLED;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            return WorkInfo.State.CANCELLED;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return WorkInfo.State.CANCELLED;
        }
    }

    // If work not ( ENQUEUED and RUNNING ) i'm running the work.
// You can check with other ways. It's up to you.
    private void startServerWork(long mills) {
        if (getStateOfWork() != WorkInfo.State.ENQUEUED && getStateOfWork() != WorkInfo.State.RUNNING) {
            createWorkRequest(mills);
            Log.wtf("startLocationUpdates", ": server started");
        } else {
            Log.wtf("startLocationUpdates", ": server already working");
        }
    }
    public long timeSettingForAlarm(int hour,int minute,int second)
    {
        Calendar calendar=Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,second);
        Date d=calendar.getTime();
        long millis=d.getTime();
        long currentMillis=System.currentTimeMillis();
        while(currentMillis>millis)
        {
            millis=millis+24*60*60*1000;
        }
        Log.d("TIME CURRENT MILLIS",Long.toString(currentMillis));
        Log.d("TIME SET TIME MILLIS",Long.toString(millis));

        return millis;
    }
    @Override
    public void onCreate() {
        Log.d("OnCreate:","Reached");
        createNotificationChannel();
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        Log.d("OnDestroy:","Reached");
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "MY SERVICE APP",
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
