package com.example.studymate.AdminDashboard.AdminHome;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;

public class MyViewHolder extends RecyclerView.ViewHolder {

    private TextView recTitle, recDesc, personCount;
    private CardView recCard;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        recCard = itemView.findViewById(R.id.recCard);
        recDesc = itemView.findViewById(R.id.recRoomDesc);
        recTitle = itemView.findViewById(R.id.recRoomTitle);
        personCount = itemView.findViewById(R.id.recUserCount);
    }

    public TextView getRecTitle() {
        return recTitle;
    }

    public TextView getRecDesc() {
        return recDesc;
    }

    public TextView getPersonCount() {
        return personCount;
    }

    public CardView getRecCard() {
        return recCard;
    }
}
