package com.example.taskmanagementapp;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final OnTaskClickListener listener;
    private final boolean showTaskNumbers;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onMarkDoneClick(Task task);
        void onDeleteClick(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener, boolean showTaskNumbers) {
        this.taskList = tasks;
        this.listener = listener;
        this.showTaskNumbers = showTaskNumbers;
    }

    public void setTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Format title: #1 - Title
        String titleText = showTaskNumbers ? "#" + (position + 1) + " - " + task.getTitle() : task.getTitle();
        holder.title.setText(titleText);

        // Styling based on completion
        if (task.isCompleted()) {
            holder.title.setAlpha(0.5f);
            holder.title.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
            holder.markDoneIcon.setImageResource(R.drawable.ic_check_circle);
            holder.markDoneIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
            holder.markDoneIcon.setAlpha(1f);
        } else {
            holder.title.setAlpha(1f);
            holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.markDoneIcon.setImageResource(R.drawable.ic_check_circle);
            holder.markDoneIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
            holder.markDoneIcon.setAlpha(0.5f);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTaskClick(task);
        });

        holder.markDoneIcon.setOnClickListener(v -> {
            if (listener != null) listener.onMarkDoneClick(task);
        });

        holder.deleteIcon.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(task);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public Task getTaskAt(int position) {
        return taskList.get(position);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView markDoneIcon, deleteIcon;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            markDoneIcon = itemView.findViewById(R.id.markDoneIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }
}
