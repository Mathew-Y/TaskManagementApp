package com.example.taskmanagementapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TaskDatabase dbHelper;

    public CurrentTasksFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        dbHelper = new TaskDatabase(getContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
                intent.putExtra("task_id", task.getId());
                intent.putExtra("task_title", task.getTitle());
                intent.putExtra("task_description", task.getDescription());
                startActivity(intent);
            }

            @Override
            public void onMarkDoneClick(Task task) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Complete Task")
                        .setMessage("Mark this task as completed?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            task.setCompleted(true);
                            dbHelper.updateTask(task);
                            loadTasks();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onDeleteClick(Task task) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            dbHelper.deleteTask(task.getId());
                            loadTasks();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }, true);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
            startActivity(intent);
        });

        loadTasks();
        return view;
    }

    private void loadTasks() {
        List<Task> currentTasks = dbHelper.getCurrentTasks();
        Collections.reverse(currentTasks);
        adapter.setTasks(currentTasks);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    public void deleteTaskByNumber(int number) {
        if (number > 0 && number <= adapter.getItemCount()) {
            Task task = adapter.getTaskAt(number - 1);
            dbHelper.deleteTask(task.getId());
            loadTasks();
            Toast.makeText(getContext(), "Deleted task #" + number, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Invalid task number.", Toast.LENGTH_SHORT).show();
        }
    }

    public void completeTaskByNumber(int number) {
        if (number > 0 && number <= adapter.getItemCount()) {
            Task task = adapter.getTaskAt(number - 1);
            task.setCompleted(true);
            dbHelper.updateTask(task);
            loadTasks();
            Toast.makeText(getContext(), "Marked task #" + number + " as completed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Invalid task number.", Toast.LENGTH_SHORT).show();
        }
    }

    public void editTaskByNumber(int number) {
        if (number > 0 && number <= adapter.getItemCount()) {
            Task task = adapter.getTaskAt(number - 1);
            Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
            intent.putExtra("task_id", task.getId());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Invalid task number.", Toast.LENGTH_SHORT).show();
        }
    }

    public Task getTaskByNumber(int number) {
        if (number > 0 && number <= adapter.getItemCount()) {
            return adapter.getTaskAt(number - 1);
        }
        return null;
    }

    public void editTaskDescription(int number, String newText, boolean append) {
        if (number > 0 && number <= adapter.getItemCount()) {
            Task task = adapter.getTaskAt(number - 1);

            String current = task.getDescription();
            String updated;

            if (append) {
                updated = current.isEmpty() ? newText : current + "\n" + newText;
            } else {
                updated = newText;
            }

            // Log what’s happening
            Log.d("VoiceEdit", "Task #" + number + " BEFORE: " + current);
            Log.d("VoiceEdit", "Task #" + number + " AFTER: " + updated);

            task.setDescription(updated);

            dbHelper.updateTask(task);
            refreshTasks();

            Toast.makeText(getContext(), "Updated task #" + number, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Invalid task number.", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshTasks() {
        loadTasks();
    }

}
