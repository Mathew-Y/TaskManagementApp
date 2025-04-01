package com.example.taskmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity implements ExtendedVoiceCommandListener {

    private VoiceCommandProcessor voiceProcessor;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SwitchCompat modeToggle;
    private ImageButton micButton;
    private Animation micAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        modeToggle = findViewById(R.id.modeToggle);
        micButton = findViewById(R.id.micButton);
        micAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_mic);
        voiceProcessor = new VoiceCommandProcessor(this, this);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Current Tasks" : "Completed Tasks")
        ).attach();

        modeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // You can store isChecked in a shared variable or pass it to fragments
            if (isChecked) {
                // Enable voice mode
                micButton.setVisibility(ViewPager2.VISIBLE);
            } else {
                micButton.setVisibility(ViewPager2.GONE);
            }
        });

        micButton.setOnClickListener(v -> {
            micButton.startAnimation(micAnimation);
            voiceProcessor.startListening();
        });
    }

    @Override
    public void onCreateNewTask() {
        startActivity(new Intent(this, TaskDetailsActivity.class));
    }

    @Override
    public void onDeleteTask(int taskNumber) {
        CurrentTasksFragment frag = getCurrentTasksFragment();
        if (frag != null) frag.deleteTaskByNumber(taskNumber);
    }

    @Override
    public void onCompleteTask(int taskNumber) {
        CurrentTasksFragment frag = getCurrentTasksFragment();
        if (frag != null) frag.completeTaskByNumber(taskNumber);
    }

    @Override
    public void onEditTask(int taskNumber) {
        CurrentTasksFragment frag = getCurrentTasksFragment();
        if (frag != null) frag.editTaskByNumber(taskNumber);
    }

    @Override
    public void onNavigateToCompletedTasks() {
        viewPager.setCurrentItem(1); // Assuming tab index 1 is Completed Tasks
    }

    @Override
    public void onNavigateToOppositeTab() {
        int currentItem = viewPager.getCurrentItem();
        int newItem = currentItem == 0 ? 1 : 0;
        viewPager.setCurrentItem(newItem, true);
    }

    @Override
    public void onNavigateToCurrentTasks() {
        viewPager.setCurrentItem(0); // Assuming tab index 0 is Current Tasks
    }
    @Override
    public void onVoiceCommandError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateTaskWithDetails(String title, String description) {
        Task task = new Task(0, title, description, false);
        TaskDatabase db = new TaskDatabase(this);
        db.addTask(task);
        Toast.makeText(this, "Task created: " + title, Toast.LENGTH_SHORT).show();

        // Refresh current tasks
        CurrentTasksFragment frag = getCurrentTasksFragment();
        if (frag != null) frag.refreshTasks();
    }

    @Override
    public void onEditTaskTitle(int taskNumber, String newTitle) {
        CurrentTasksFragment frag = getCurrentTasksFragment();
        if (frag != null) {
            Task task = frag.getTaskByNumber(taskNumber);
            if (task != null) {
                task.setTitle(newTitle);
                new TaskDatabase(this).updateTask(task);
                frag.refreshTasks();
                Toast.makeText(this, "Updated title of task #" + taskNumber, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onEditTaskDescription(int taskNumber, String text, boolean append) {
        CurrentTasksFragment frag = getCurrentTasksFragment();
        if (frag != null) {
            frag.editTaskDescription(taskNumber, text, append);
        } else {
            Toast.makeText(this, "Couldn't access tasks right now.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteCompletedTask(int taskNumber) {
        CompletedTasksFragment frag = getCompletedTasksFragment();
        if (frag != null) frag.deleteTaskByNumber(taskNumber);
    }

    @Override
    public void onUncompleteTask(int taskNumber) {
        CompletedTasksFragment frag = getCompletedTasksFragment();
        if (frag != null) frag.uncompleteTaskByNumber(taskNumber);
    }

    private CurrentTasksFragment getCurrentTasksFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (fragment instanceof CurrentTasksFragment) {
            return (CurrentTasksFragment) fragment;
        }
        return null;
    }

    private CompletedTasksFragment getCompletedTasksFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f1"); // assuming position 1 = completed
        if (fragment instanceof CompletedTasksFragment) {
            return (CompletedTasksFragment) fragment;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (voiceProcessor != null) {
            voiceProcessor.handleResult(requestCode, resultCode, data);
            micButton.clearAnimation(); // Stop pulse after listening
        }
    }
}
