package com.cbit.disha;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;


public class EmergencyAdapter extends FirebaseRecyclerAdapter<EmergencyModel, EmergencyAdapter.EmergencyViewHolder> {

    private Context context;

    public EmergencyAdapter(@NonNull FirebaseRecyclerOptions<EmergencyModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position, @NonNull EmergencyModel model) {
        holder.phone.setText(model.getPhone());
        holder.district.setText(model.getName());
        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + model.getPhone()));
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emergency_single_layout, parent, false);
        context = parent.getContext();
        return new EmergencyViewHolder(view);
    }

    public class EmergencyViewHolder extends RecyclerView.ViewHolder {
        private TextView district, phone;
        private ImageView call;
        public EmergencyViewHolder(@NonNull View itemView) {
            super(itemView);
            district = itemView.findViewById(R.id.district_name);
            phone = itemView.findViewById(R.id.phone_number);
            call = itemView.findViewById(R.id.call_button);
        }
    }
}
