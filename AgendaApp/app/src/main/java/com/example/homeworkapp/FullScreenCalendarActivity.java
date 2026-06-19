package com.example.homeworkapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class FullScreenCalendarActivity extends AppCompatActivity {
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private TextView currentMonthText;
    private YearMonth currentMonth;
    private CalendarHelper calendarHelper;
    private List<Assignment> allAssignments = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_calendar);

        currentMonth = YearMonth.now();
        calendarHelper = new CalendarHelper();

        findViewById(R.id.fullscreenBackBtn).setOnClickListener(v -> finish());

        currentMonthText = findViewById(R.id.fullscreenMonthText);
        calendarRecyclerView = findViewById(R.id.fullscreenCalendarRecyclerView);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        calendarAdapter = new CalendarAdapter(this, new ArrayList<>());
        calendarRecyclerView.setAdapter(calendarAdapter);

        findViewById(R.id.fullscreenPrevBtn).setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1); updateDisplay();
        });
        findViewById(R.id.fullscreenNextBtn).setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1); updateDisplay();
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("assignments");
        loadData();
    }

    private void loadData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allAssignments.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Assignment a = s.getValue(Assignment.class);
                    if (a != null) { a.setId(s.getKey()); allAssignments.add(a); }
                }
                updateDisplay();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FullScreenCalendarActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDisplay() {
        calendarAdapter.updateData(calendarHelper.generateCalendarDays(currentMonth, allAssignments));
        currentMonthText.setText(calendarHelper.getMonthYearString(currentMonth));
    }
}
