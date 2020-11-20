package com.cbit.disha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import static com.cbit.disha.Constants.EMERGENCY;
import static com.cbit.disha.Constants.FAV;
import static com.cbit.disha.Constants.USERS;

public class EmergencyActivity extends AppCompatActivity {

    private RecyclerView emergencyRecycler;
    private EmergencyAdapter adapter;
    private DatabaseReference emergencyRef;
    private FirebaseRecyclerOptions<EmergencyModel> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        getSupportActionBar().setTitle(getResources().getString(R.string.emergency));
        initializeFields();
    }

    private void initializeFields() {
        String uid = FirebaseAuth.getInstance().getUid();
        emergencyRecycler = findViewById(R.id.emergency_recycler_view);
        emergencyRecycler.setLayoutManager(new LinearLayoutManager(EmergencyActivity.this));
        emergencyRecycler.setHasFixedSize(true);
        emergencyRef = FirebaseDatabase.getInstance().getReference().child(EMERGENCY);
        options = new FirebaseRecyclerOptions.Builder<EmergencyModel>().setQuery(emergencyRef, EmergencyModel.class).build();
        adapter = new EmergencyAdapter(options);
        emergencyRecycler.setAdapter(adapter);
        adapter.startListening();
    }
}