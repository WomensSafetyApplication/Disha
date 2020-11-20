package com.cbit.disha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import static com.cbit.disha.Constants.FAV;
import static com.cbit.disha.Constants.USERS;

public class FavoriteContacts extends AppCompatActivity {

    private RecyclerView contactRecycler;
    private ContactsAdapter adapter;
    private Button addContact;
    private DatabaseReference contactRef;
    private FirebaseRecyclerOptions<ContactModel> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_contacts);
        getSupportActionBar().setTitle(getResources().getString(R.string.fav_cont));
        initializeFields();
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(FavoriteContacts.this).inflate(R.layout.add_contact_layout, null);
                EditText name = view.findViewById(R.id.add_name);
                EditText phone = view.findViewById(R.id.add_phone);
                AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteContacts.this);
                builder.setView(view);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String n = name.getText().toString().trim();
                        String p = phone.getText().toString().trim();
                        if(n.isEmpty()){
                            Toast.makeText(FavoriteContacts.this, getResources().getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(p.isEmpty()){
                            Toast.makeText(FavoriteContacts.this, getResources().getString(R.string.enter_mobile_number), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(p.length() != 10){
                            Toast.makeText(FavoriteContacts.this, getResources().getString(R.string.enter_valid_mobile_number), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("name",n);
                        hashMap.put("phone",p);
                        contactRef.push().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(FavoriteContacts.this, "Contact Added Successfully", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(FavoriteContacts.this, "Contact not added ! Try Again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        });
    }

    private void initializeFields() {
        String uid = FirebaseAuth.getInstance().getUid();
        contactRecycler = findViewById(R.id.contact_recycler_view);
        contactRecycler.setLayoutManager(new LinearLayoutManager(FavoriteContacts.this));
        contactRecycler.setHasFixedSize(true);
        addContact = findViewById(R.id.add_contact);
        contactRef = FirebaseDatabase.getInstance().getReference().child(USERS).child(uid).child(FAV);
        options = new FirebaseRecyclerOptions.Builder<ContactModel>().setQuery(contactRef, ContactModel.class).build();
        adapter = new ContactsAdapter(options);
        contactRecycler.setAdapter(adapter);
        adapter.startListening();
    }
}