package com.example.taskmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class TaskDetailsActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText;
    private TaskDatabase dbHelper;
    private long taskId = -1;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        titleEditText = findViewById(R.id.editTextTitle);
        descriptionEditText = findViewById(R.id.editTextDescription);
        Button saveButton = findViewById(R.id.saveButton);

        dbHelper = new TaskDatabase(this);

        Intent intent = getIntent();
        if (intent.hasExtra("task_id")) {
            taskId = intent.getLongExtra("task_id", -1);
            currentTask = dbHelper.getTaskById(taskId);

            if (currentTask != null) {
                titleEditText.setText(currentTask.getTitle());
                descriptionEditText.setText(currentTask.getDescription());
            } else {
                // fallback if task not found
                currentTask = new Task(0, "", "", false);
            }
        } else {
            currentTask = new Task(0, "", "", false);
        }

        // Auto-save on typing
        TextWatcher watcher = new SimpleTextWatcher(() -> {
            currentTask.setTitle(titleEditText.getText().toString());
            currentTask.setDescription(descriptionEditText.getText().toString());

            if (currentTask.getId() == 0) {
                long id = dbHelper.addTask(currentTask);
                currentTask.setId(id);
            } else {
                dbHelper.updateTask(currentTask);
            }
        }) {};

        titleEditText.addTextChangedListener(watcher);
        descriptionEditText.addTextChangedListener(watcher);

        // Save button simply finishes the activity
        saveButton.setOnClickListener(v -> finish());
    }
}
