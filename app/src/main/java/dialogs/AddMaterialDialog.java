package dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.ConstructionApp.R;

import workers.works.invoice.MaterialItem;

public class AddMaterialDialog extends Dialog {
    
    private EditText etQuantity, etMaterialName, etRate;
    private Button btnCancel, btnSave;
    private MaterialItem existingMaterial;
    private OnMaterialAddedListener listener;
    
    public interface OnMaterialAddedListener {
        void onMaterialAdded(MaterialItem material);
    }
    
    public AddMaterialDialog(@NonNull Context context, MaterialItem material, OnMaterialAddedListener listener) {
        super(context);
        this.existingMaterial = material;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_material);
        
        initializeViews();
        setupListeners();
        
        // If editing, populate fields
        if (existingMaterial != null) {
            etQuantity.setText(String.valueOf(existingMaterial.getQuantity()));
            etMaterialName.setText(existingMaterial.getMaterialName());
            etRate.setText(String.valueOf(existingMaterial.getRate()));
        }
    }
    
    private void initializeViews() {
        etQuantity = findViewById(R.id.et_quantity);
        etMaterialName = findViewById(R.id.et_material_name);
        etRate = findViewById(R.id.et_rate);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveMaterial();
            }
        });
    }
    
    private boolean validateInputs() {
        String quantityStr = etQuantity.getText().toString().trim();
        String materialName = etMaterialName.getText().toString().trim();
        String rateStr = etRate.getText().toString().trim();
        
        if (quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter quantity", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (materialName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter material name", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (rateStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter rate", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(getContext(), "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            double rate = Double.parseDouble(rateStr);
            if (rate <= 0) {
                Toast.makeText(getContext(), "Rate must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid rate", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void saveMaterial() {
        int quantity = Integer.parseInt(etQuantity.getText().toString().trim());
        String materialName = etMaterialName.getText().toString().trim();
        double rate = Double.parseDouble(etRate.getText().toString().trim());
        
        MaterialItem material = new MaterialItem(quantity, materialName, rate);
        
        if (listener != null) {
            listener.onMaterialAdded(material);
        }
        
        dismiss();
    }
}
