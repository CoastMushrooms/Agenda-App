package com.example.homeworkapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.UUID;

public class AssignmentDetailsActivity extends AppCompatActivity {
    private ImageView capturedImagePreview;
    private EditText assignmentTitleInput, assignmentDescriptionInput, assignmentSubjectInput;
    private TextView dueDateDisplay;
    private Button selectDateBtn, saveAssignmentBtn, cancelBtn;
    private LinearLayout imagePreviewContainer;
    private RadioGroup priorityRadioGroup;

    private String photoPath;
    private LocalDate selectedDueDate;
    private boolean isManualEntry;
    private DatabaseReference databaseReference;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_details);
        initializeViews();
        getIntentData();
        setupListeners();
        setupFirebase();
    }

    private void initializeViews() {
        capturedImagePreview = findViewById(R.id.capturedImagePreview);
        assignmentTitleInput = findViewById(R.id.assignmentTitleInput);
        assignmentDescriptionInput = findViewById(R.id.assignmentDescriptionInput);
        assignmentSubjectInput = findViewById(R.id.assignmentSubjectInput);
        dueDateDisplay = findViewById(R.id.dueDateDisplay);
        selectDateBtn = findViewById(R.id.selectDateBtn);
        saveAssignmentBtn = findViewById(R.id.saveAssignmentBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        priorityRadioGroup = findViewById(R.id.priorityRadioGroup);
        selectedDueDate = LocalDate.now();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        isManualEntry = intent.getBooleanExtra("isManualEntry", true);
        if (!isManualEntry) {
            photoPath = intent.getStringExtra("photoPath");
            if (photoPath != null) {
                capturedImagePreview.setImageBitmap(BitmapFactory.decodeFile(photoPath));
                imagePreviewContainer.setVisibility(android.view.View.VISIBLE);
            }
        } else {
            imagePreviewContainer.setVisibility(android.view.View.GONE);
        }
        updateDateDisplay();
    }

    private void setupListeners() {
        selectDateBtn.setOnClickListener(v -> showDatePicker());
        saveAssignmentBtn.setOnClickListener(v -> saveAssignment());
        cancelBtn.setOnClickListener(v -> finish());
    }

    private void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("assignments");
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.set(selectedDueDate.getYear(), selectedDueDate.getMonthValue() - 1, selectedDueDate.getDayOfMonth());
        new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDueDate = LocalDate.of(y, m + 1, d);
            updateDateDisplay();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateDisplay() {
        dueDateDisplay.setText("Due: " + selectedDueDate.format(dateFormatter));
    }

    private String getSelectedPriority() {
        int id = priorityRadioGroup.getCheckedRadioButtonId();
        if (id == R.id.priorityHigh) return "High";
        if (id == R.id.priorityMedium) return "Medium";
        if (id == R.id.priorityLow) return "Low";
        return "None";
    }

    private void saveAssignment() {
        String title = assignmentTitleInput.getText().toString().trim();
        String description = assignmentDescriptionInput.getText().toString().trim();
        String subject = assignmentSubjectInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter assignment title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) description = "No description provided";
        if (subject.isEmpty()) subject = "General";

        String assignmentId = UUID.randomUUID().toString();
        Assignment assignment = new Assignment(
                assignmentId, title, description, subject,
                selectedDueDate.toString(), System.currentTimeMillis());
        assignment.setPriority(getSelectedPriority());

        if (!isManualEntry && photoPath != null) {
            String b64 = encodeImageToBase64(photoPath);
            if (b64 != null) assignment.setImageBase64(b64);
            new File(photoPath).delete();
        }

        databaseReference.child(assignmentId).setValue(assignment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Assignment saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String encodeImageToBase64(String path) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            int maxDim = 800, scale = 1;
            if (opts.outWidth > maxDim || opts.outHeight > maxDim)
                scale = Math.max(opts.outWidth / maxDim, opts.outHeight / maxDim);
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
            if (bitmap == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] bytes = baos.toByteArray();
            bitmap.recycle();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) { return null; }
    }
}
