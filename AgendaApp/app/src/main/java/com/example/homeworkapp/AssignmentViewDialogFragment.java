package com.example.homeworkapp;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.FirebaseDatabase;

import java.time.format.DateTimeFormatter;

public class AssignmentViewDialogFragment extends DialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_DUE_DATE = "dueDate";
    private static final String ARG_IMAGE_BASE64 = "imageBase64";
    private static final String ARG_COMPLETED = "completed";
    private static final String ARG_COMPLETION_STATUS = "completionStatus";
    private static final String ARG_PRIORITY = "priority";

    public static AssignmentViewDialogFragment newInstance(Assignment assignment) {
        AssignmentViewDialogFragment f = new AssignmentViewDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, assignment.getId());
        args.putString(ARG_TITLE, assignment.getTitle());
        args.putString(ARG_SUBJECT, assignment.getSubject());
        args.putString(ARG_DESCRIPTION, assignment.getDescription());
        args.putString(ARG_DUE_DATE, assignment.getDueDate());
        args.putString(ARG_IMAGE_BASE64, assignment.getImageBase64());
        args.putBoolean(ARG_COMPLETED, assignment.isCompleted());
        args.putString(ARG_COMPLETION_STATUS, assignment.getCompletionStatus());
        args.putString(ARG_PRIORITY, assignment.getPriority());
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_assignment_view, container, false);
        Bundle args = getArguments();
        if (args == null) { dismiss(); return view; }

        String assignmentId = args.getString(ARG_ID, "");
        boolean completed = args.getBoolean(ARG_COMPLETED, false);

        view.findViewById(R.id.dialogCloseBtn).setOnClickListener(v -> dismiss());

        ((TextView) view.findViewById(R.id.dialogTitle)).setText(args.getString(ARG_TITLE, ""));
        ((TextView) view.findViewById(R.id.dialogSubject)).setText(args.getString(ARG_SUBJECT, ""));
        ((TextView) view.findViewById(R.id.dialogDescription)).setText(args.getString(ARG_DESCRIPTION, ""));

        // Priority
        TextView priorityText = view.findViewById(R.id.dialogPriority);
        String priority = args.getString(ARG_PRIORITY, "None");
        if (priority != null && !priority.isEmpty() && !"None".equals(priority)) {
            priorityText.setVisibility(View.VISIBLE);
            priorityText.setText("Priority: " + priority);
            if ("High".equals(priority)) {
                priorityText.setTextColor(getResources().getColor(R.color.overdue_color));
            } else if ("Medium".equals(priority)) {
                priorityText.setTextColor(getResources().getColor(R.color.due_soon_color));
            } else {
                priorityText.setTextColor(getResources().getColor(R.color.normal_color));
            }
        } else {
            priorityText.setVisibility(View.GONE);
        }

        // Due date
        String rawDate = args.getString(ARG_DUE_DATE, "");
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(rawDate);
            ((TextView) view.findViewById(R.id.dialogDueDate))
                    .setText("Due: " + date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        } catch (Exception e) {
            ((TextView) view.findViewById(R.id.dialogDueDate)).setText("Due: " + rawDate);
        }

        // Completion status
        TextView statusText = view.findViewById(R.id.dialogStatus);
        if (completed) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(args.getString(ARG_COMPLETION_STATUS, "Completed"));
        } else {
            statusText.setVisibility(View.GONE);
        }

        // Image
        ImageView imageView = view.findViewById(R.id.dialogImage);
        LinearLayout imageContainer = view.findViewById(R.id.dialogImageContainer);
        String base64 = args.getString(ARG_IMAGE_BASE64, null);
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) {
                    imageView.setImageBitmap(bmp);
                    imageContainer.setVisibility(View.VISIBLE);
                } else { imageContainer.setVisibility(View.GONE); }
            } catch (Exception e) { imageContainer.setVisibility(View.GONE); }
        } else { imageContainer.setVisibility(View.GONE); }

        // Action buttons
        TextView markDoneBtn = view.findViewById(R.id.dialogMarkDoneBtn);
        TextView deleteBtn = view.findViewById(R.id.dialogDeleteBtn);

        if (completed) {
            markDoneBtn.setVisibility(View.GONE);
        } else {
            markDoneBtn.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference("assignments")
                        .child(assignmentId).child("completed").setValue(true);
                FirebaseDatabase.getInstance().getReference("assignments")
                        .child(assignmentId).child("completedAt").setValue(System.currentTimeMillis());
                Toast.makeText(getContext(), "Marked as done!", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }

        deleteBtn.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("assignments")
                    .child(assignmentId).removeValue();
            Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return d;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
