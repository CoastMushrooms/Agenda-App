package com.example.homeworkapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private List<CalendarDay> calendarDays;
    private Context context;
    private LocalDate today;

    public CalendarAdapter(Context context, List<CalendarDay> calendarDays) {
        this.context = context;
        this.calendarDays = calendarDays;
        this.today = LocalDate.now();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.bind(calendarDays.get(position));
    }

    @Override
    public int getItemCount() { return calendarDays.size(); }

    public void updateData(List<CalendarDay> newDays) {
        this.calendarDays = newDays;
        notifyDataSetChanged();
    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder {
        private TextView dayNumberText;
        private LinearLayout assignmentsContainer;
        private View dayCell;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dayNumberText = itemView.findViewById(R.id.dayNumberText);
            assignmentsContainer = itemView.findViewById(R.id.assignmentsContainer);
            dayCell = itemView.findViewById(R.id.dayCell);
        }

        public void bind(CalendarDay calendarDay) {
            if (calendarDay.isEmpty()) {
                // Blank padding cell
                dayNumberText.setText("");
                assignmentsContainer.removeAllViews();
                dayCell.setBackgroundColor(context.getResources().getColor(R.color.app_background));
                dayCell.setAlpha(1.0f);
                return;
            }

            dayNumberText.setText(String.valueOf(calendarDay.getDay()));
            dayCell.setAlpha(1.0f);

            if (calendarDay.getDate().isEqual(today)) {
                dayCell.setBackgroundColor(context.getResources().getColor(R.color.today_background));
            } else if (calendarDay.hasAssignments()) {
                dayCell.setBackgroundColor(context.getResources().getColor(R.color.assignment_day_background));
            } else {
                dayCell.setBackgroundColor(context.getResources().getColor(R.color.calendar_day_background));
            }

            assignmentsContainer.removeAllViews();

            int visibleCount = 0;
            for (Assignment assignment : calendarDay.getAssignments()) {
                if (assignment.isCompleted()) continue;
                if (visibleCount >= 2) break;

                TextView assignmentText = new TextView(context);
                assignmentText.setText("• " + assignment.getTitle());
                assignmentText.setTextSize(10);
                assignmentText.setMaxLines(1);
                assignmentText.setEllipsize(android.text.TextUtils.TruncateAt.END);
                assignmentText.setPadding(0, 2, 0, 2);

                if (assignment.isOverdue()) {
                    assignmentText.setTextColor(context.getResources().getColor(R.color.overdue_color));
                } else if (assignment.isDueSoon()) {
                    assignmentText.setTextColor(context.getResources().getColor(R.color.due_soon_color));
                } else {
                    assignmentText.setTextColor(context.getResources().getColor(R.color.normal_color));
                }

                final Assignment clicked = assignment;
                assignmentText.setOnClickListener(v -> showAssignmentDialog(clicked));
                assignmentsContainer.addView(assignmentText);
                visibleCount++;
            }

            int completedCount = 0;
            for (Assignment a : calendarDay.getAssignments()) {
                if (a.isCompleted()) completedCount++;
            }
            int totalNonCompleted = calendarDay.getAssignmentCount() - completedCount;
            if (totalNonCompleted > 2) {
                TextView moreText = new TextView(context);
                moreText.setText("+" + (totalNonCompleted - 2) + " more");
                moreText.setTextSize(9);
                moreText.setTextColor(context.getResources().getColor(R.color.due_soon_color));
                assignmentsContainer.addView(moreText);
            }
        }

        private void showAssignmentDialog(Assignment assignment) {
            if (context instanceof AppCompatActivity) {
                FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
                AssignmentViewDialogFragment dialog = AssignmentViewDialogFragment.newInstance(assignment);
                dialog.show(fm, "assignment_view");
            }
        }
    }
}
