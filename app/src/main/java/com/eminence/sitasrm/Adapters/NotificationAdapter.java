package com.eminence.sitasrm.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eminence.sitasrm.Activity.Notification;
import com.eminence.sitasrm.Activity.OrderDetails;
import com.eminence.sitasrm.Models.Images;
import com.eminence.sitasrm.Models.NotificationModel;
import com.eminence.sitasrm.Models.OrderModel;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.DatabaseHandler;
import com.eminence.sitasrm.Utils.YourPreference;

import java.util.ArrayList;
import java.util.List;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    private List<NotificationModel> data;
    private Context context;

    public NotificationAdapter(List<NotificationModel> data, Context context) {
         this.context = context;
         this.data = data;
      }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
         holder.title.setText(data.get(position).getTitle());
        holder.Discription.setText(data.get(position).getDescription());

        holder.notificationListner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data.get(position).getType().equalsIgnoreCase("Order Placed")){
                    Intent intent = new Intent(context, OrderDetails.class);
                    intent.putExtra("order_id", data.get(position).getN_id());
                    context.startActivity(intent);
                }
            }
        });
       }


    @Override
    public int getItemCount() {

        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
         TextView title,Discription;
         LinearLayout notificationListner;
         public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            Discription = view.findViewById(R.id.Discription);
             notificationListner = view.findViewById(R.id.notificationListner);

        }
    }


}


