package com.example.homeworkapp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AssignmentCleanupWorker extends Worker {
    private static final String TAG = "AssignmentCleanup";

    public AssignmentCleanupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("assignments");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                        Assignment assignment = assignmentSnapshot.getValue(Assignment.class);

                        if (assignment != null && assignment.isCompleted()) {
                            if (assignment.shouldAutoDelete()) {
                                // Delete the assignment
                                databaseReference.child(assignmentSnapshot.getKey()).removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Auto-deleted assignment: " + assignment.getTitle());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to auto-delete: " + e.getMessage());
                                        });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error in cleanup worker: " + e.getMessage());
            return Result.retry();
        }
    }
}
