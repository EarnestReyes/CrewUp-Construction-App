package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;
import com.google.firebase.Timestamp;

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

        NotificationModel model = list.get(position);

        // Message text
        holder.txtMessage.setText(
                model.getTitle() + " " + model.getMessage()
        );

        // Timestamp (SAFE)
        Timestamp ts = model.getTimestamp();
        if (ts != null) {
            String time = new SimpleDateFormat(
                    "MMM dd • hh:mm a",
                    Locale.getDefault()
            ).format(ts.toDate());
            holder.txtTime.setText(time);
        } else {
            holder.txtTime.setText("");
        }

        // Icon (SAFE switch)
        String type = model.getType();
        if (type == null) type = "";

        switch (type) {
            case "like":
                holder.icon.setImageResource(R.drawable.ic_filled);
                break;

            case "message":
                holder.icon.setImageResource(R.drawable.baseline_post_add_24);
                break;

            case "hire":
                holder.icon.setImageResource(R.drawable.ic_hire);
                break;

            default:
                holder.icon.setImageResource(R.drawable.ic_notification);
        }

        // Optional: dim read notifications
        holder.itemView.setAlpha(model.isRead() ? 0.5f : 1f);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView txtMessage, txtTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.imgIcon);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}
