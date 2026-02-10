package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;

import java.text.DecimalFormat;
import java.util.List;

import workers.works.invoice.MaterialItem;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {
    
    private List<MaterialItem> materialList;
    private OnItemActionListener listener;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    
    public interface OnItemActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }
    
    public MaterialAdapter(List<MaterialItem> materialList, OnItemActionListener listener) {
        this.materialList = materialList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        MaterialItem item = materialList.get(position);
        
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvMaterialName.setText(item.getMaterialName());
        holder.tvRate.setText("₱" + currencyFormat.format(item.getRate()));
        holder.tvTotal.setText("₱" + currencyFormat.format(item.getTotal()));
        
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(holder.getAdapterPosition());
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return materialList.size();
    }
    
    static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuantity, tvMaterialName, tvRate, tvTotal;
        ImageButton btnEdit, btnDelete;
        
        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvMaterialName = itemView.findViewById(R.id.tv_material_name);
            tvRate = itemView.findViewById(R.id.tv_rate);
            tvTotal = itemView.findViewById(R.id.tv_total);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
