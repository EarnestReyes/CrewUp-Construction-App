package adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import models.NotificationModel;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final ArrayList<NotificationModel> list;

    public NotificationAdapter(Context context, ArrayList<NotificationModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notifications, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NotificationViewHolder holder, int position) {
        holder.body.setBackgroundColor(Color.TRANSPARENT);
        NotificationModel model = list.get(position);

        // Message text (SAFE)
        String title = model.getTitle() == null ? "" : model.getTitle();
        String message = model.getMessage() == null ? "" : model.getMessage();
        holder.txtMessage.setText(title + " " + message);

        // Timestamp (SAFE)
        if (model.getTimestamp() != null) {
            String time = new SimpleDateFormat(
                    "MMM dd • hh:mm a",
                    Locale.getDefault()
            ).format(model.getTimestamp().toDate());
            holder.txtTime.setText(time);
        } else {
            holder.txtTime.setText("");
        }

        String type = model.getType() == null ? "" : model.getType();

        switch (type) {
            case "like":
                holder.icon.setImageResource(R.drawable.ic_filled);
                break;
            case "message":
                holder.icon.setImageResource(R.drawable.ic_send);
                break;
            case "hire":
                holder.icon.setImageResource(R.drawable.ic_event);
                break;
            case "warning":
                holder.icon.setImageResource(R.drawable.ic_location);
                break;
            default:
                holder.icon.setImageResource(R.drawable.ic_notification);
        }

        holder.itemView.setAlpha(model.isRead() ? 0.5f : 1f);
        holder.itemView.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("notifications")
                    .document(model.getId())
                    .update("read", true);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView txtMessage, txtTime;
        LinearLayout body;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.imgIcon);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            body = itemView.findViewById(R.id.body);
        }
    }
}
