package com.example.taskmanagementapp;

public interface VoiceCommandListener {
    void onCreateNewTask();
    void onDeleteTask(int taskNumber);
    void onCompleteTask(int taskNumber);
    void onEditTask(int taskNumber);
    void onVoiceCommandError(String message);
    void onNavigateToCompletedTasks();
    void onNavigateToCurrentTasks();
}
