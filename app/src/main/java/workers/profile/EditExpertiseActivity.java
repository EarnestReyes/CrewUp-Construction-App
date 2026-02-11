package workers.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ConstructionApp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.FieldValue;
/**
 * Activity to manage worker's fields of expertise
 * Allows selecting multiple predefined options and adding custom expertise
 */
public class EditExpertiseActivity extends AppCompatActivity {

    private static final String TAG = "EditExpertise";

    private ChipGroup chipGroupPredefined, chipGroupCustom;
    private TextInputEditText edtCustomExpertise;
    private Button btnAddCustom, btnSave;
    private TextView txtSelectedCount, txtNoCustom;

    private FirebaseFirestore db;
    private String userId;

    private List<String> selectedExpertise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expertise);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        selectedExpertise = new ArrayList<>();

        initializeViews();
        setupToolbar();
        loadCurrentExpertise();
        setupListeners();
    }

    private void initializeViews() {
        chipGroupPredefined = findViewById(R.id.chipGroupPredefined);
        chipGroupCustom = findViewById(R.id.chipGroupCustom);
        edtCustomExpertise = findViewById(R.id.edtCustomExpertise);
        btnAddCustom = findViewById(R.id.btnAddCustom);
        btnSave = findViewById(R.id.btnSave);
        txtSelectedCount = findViewById(R.id.txtSelectedCount);
        txtNoCustom = findViewById(R.id.txtNoCustom);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        // Predefined chips listener
        for (int i = 0; i < chipGroupPredefined.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupPredefined.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateSelectedCount());
        }

        // Add custom expertise
        btnAddCustom.setOnClickListener(v -> addCustomExpertise());

        // Save button
        btnSave.setOnClickListener(v -> saveExpertise());
    }

    private void loadCurrentExpertise() {
        Log.d(TAG, "Loading current expertise for user: " + userId);

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object expertiseObj = documentSnapshot.get("expertise");

                        if (expertiseObj instanceof List) {
                            List<String> expertiseList = (List<String>) expertiseObj;
                            Log.d(TAG, "Found " + expertiseList.size() + " expertise items");

                            for (String expertise : expertiseList) {
                                // Check if it's a predefined option
                                boolean foundPredefined = false;
                                for (int i = 0; i < chipGroupPredefined.getChildCount(); i++) {
                                    Chip chip = (Chip) chipGroupPredefined.getChildAt(i);
                                    if (chip.getText().toString().equals(expertise)) {
                                        chip.setChecked(true);
                                        foundPredefined = true;
                                        break;
                                    }
                                }

                                // If not predefined, add as custom
                                if (!foundPredefined) {
                                    addCustomChip(expertise);
                                }
                            }

                            updateSelectedCount();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading expertise", e);
                    Toast.makeText(this, "Error loading expertise", Toast.LENGTH_SHORT).show();
                });
    }

    private void addCustomExpertise() {
        String customText = edtCustomExpertise.getText().toString().trim();

        if (customText.isEmpty()) {
            edtCustomExpertise.setError("Enter expertise name");
            return;
        }

        // Check if already exists
        for (int i = 0; i < chipGroupCustom.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCustom.getChildAt(i);
            if (chip.getText().toString().equals(customText)) {
                Toast.makeText(this, "This expertise already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Add custom chip
        addCustomChip(customText);

        // Clear input
        edtCustomExpertise.setText("");

        // Update count
        updateSelectedCount();
    }

    private void addCustomChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(false);
        chip.setCloseIconVisible(true);

        // Remove on close icon click
        chip.setOnCloseIconClickListener(v -> {
            chipGroupCustom.removeView(chip);
            updateSelectedCount();
            updateCustomEmptyState();
        });

        chipGroupCustom.addView(chip);
        updateCustomEmptyState();
    }

    private void updateSelectedCount() {
        int count = getSelectedExpertiseCount();
        txtSelectedCount.setText(count + " expertise selected");

        // Enable/disable save button
        btnSave.setEnabled(count > 0);
    }

    private int getSelectedExpertiseCount() {
        int count = 0;

        // Count checked predefined chips
        for (int i = 0; i < chipGroupPredefined.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupPredefined.getChildAt(i);
            if (chip.isChecked()) {
                count++;
            }
        }

        // Count custom chips
        count += chipGroupCustom.getChildCount();

        return count;
    }

    private void updateCustomEmptyState() {
        if (chipGroupCustom.getChildCount() == 0) {
            txtNoCustom.setVisibility(View.VISIBLE);
        } else {
            txtNoCustom.setVisibility(View.GONE);
        }
    }



    private void saveExpertise() {
        selectedExpertise.clear();

        // Get checked predefined expertise
        for (int i = 0; i < chipGroupPredefined.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupPredefined.getChildAt(i);
            if (chip.isChecked()) {
                selectedExpertise.add(chip.getText().toString());
            }
        }

        // Get custom expertise
        for (int i = 0; i < chipGroupCustom.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCustom.getChildAt(i);
            selectedExpertise.add(chip.getText().toString());
        }

        if (selectedExpertise.isEmpty()) {
            Toast.makeText(this, "Please select at least one expertise", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(userId)
                .update("Expertise", FieldValue.arrayUnion(
                        selectedExpertise.toArray(new String[0])
                ))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Expertise added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}