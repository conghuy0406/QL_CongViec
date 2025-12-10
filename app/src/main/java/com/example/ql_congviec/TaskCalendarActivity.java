package com.example.ql_congviec;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_congviec.Adapter.TaskAdapter;
import com.example.ql_congviec.Database.DBHelperDatabase;
import com.example.ql_congviec.activity_task_detail;
import com.example.ql_congviec.model.UserTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskCalendarActivity extends AppCompatActivity {
    CalendarView calendarView;
    RecyclerView recyclerTasks;
    TextView textSelectedDate;
    ImageView btnAddTask;
    ImageView iv_backmain;

    DBHelperDatabase dbHelper;
    String selectedDate;  // yyyy-MM-dd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_calendar);

        calendarView = findViewById(R.id.calendarView);
        iv_backmain=findViewById(R.id.iv_backmain);
        recyclerTasks = findViewById(R.id.recyclerTasks);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        btnAddTask = findViewById(R.id.btnAddTask);

        dbHelper = new DBHelperDatabase(this);

        selectedDate = formatDate(calendarView.getDate());
        textSelectedDate.setText("Tasks for: " + selectedDate);
        loadTasksForDate(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Format đúng: yyyy-MM-dd
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            textSelectedDate.setText("Tasks for: " + selectedDate);
            loadTasksForDate(selectedDate);
        });
        iv_backmain.setOnClickListener(v -> finish());

        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, addCategory.class);
            startActivity(intent);
        });
    }

    private String formatDate(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    private void loadTasksForDate(String date) {
        int userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
        List<UserTask> tasks = dbHelper.getTasksByDate(date,userId);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setAdapter(new TaskAdapter(tasks));  // sử dụng RecyclerView adapter như bạn có
    }
}
