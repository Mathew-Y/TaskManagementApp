package com.example.taskmanagementapp;

public interface ExtendedVoiceCommandListener extends VoiceCommandListener {
    void onNavigateToOppositeTab();
    void onCreateTaskWithDetails(String title, String description);
    void onEditTaskTitle(int taskNumber, String newTitle);
    void onEditTaskDescription(int taskNumber, String newDesc, boolean append);
    void onDeleteCompletedTask(int taskNumber);
    void onUncompleteTask(int taskNumber);
}

