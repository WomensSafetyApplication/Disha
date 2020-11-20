package com.cbit.disha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FileAComplaint extends AppCompatActivity {

    private EditText complaintText;
    private Button complaintButton;
    private DatabaseReference complaintRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_a_complaint);
        getSupportActionBar().setTitle(getResources().getString(R.string.file_complaint));
        initializeFields();
        complaintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = complaintText.getText().toString().trim();
                if(text.isEmpty()) {
                    Toast.makeText(FileAComplaint.this, "Enter complaint", Toast.LENGTH_SHORT).show();
                    return;
                }
                complaintRef.push().child("Complaint").setValue(text).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        Toast.makeText(FileAComplaint.this, "Complaint successfully registered", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(FileAComplaint.this, "Error! Try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initializeFields() {
        complaintButton = findViewById(R.id.complaint_btn);
        complaintText = findViewById(R.id.complaint_text);
        complaintRef = FirebaseDatabase.getInstance().getReference().child("Complaints").child(FirebaseAuth.getInstance().getUid());
    }
}