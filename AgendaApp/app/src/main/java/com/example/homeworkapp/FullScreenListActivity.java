package com.example.homeworkapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FullScreenListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView titleText, emptyText;
    private String mode;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_list);

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "upcoming";

        titleText = findViewById(R.id.fullscreenTitle);
        emptyText = findViewById(R.id.fullscreenEmptyText);
        recyclerView = findViewById(R.id.fullscreenRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fullscreenBackBtn).setOnClickListener(v -> finish());

        titleText.setText("upcoming".equals(mode) ? "Upcoming Tasks" : "Priority Tasks");

        databaseReference = FirebaseDatabase.getInstance().getReference("assignments");
        loadData();
    }

    private void loadData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Assignment> all = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Assignment a = s.getValue(Assignment.class);
                    if (a != null) { a.setId(s.getKey()); all.add(a); }
                }

                List<Assignment> filtered = "upcoming".equals(mode)
                        ? filterUpcoming(all) : filterPriority(all);

                if (filtered.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("upcoming".equals(mode)
                            ? "No tasks due in the next 7 days"
                            : "No priority tasks");
                } else {
                    emptyText.setVisibility(View.GONE);
                }
                recyclerView.setAdapter(new PriorityAdapter(filtered));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FullScreenListActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Assignment> filterUpcoming(List<Assignment> all) {
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);
        List<Assignment> list = new ArrayList<>();
        for (Assignment a : all) {
            if (a.isCompleted()) continue;
            LocalDate d = a.getDueDateAsLocalDate();
            if (d != null && !d.isBefore(today) && !d.isAfter(weekLater)) list.add(a);
        }
        Collections.sort(list, (a, b) -> a.getDueDateAsLocalDate().compareTo(b.getDueDateAsLocalDate()));
        return list;
    }

    private List<Assignment> filterPriority(List<Assignment> all) {
        List<Assignment> list = new ArrayList<>();
        for (Assignment a : all) {
            if (a.isCompleted()) continue;
            if (a.hasPriority()) list.add(a);
        }
        Collections.sort(list, (a, b) -> {
            int cmp = Integer.compare(a.getPriorityOrder(), b.getPriorityOrder());
            if (cmp != 0) return cmp;
            LocalDate da = a.getDueDateAsLocalDate(), db = b.getDueDateAsLocalDate();
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return da.compareTo(db);
        });
        return list;
    }
}
