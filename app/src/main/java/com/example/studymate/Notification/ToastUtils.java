package com.example.studymate.Notification;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studymate.R;

public class ToastUtils {

    public static void showCustomToast(Context context, String message) {
        // Inflate the custom layout
        View layout = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, null);

        // Customize views inside the layout
        ImageView imageView = layout.findViewById(R.id.toast_icon);
        imageView.setImageResource(R.drawable.baseline_info_24); // Set your custom icon if needed

        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(message);

        // Create and show the toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50); // Adjust position as needed
        toast.show();
    }
}
