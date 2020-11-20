package com.cbit.disha;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class ContactsAdapter extends FirebaseRecyclerAdapter<ContactModel, ContactsAdapter.ContactViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    private Context context;
    public ContactsAdapter(@NonNull FirebaseRecyclerOptions<ContactModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ContactViewHolder holder, int position, @NonNull ContactModel model) {
        holder.deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setMessage("Do you want to delete the contact ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getRef(position).removeValue();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(true).show();
            }
        });
        holder.name.setText(model.getName());
        holder.phone.setText(model.getPhone());
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.contact_layout, parent, false);
        return new ContactViewHolder(view);
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView name, phone;
        private ImageView deleteContact;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.display_name);
            phone = itemView.findViewById(R.id.display_phone);
            deleteContact = itemView.findViewById(R.id.delete_contact);
        }
    }
}
