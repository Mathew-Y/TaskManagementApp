package com.example.taskmanagementapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CompletedTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TaskDatabase dbHelper;
    private List<Task> completedTasks = new ArrayList<>();

    public CompletedTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        dbHelper = new TaskDatabase(getContext());
        recyclerView = view.findViewById(R.id.recyclerView);

        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setVisibility(View.GONE);

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
                        .setTitle("Mark as Incomplete")
                        .setMessage("Do you want to move this task back to your current tasks?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            task.setCompleted(false);
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

        loadTasks();

        return view;
    }

    private void loadTasks() {
        List<Task> completedTasks = dbHelper.getCompletedTasks();
        adapter.setTasks(completedTasks);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    public Task getTaskByNumber(int number) {
        if (number > 0 && number <= completedTasks.size()) {
            return completedTasks.get(number - 1);
        }
        return null;
    }

    public void deleteTaskByNumber(int number) {
        if (number > 0 && number <= adapter.getItemCount()) {
            Task task = adapter.getTaskAt(number - 1);
            dbHelper.deleteTask(task.getId());
            loadTasks();
            Toast.makeText(getContext(), "Deleted completed task #" + number, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Invalid completed task number.", Toast.LENGTH_SHORT).show();
        }
    }

    public void uncompleteTaskByNumber(int number) {
        if (number > 0 && number <= adapter.getItemCount()) {
            Task task = adapter.getTaskAt(number - 1);
            task.setCompleted(false);
            dbHelper.updateTask(task);
            loadTasks();
            Toast.makeText(getContext(), "Moved completed task #" + number + " to current tasks", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Invalid completed task number.", Toast.LENGTH_SHORT).show();
        }
    }


    public void refreshTasks() {
        loadTasks();
    }

}
