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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import workers.works.invoice.LaborItem;

public class AddLaborDialog extends BottomSheetDialog {
    
    private EditText etLaborType, etHours, etRate;
    private Button btnCancel, btnSave;
    private LaborItem existingLabor;
    private OnLaborAddedListener listener;
    
    public interface OnLaborAddedListener {
        void onLaborAdded(LaborItem labor);
    }
    
    public AddLaborDialog(@NonNull Context context, LaborItem labor, OnLaborAddedListener listener) {
        super(context);
        this.existingLabor = labor;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_labor);
        
        initializeViews();
        setupListeners();
        
        // If editing, populate fields
        if (existingLabor != null) {
            etLaborType.setText(existingLabor.getLaborType());
            etHours.setText(String.valueOf(existingLabor.getHours()));
            etRate.setText(String.valueOf(existingLabor.getRate()));
        }

    }
    
    private void initializeViews() {
        etLaborType = findViewById(R.id.et_labor_type);
        etHours = findViewById(R.id.et_hours);
        etRate = findViewById(R.id.et_rate);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveLabor();
            }
        });
    }
    
    private boolean validateInputs() {
        String laborType = etLaborType.getText().toString().trim();
        String hoursStr = etHours.getText().toString().trim();
        String rateStr = etRate.getText().toString().trim();
        
        if (laborType.isEmpty()) {
            Toast.makeText(getContext(), "Please enter labor type", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (hoursStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter hours", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (rateStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter rate", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            int hours = Integer.parseInt(hoursStr);
            if (hours <= 0) {
                Toast.makeText(getContext(), "Hours must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid hours", Toast.LENGTH_SHORT).show();
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
    
    private void saveLabor() {
        String laborType = etLaborType.getText().toString().trim();
        int hours = Integer.parseInt(etHours.getText().toString().trim());
        double rate = Double.parseDouble(etRate.getText().toString().trim());
        
        LaborItem labor = new LaborItem(laborType, hours, rate);
        
        if (listener != null) {
            listener.onLaborAdded(labor);
        }
        
        dismiss();
    }
}
