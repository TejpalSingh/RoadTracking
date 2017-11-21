package org.usroads.roadconditiontracking;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Button b1;
    boolean start = false;
    private SensorManager mSensorManager;
    private Sensor acc, gyro;
    AlertDialog.Builder alertDialog;
    boolean acc1 = true, gyro1 = false;
    ArrayList<Float> x_acc, y_acc, z_acc, x_gyro, y_gyro, z_gyro;
    ArrayList<Double> latitude, longitude;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 500;  /* 1/2 secs */
    private long FASTEST_INTERVAL = 500; /* 1/2 sec */
    ArrayList<String> time;
    Double lat = 0d;
    Double lng = 0d;
    TextView text;
    Random rand;
    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button) findViewById(R.id.button);
        text = (TextView) findViewById(R.id.text);
        rand = new Random();
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.ping);
        Log.e("time", DateFormat.getDateTimeInstance().format(new Date()));
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = !start;
                b1.setText(start ? "Stop" : "Start");
                if (start) {
                    handler.postDelayed(start_thread, 5000);
                } else {
                    recordData(false);
                    handler.removeCallbacksAndMessages(null);


                }


            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        startLocationUpdates();


    }

    Handler handler = new Handler();
    Runnable start_thread = new Runnable() {
        @Override
        public void run() {
            recordData(true);
            //handler.postDelayed(stop_thread, 300000);
        }
    };
    Runnable stop_thread = new Runnable() {
        @Override
        public void run() {
            recordData(false);
        }
    };

    private SensorEventListener accSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:


                    //Log.e("ACCELEROMETER ", "X " + event.values[0] + " " + "Y " + event.values[1] + " " + "Z " + event.values[2] + " ");
                    x_acc.add(event.values[0]);
                    y_acc.add(event.values[1]);
                    z_acc.add(event.values[2]);
                    latitude.add(lat);
                    longitude.add(lng);
                    time.add(DateFormat.getDateTimeInstance().format(new Date()));

                    if (event.values[1] > 14) {
                        Log.e("Y", event.values[1] + "");

                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                    }


                    break;

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Log.d("MY_APP", sensor.toString() + " - " + accuracy);
        }
    };

    public void recordData(boolean b) {

        if (b) {
            if (acc != null) {
                x_acc = new ArrayList<>();
                y_acc = new ArrayList<>();
                z_acc = new ArrayList<>();
                latitude = new ArrayList<>();
                longitude = new ArrayList<>();
                time = new ArrayList<>();
                mSensorManager.registerListener(accSensorListener, acc, SensorManager.SENSOR_DELAY_GAME);
                Log.e("log", "Started Recoding");
                //mSensorManager.registerListener(gyroSensorListener, gyro, SensorManager.SENSOR_DELAY_FASTEST);
            }
        } else {
            if (acc != null) {
                mSensorManager.unregisterListener(accSensorListener);
                //mSensorManager.unregisterListener(gyroSensorListener);
                Log.e("Size ", "acc " + x_acc.size());
                Log.e("log", "Recoding Stoped");

                write();
//                alertDialog = new AlertDialog.Builder(MainActivity.this);
//                alertDialog.setTitle("Activity Name");
//                alertDialog.setMessage("Enter Name");
//
//                final EditText input = new EditText(MainActivity.this);
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT);
//                input.setLayoutParams(lp);
//                alertDialog.setView(input);
//                alertDialog.setPositiveButton("Save",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//
//
//                            }
//                        });
//
//                alertDialog.show();


            }
        }

    }


    public void write() {
        try {
            CSVWriter writer1, writer2;
            FileWriter mFileWriter1, mFileWriter2;
            String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String fileName1 = "MyRoad" + rand.nextInt(1000000000) + ".csv";
            String filePath1 = baseDir + File.separator + fileName1;
            File f1 = new File(filePath1);
            //File f2 = new File(filePath2);
            if (f1.exists() && !f1.isDirectory()) {
                mFileWriter1 = new FileWriter(filePath1, true);
                writer1 = new CSVWriter(mFileWriter1);
            } else {
                writer1 = new CSVWriter(new FileWriter(filePath1));

            }

            for (int i = 0; i < x_acc.size(); i++) {
                String[] acc_data = {time.get(i), x_acc.get(i).toString(), y_acc.get(i).toString(), z_acc.get(i).toString(), latitude.get(i).toString(), longitude.get(i).toString()};
                writer1.writeNext(acc_data);
            }
            writer1.close();

        } catch (Exception e) {

        }

    }


    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Log.e("Location", locationResult.toString());
                        String geo = String.valueOf(locationResult.getLastLocation().getLatitude()) + "  " + String.valueOf(locationResult.getLastLocation().getLongitude());
                        lat = locationResult.getLastLocation().getLatitude();
                        lng = locationResult.getLastLocation().getLongitude();
                        text.setText(geo);
                    }
                },
                Looper.myLooper());
    }

}

