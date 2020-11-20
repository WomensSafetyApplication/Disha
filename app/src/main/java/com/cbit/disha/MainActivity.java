package com.cbit.disha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.cbit.disha.Constants.EXTRA;
import static com.cbit.disha.Constants.FAV;
import static com.cbit.disha.Constants.FIRST;
import static com.cbit.disha.Constants.NAME;
import static com.cbit.disha.Constants.PHONE;
import static com.cbit.disha.Constants.USERS;

public class MainActivity extends AppCompatActivity {

    private LinearLayout emergency, tipsForWomen, favContacts, makeComplaint, helpMe;
    public static SharedPreferences sharedPreferences;
    public static final String HasProfile = "profile";
    public static final String MySharedPreferences = "MyPrefs";
    private FirebaseAuth myAuth;
    private static final int LOCATION_REQUEST_CODE = 9579;
    private DatabaseReference contactsRef;
    private static final int SMS_REQUEST_CODE = 9759;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeFields();
        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EmergencyActivity.class));
            }
        });
        tipsForWomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TipsForWomenActivity.class));
            }
        });
        favContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FavoriteContacts.class));
            }
        });
        helpMe.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED) {
                    locationEnabled();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                }
                return true;
            }
        });
        makeComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FileAComplaint.class));
            }
        });
    }

    private void locationEnabled() {
        if((ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.SEND_SMS)) == PackageManager.PERMISSION_GRANTED){
            if(checkLocationEnabled()) {
                final LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(3000);
                locationRequest.setFastestInterval(5000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
                        locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);
                                if (locationResult != null && locationResult.getLocations().size() > 0) {
                                    int locationIndex = locationResult.getLocations().size() - 1;
                                    Location location = locationResult.getLocations().get(locationIndex);
                                    sendMessages(location);
                                }
                            }

                        }, Looper.getMainLooper());
            }
            else{
                showLocationOn();
            }
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_REQUEST_CODE);
        }
    }

    private void sendMessages(Location location) {
        String mapUrl = "http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    Toast.makeText(MainActivity.this, "No contacts added!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int t_count = 0, count = 0;
                String name, phone;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    ++t_count;
                    name = dataSnapshot.child(NAME).getValue(String.class);
                    phone = dataSnapshot.child(PHONE).getValue(String.class);
                    if(sendSms(name, phone, mapUrl)) ++count;
                }
                Toast.makeText(MainActivity.this, count + "/" + t_count + " messages are sent...", Toast.LENGTH_SHORT).show();
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(200);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean sendSms(String name, String phone, String url) {
        try {
            SmsManager smgr = SmsManager.getDefault();
            smgr.sendTextMessage(phone, null, "Alert! " + name + "\nI'm in risk now. Please help me through my location :\n\n" + url, null, null);
            return true;
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void helping() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(5000,10));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(5000);
        }
    }

    private void initializeFields() {
        myAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(MySharedPreferences, MODE_PRIVATE);
        emergency = findViewById(R.id.emergency);
        tipsForWomen = findViewById(R.id.tips_for_women);
        favContacts = findViewById(R.id.favorite_cont);
        makeComplaint = findViewById(R.id.make_complaint);
        helpMe = findViewById(R.id.help_me);
        contactsRef = FirebaseDatabase.getInstance().getReference().child(USERS).child(myAuth.getUid()).child(FAV);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!sharedPreferences.getBoolean(HasProfile, false)) {
            sendUserToProfileActivity(FIRST);
        }
    }

    private void sendUserToProfileActivity(String s) {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        if (s.equals(FIRST))
            profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        profileIntent.putExtra(EXTRA, s);
        startActivity(profileIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationEnabled();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                new android.app.AlertDialog.Builder(MainActivity.this).setMessage(getResources().getString(R.string.denied_location))
                        .setCancelable(true)
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }).create().show();
            }
        }
        if (requestCode == SMS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationEnabled();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            } else {
                new android.app.AlertDialog.Builder(MainActivity.this).setMessage(getResources().getString(R.string.denied_location))
                        .setCancelable(true)
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }).create().show();
            }
        }
    }
    private boolean checkLocationEnabled() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            return false;
        }
        return true;
    }

    private void showLocationOn() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("Enable Location and Try again")
                .setPositiveButton("Settings", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }
}