package com.cbit.disha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.cbit.disha.Constants.AGE;
import static com.cbit.disha.Constants.EXTRA;
import static com.cbit.disha.Constants.FEMALE;
import static com.cbit.disha.Constants.FIRST;
import static com.cbit.disha.Constants.GENDER;
import static com.cbit.disha.Constants.LAST;
import static com.cbit.disha.Constants.MALE;
import static com.cbit.disha.Constants.NAME;
import static com.cbit.disha.Constants.OTHERS;
import static com.cbit.disha.Constants.PHONE;
import static com.cbit.disha.Constants.USERS;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth myAuth;
    private DatabaseReference userRef;
    private String userId, extra;
    private Button saveProfileButton;
    private EditText userName, userAge;
    private RadioGroup userGender;
    private LoadingBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        extra = getIntent().getStringExtra(EXTRA);
        initializeFields();
        retrieveProfile();
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });
    }


    private void retrieveProfile() {
        loadingBar.showLoadingBar(1);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userName.setText(snapshot.child(NAME).getValue().toString());
                    userAge.setText(snapshot.child(AGE).getValue().toString());
                    userGender.check(getGenderId(snapshot.child(GENDER).getValue().toString()));
                    loadingBar.dismissLoadingBar();
                } else
                    loadingBar.dismissLoadingBar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveProfile() {
        final String name = userName.getText().toString().trim();
        String age = userAge.getText().toString().trim();
        String gender = getGender(userGender.getCheckedRadioButtonId());
        final String phone = myAuth.getCurrentUser().getPhoneNumber();
        if (name.isEmpty())
            userName.setError(getResources().getString(R.string.enter_full_name));
        else if (age.isEmpty())
            userAge.setError(getResources().getString(R.string.enter_age));
        else if (gender.isEmpty())
            Toast.makeText(this, getResources().getString(R.string.select_gender), Toast.LENGTH_SHORT).show();
        else if (!CheckInternetConnection.isNetworkConnected(ProfileActivity.this))
            Toast.makeText(this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
        else {
            loadingBar.showLoadingBar(4);
            Map userDetails = new HashMap();
            userDetails.put(NAME, name);
            userDetails.put(AGE, age);
            userDetails.put(GENDER, gender);
            userDetails.put(PHONE, phone);
            userRef.updateChildren(userDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    loadingBar.dismissLoadingBar();
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
                        editor.putBoolean(MainActivity.HasProfile, true);
                        editor.putString(NAME,name);
                        String newPhone = phone.substring(0,3)+" "+phone.substring(3);
                        editor.putString(PHONE,newPhone);
                        editor.commit();
                        if (extra.equals(LAST)) {
                            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                        }
                        sendUserToMainActivity();
                    } else {
                        if (task.getException() instanceof FirebaseNetworkException)
                            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent homeIntent = new Intent(ProfileActivity.this, MainActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    private String getGender(int genderId) {
        switch (genderId) {
            case R.id.male:
                return MALE;
            case R.id.female:
                return FEMALE;
            case R.id.others:
                return OTHERS;
        }
        return "";
    }
    private int getGenderId(String genderName) {
        switch (genderName) {
            case MALE:
                return R.id.male;
            case FEMALE:
                return R.id.female;
            case OTHERS:
                return R.id.others;
        }
        return 0;
    }

    private void initializeFields() {
        if (extra.equals(FIRST)) {
            getSupportActionBar().setTitle(getResources().getString(R.string.profile));
        } else {
            getSupportActionBar().setTitle(getResources().getString(R.string.edit_profile));
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        myAuth = FirebaseAuth.getInstance();
        userId = myAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child(USERS).child(userId);
        saveProfileButton = findViewById(R.id.save_profile_button);
        userName = findViewById(R.id.user_name);
        userAge = findViewById(R.id.user_age);
        userGender = findViewById(R.id.user_gender);
        loadingBar = new LoadingBar(ProfileActivity.this);
    }
}