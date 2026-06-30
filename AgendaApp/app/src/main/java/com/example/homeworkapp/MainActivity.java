package com.example.homeworkapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;

    private LinearLayout upcomingPanel;
    private TextView toggleUpcomingBtn, expandUpcomingBtn, noUpcomingText;
    private RecyclerView upcomingRecyclerView;

    private LinearLayout priorityPanel;
    private TextView togglePriorityBtn, expandPriorityBtn, noPriorityText;
    private RecyclerView priorityRecyclerView;

    private AppCompatButton prevMonthBtn, nextMonthBtn;
    private TextView currentMonthText, expandCalendarBtn;

    private ImageButton cameraButton;
    private android.widget.Button addManualBtn;

    private YearMonth currentMonth;
    private List<Assignment> allAssignments;
    private DatabaseReference databaseReference;
    private CalendarHelper calendarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        setupFirebase();
        setupCalendar();
        setupListeners();
        scheduleCleanupWorker();
        loadAssignments();
    }

    private void initializeViews() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        upcomingPanel = findViewById(R.id.upcomingPanel);
        toggleUpcomingBtn = findViewById(R.id.toggleUpcomingBtn);
        expandUpcomingBtn = findViewById(R.id.expandUpcomingBtn);
        noUpcomingText = findViewById(R.id.noUpcomingText);
        upcomingRecyclerView = findViewById(R.id.upcomingRecyclerView);

        priorityPanel = findViewById(R.id.priorityPanel);
        togglePriorityBtn = findViewById(R.id.togglePriorityBtn);
        expandPriorityBtn = findViewById(R.id.expandPriorityBtn);
        noPriorityText = findViewById(R.id.noPriorityText);
        priorityRecyclerView = findViewById(R.id.priorityRecyclerView);

        prevMonthBtn = findViewById(R.id.prevMonthBtn);
        nextMonthBtn = findViewById(R.id.nextMonthBtn);
        currentMonthText = findViewById(R.id.currentMonthText);
        expandCalendarBtn = findViewById(R.id.expandCalendarBtn);

        cameraButton = findViewById(R.id.cameraButton);
        addManualBtn = findViewById(R.id.addManualBtn);

        allAssignments = new ArrayList<>();
        currentMonth = YearMonth.now();
        calendarHelper = new CalendarHelper();

        calendarRecyclerView.setLayoutManager(
                new androidx.recyclerview.widget.GridLayoutManager(this, 7));
        calendarAdapter = new CalendarAdapter(this, new ArrayList<>());
        calendarRecyclerView.setAdapter(calendarAdapter);

        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        priorityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("assignments");
    }

    private void setupCalendar() {
        updateCalendarDisplay();
    }

    private void setupListeners() {
        cameraButton.setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));
        addManualBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssignmentDetailsActivity.class);
            intent.putExtra("isManualEntry", true);
            startActivity(intent);
        });
        prevMonthBtn.setOnClickListener(v -> { currentMonth = currentMonth.minusMonths(1); updateCalendarDisplay(); });
        nextMonthBtn.setOnClickListener(v -> { currentMonth = currentMonth.plusMonths(1); updateCalendarDisplay(); });

        toggleUpcomingBtn.setOnClickListener(v -> {
            if (upcomingPanel.getVisibility() == View.VISIBLE) {
                upcomingPanel.setVisibility(View.GONE);
                toggleUpcomingBtn.setText("Show");
            } else {
                upcomingPanel.setVisibility(View.VISIBLE);
                toggleUpcomingBtn.setText("Hide");
            }
        });

        togglePriorityBtn.setOnClickListener(v -> {
            if (priorityPanel.getVisibility() == View.VISIBLE) {
                priorityPanel.setVisibility(View.GONE);
                togglePriorityBtn.setText("Show");
            } else {
                priorityPanel.setVisibility(View.VISIBLE);
                togglePriorityBtn.setText("Hide");
            }
        });

        expandUpcomingBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, FullScreenListActivity.class);
            i.putExtra("mode", "upcoming");
            startActivity(i);
        });
        expandPriorityBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, FullScreenListActivity.class);
            i.putExtra("mode", "priority");
            startActivity(i);
        });
        expandCalendarBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, FullScreenCalendarActivity.class);
            startActivity(i);
        });
    }

    private void scheduleCleanupWorker() {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                AssignmentCleanupWorker.class, 60, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "assignment_cleanup", ExistingPeriodicWorkPolicy.KEEP, req);
    }

    private void loadAssignments() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allAssignments.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Assignment a = s.getValue(Assignment.class);
                    if (a != null) {
                        a.setId(s.getKey());
                        if (a.isCompleted() && a.shouldAutoDelete()) {
                            databaseReference.child(s.getKey()).removeValue();
                        } else {
                            allAssignments.add(a);
                        }
                    }
                }
                updateCalendarDisplay();
                updateUpcomingPanel();
                updatePriorityPanel();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCalendarDisplay() {
        List<CalendarDay> days = calendarHelper.generateCalendarDays(currentMonth, allAssignments);
        calendarAdapter.updateData(days);
        currentMonthText.setText(calendarHelper.getMonthYearString(currentMonth));
    }

    private void updateUpcomingPanel() {
        List<Assignment> upcoming = filterUpcoming();
        if (upcoming.isEmpty()) {
            noUpcomingText.setVisibility(View.VISIBLE);
            upcomingRecyclerView.setAdapter(new PriorityAdapter(new ArrayList<>()));
        } else {
            noUpcomingText.setVisibility(View.GONE);
            upcomingRecyclerView.setAdapter(new PriorityAdapter(upcoming));
        }
    }

    private void updatePriorityPanel() {
        List<Assignment> priorityTasks = filterPriority();
        if (priorityTasks.isEmpty()) {
            noPriorityText.setVisibility(View.VISIBLE);
            priorityRecyclerView.setAdapter(new PriorityAdapter(new ArrayList<>()));
        } else {
            noPriorityText.setVisibility(View.GONE);
            priorityRecyclerView.setAdapter(new PriorityAdapter(priorityTasks));
        }
    }

    private List<Assignment> filterUpcoming() {
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);
        List<Assignment> list = new ArrayList<>();
        for (Assignment a : allAssignments) {
            if (a.isCompleted()) continue;
            LocalDate d = a.getDueDateAsLocalDate();
            if (d != null && !d.isBefore(today) && !d.isAfter(weekLater)) {
                list.add(a);
            }
        }
        Collections.sort(list, (a, b) -> {
            LocalDate da = a.getDueDateAsLocalDate();
            LocalDate db = b.getDueDateAsLocalDate();
            return da.compareTo(db);
        });
        return list;
    }

    private List<Assignment> filterPriority() {
        List<Assignment> list = new ArrayList<>();
        for (Assignment a : allAssignments) {
            if (a.isCompleted()) continue;
            if (a.hasPriority()) list.add(a);
        }
        Collections.sort(list, (a, b) -> {
            int cmp = Integer.compare(a.getPriorityOrder(), b.getPriorityOrder());
            if (cmp != 0) return cmp;
            LocalDate da = a.getDueDateAsLocalDate();
            LocalDate db = b.getDueDateAsLocalDate();
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return da.compareTo(db);
        });
        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignments();
    }
}
