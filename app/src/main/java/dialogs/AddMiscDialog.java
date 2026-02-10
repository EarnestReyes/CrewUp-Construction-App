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

import workers.works.invoice.MiscItem;

public class AddMiscDialog extends Dialog {
    
    private EditText etDescription, etAmount;
    private Button btnCancel, btnSave;
    private MiscItem existingMisc;
    private OnMiscAddedListener listener;
    
    public interface OnMiscAddedListener {
        void onMiscAdded(MiscItem misc);
    }
    
    public AddMiscDialog(@NonNull Context context, MiscItem misc, OnMiscAddedListener listener) {
        super(context);
        this.existingMisc = misc;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_misc);
        
        initializeViews();
        setupListeners();
        
        // If editing, populate fields
        if (existingMisc != null) {
            etDescription.setText(existingMisc.getDescription());
            etAmount.setText(String.valueOf(existingMisc.getAmount()));
        }
    }
    
    private void initializeViews() {
        etDescription = findViewById(R.id.et_description);
        etAmount = findViewById(R.id.et_amount);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveMisc();
            }
        });
    }
    
    private boolean validateInputs() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Please enter description", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(getContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void saveMisc() {
        String description = etDescription.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        
        MiscItem misc = new MiscItem(description, amount);
        
        if (listener != null) {
            listener.onMiscAdded(misc);
        }
        
        dismiss();
    }
}
