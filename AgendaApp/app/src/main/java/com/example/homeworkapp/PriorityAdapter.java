package com.example.homeworkapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PriorityAdapter extends RecyclerView.Adapter<PriorityAdapter.PriorityViewHolder> {
    private List<Assignment> assignments;
    private Context context;

    public PriorityAdapter(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    @NonNull
    @Override
    public PriorityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_priority, parent, false);
        return new PriorityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PriorityViewHolder holder, int position) {
        holder.bind(assignments.get(position));
    }

    @Override
    public int getItemCount() { return assignments.size(); }

    public class PriorityViewHolder extends RecyclerView.ViewHolder {
        private TextView priorityTaskTitle, priorityTaskDays, priorityTaskSubject, completionStatus, priorityLabel;
        private View urgencyIndicator;
        private ImageButton actionBtn;

        public PriorityViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityTaskTitle = itemView.findViewById(R.id.priorityTaskTitle);
            priorityTaskDays = itemView.findViewById(R.id.priorityTaskDays);
            priorityTaskSubject = itemView.findViewById(R.id.priorityTaskSubject);
            completionStatus = itemView.findViewById(R.id.completionStatus);
            urgencyIndicator = itemView.findViewById(R.id.urgencyIndicator);
            actionBtn = itemView.findViewById(R.id.deleteBtn);
            priorityLabel = itemView.findViewById(R.id.priorityLabel);
        }

        public void bind(Assignment assignment) {
            priorityTaskTitle.setText(assignment.getTitle());
            priorityTaskSubject.setText(assignment.getSubject());

            // Show priority label
            if (priorityLabel != null) {
                if (assignment.hasPriority()) {
                    priorityLabel.setVisibility(View.VISIBLE);
                    priorityLabel.setText(assignment.getPriority());
                    if (assignment.isHighPriority()) {
                        priorityLabel.setTextColor(context.getResources().getColor(R.color.overdue_color));
                    } else if (assignment.isMediumPriority()) {
                        priorityLabel.setTextColor(context.getResources().getColor(R.color.due_soon_color));
                    } else {
                        priorityLabel.setTextColor(context.getResources().getColor(R.color.normal_color));
                    }
                } else {
                    priorityLabel.setVisibility(View.GONE);
                }
            }

            // Tap row to open detail dialog
            itemView.setOnClickListener(v -> {
                if (context instanceof AppCompatActivity) {
                    FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
                    AssignmentViewDialogFragment.newInstance(assignment).show(fm, "assignment_view");
                }
            });

            if (assignment.isCompleted()) {
                completionStatus.setVisibility(View.VISIBLE);
                completionStatus.setText(assignment.getCompletionStatus());
                completionStatus.setTextColor(context.getResources().getColor(R.color.completed_color));
                priorityTaskTitle.setPaintFlags(priorityTaskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                priorityTaskTitle.setAlpha(0.6f);
                priorityTaskDays.setAlpha(0.6f);
                priorityTaskSubject.setAlpha(0.6f);
                actionBtn.setImageResource(android.R.drawable.ic_menu_delete);
                actionBtn.setOnClickListener(v -> deleteAssignment(assignment));
                urgencyIndicator.setBackgroundColor(context.getResources().getColor(R.color.completed_color));
                priorityTaskDays.setVisibility(View.GONE);
            } else {
                completionStatus.setVisibility(View.GONE);
                priorityTaskTitle.setPaintFlags(0);
                priorityTaskTitle.setAlpha(1.0f);
                priorityTaskDays.setAlpha(1.0f);
                priorityTaskSubject.setAlpha(1.0f);
                priorityTaskDays.setVisibility(View.VISIBLE);

                int daysUntil = assignment.getDaysUntilDue();
                if (daysUntil < 0) {
                    priorityTaskDays.setText("OVERDUE");
                    urgencyIndicator.setBackgroundColor(context.getResources().getColor(R.color.overdue_color));
                } else if (daysUntil == 0) {
                    priorityTaskDays.setText("Due TODAY");
                    urgencyIndicator.setBackgroundColor(context.getResources().getColor(R.color.due_soon_color));
                } else if (daysUntil == 1) {
                    priorityTaskDays.setText("Due TOMORROW");
                    urgencyIndicator.setBackgroundColor(context.getResources().getColor(R.color.due_soon_color));
                } else {
                    priorityTaskDays.setText("Due in " + daysUntil + " days");
                    urgencyIndicator.setBackgroundColor(context.getResources().getColor(R.color.normal_color));
                }

                actionBtn.setImageResource(android.R.drawable.ic_menu_view);
                actionBtn.setOnClickListener(v -> markAsDone(assignment));
            }
        }

        private void deleteAssignment(Assignment a) {
            FirebaseDatabase.getInstance().getReference("assignments").child(a.getId()).removeValue()
                    .addOnSuccessListener(v -> Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show());
        }

        private void markAsDone(Assignment a) {
            a.setCompleted(true);
            FirebaseDatabase.getInstance().getReference("assignments").child(a.getId()).setValue(a)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(context, "Marked as done!", Toast.LENGTH_SHORT).show();
                        notifyItemChanged(getAdapterPosition());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show());
        }
    }
}
