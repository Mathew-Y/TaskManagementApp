package com.example.taskmanagementapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceCommandProcessor {

    private static final int VOICE_REQUEST_CODE = 1001;

    private final Activity activity;
    private final VoiceCommandListener listener;

    public VoiceCommandProcessor(Activity activity, VoiceCommandListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    public int getRequestCode() {
        return VOICE_REQUEST_CODE;
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What would you like to do?");
        try {
            activity.startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "Voice input not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != VOICE_REQUEST_CODE || resultCode != Activity.RESULT_OK || data == null)
            return;

        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results == null || results.isEmpty()) {
            listener.onVoiceCommandError("Didn't catch that. Please try again.");
            return;
        }

        String command = results.get(0).toLowerCase();
        parseCommand(command);
    }

    private void parseCommand(String command) {
        command = command.toLowerCase();
        command = normalizePossessiveNumbers(command);

        // --- VOICE NAVIGATION ---
        if (command.contains("go to completed") || command.contains("navigate to completed") ||
                command.contains("navigate to my completed") || command.contains("go to my completed")) {
            if (listener instanceof ExtendedVoiceCommandListener) {
                listener.onNavigateToCompletedTasks();
            }
            return;
        }

        if (command.contains("go to current") || command.contains("navigate to current") ||
                command.contains("navigate to my current") || command.contains("go to my current")) {
            if (listener instanceof ExtendedVoiceCommandListener) {
                listener.onNavigateToCurrentTasks();
            }
            return;
        }

        if (command.matches(".*(go back|navigate back).*current.*")) {
            if (listener instanceof ExtendedVoiceCommandListener) {
                listener.onNavigateToCurrentTasks();
            }
            return;
        }
        if (command.matches(".*(go back|navigate back).*completed.*")) {
            if (listener instanceof ExtendedVoiceCommandListener) {
                listener.onNavigateToCompletedTasks();
            }
            return;
        }
        if (command.matches(".*(go|navigate).*tab.*") || command.contains("switch tab") || command.contains("switch back")) {
            if (listener instanceof ExtendedVoiceCommandListener) {
                ((ExtendedVoiceCommandListener) listener).onNavigateToOppositeTab();
            }
            return;
        }

        // CREATE
        if (command.contains("create") && command.contains("task")) {
            String title = extractTitle(command);
            String description = extractDescription(command);

            if (listener instanceof ExtendedVoiceCommandListener) {
                ((ExtendedVoiceCommandListener) listener).onCreateTaskWithDetails(title, description);
            } else {
                listener.onCreateNewTask();
            }
            return;
        }

        // DELETE completed task — must come BEFORE generic delete
        if (command.contains("delete completed task") || command.contains("remove completed task")) {
            int taskNum = extractNumber(command);
            if (taskNum > 0 && listener instanceof ExtendedVoiceCommandListener) {
                ((ExtendedVoiceCommandListener) listener).onDeleteCompletedTask(taskNum);
            } else {
                listener.onVoiceCommandError("Couldn't identify completed task number to delete.");
            }
            return;
        }

        // UNCOMPLETE completed task
        if ((command.contains("uncomplete") || command.contains("mark as not done") ||
                command.contains("mark task as incomplete") || command.contains("move back")) &&
                command.contains("task")) {

            int taskNum = extractNumber(command);
            Log.d("VoiceProcessor", "Uncompleting task #" + taskNum);

            if (taskNum > 0 && listener instanceof ExtendedVoiceCommandListener) {
                ((ExtendedVoiceCommandListener) listener).onUncompleteTask(taskNum);
            } else {
                listener.onVoiceCommandError("Couldn't identify task number to uncomplete.");
            }
            return;
        }

        // EDIT TITLE
        if (command.contains("edit task") && command.contains("title") && command.contains("to")) {
            int taskNum = extractNumber(command);
            String newTitle = extractTextAfter(command, "title to");
            if (taskNum > 0 && listener instanceof ExtendedVoiceCommandListener) {
                ((ExtendedVoiceCommandListener) listener).onEditTaskTitle(taskNum, newTitle);
            } else {
                listener.onVoiceCommandError("Couldn't identify task number to edit title.");
            }
            return;
        }

        // EDIT DESCRIPTION
        if (command.contains("edit task") && command.contains("description") &&
                (command.contains("include") || command.contains("also say") || command.contains("to"))) {

            Log.d("VoiceProcessor", "Editing description for command: " + command);
            int taskNum = extractNumber(command);
            boolean append = command.contains("include") || command.contains("also say");
            String newText = extractTextAfter(command, append ? "include" : "description to");

            if (taskNum > 0 && listener instanceof ExtendedVoiceCommandListener) {
                ((ExtendedVoiceCommandListener) listener).onEditTaskDescription(taskNum, newText, append);
            } else {
                listener.onVoiceCommandError("Couldn't identify task number to edit description.");
            }
            return;
        }

        // DELETE task
        if (command.contains("delete task") || command.contains("remove task")) {
            int taskNum = extractNumber(command);
            if (taskNum > 0) {
                listener.onDeleteTask(taskNum);
            } else {
                listener.onVoiceCommandError("Couldn't identify task number to delete.");
            }
            return;
        }

        // COMPLETE task
        if (command.contains("complete") || command.contains("mark task")) {
            int taskNum = extractNumber(command);
            if (taskNum > 0) {
                listener.onCompleteTask(taskNum);
            } else {
                listener.onVoiceCommandError("Couldn't identify task number to complete.");
            }
            return;
        }

        // MOVE task to current or completed
        if (command.contains("move task") || command.contains("send task")) {
            int taskNum = extractNumber(command);

            boolean moveToCurrent = command.contains("current") || command.contains("back");
            boolean moveToCompleted = command.contains("completed") || command.contains("done");

            if (taskNum > 0) {
                if (moveToCurrent && listener instanceof ExtendedVoiceCommandListener) {
                    ((ExtendedVoiceCommandListener) listener).onUncompleteTask(taskNum);
                    return;
                } else if (moveToCompleted) {
                    listener.onCompleteTask(taskNum);
                    return;
                }
            }

            listener.onVoiceCommandError("Couldn't identify destination for task.");
            return;
        }

        // EDIT task
        if (command.contains("edit task")) {
            int taskNum = extractNumber(command);
            if (taskNum > 0) {
                listener.onEditTask(taskNum);
            } else {
                listener.onVoiceCommandError("Couldn't identify task number to edit.");
            }
            return;
        }

        // UNKNOWN
        listener.onVoiceCommandError("Unrecognized command.");
    }

    private int extractNumber(String input) {
        input = normalizePossessiveNumbers(input.toLowerCase());

        // Fix common homophones from voice-to-text
        input = normalizeSpokenNumbers(input);

        // First: digit-based search
        Pattern digitPattern = Pattern.compile("\\d+");
        Matcher digitMatcher = digitPattern.matcher(input);
        if (digitMatcher.find()) {
            return Integer.parseInt(digitMatcher.group());
        }

        // Word-based search
        Pattern wordPattern = Pattern.compile("(?:task(?: number)?|completed task|uncomplete task) ([a-z\\s\\-]+)");
        Matcher wordMatcher = wordPattern.matcher(input);
        if (wordMatcher.find()) {
            String wordSegment = wordMatcher.group(1).trim();
            return WordToNumber.parse(wordSegment);
        }

        return -1;
    }

    private String normalizeSpokenNumbers(String input) {
        // Replace only when it follows "task"
        input = input.replaceAll("\\btask for\\b", "task four");
        input = input.replaceAll("\\btask to\\b", "task two");
        input = input.replaceAll("\\btask too\\b", "task two");
        input = input.replaceAll("\\btask won\\b", "task one");

        // Optional: Handle "task number for", etc.
        input = input.replaceAll("\\btask number for\\b", "task number four");
        input = input.replaceAll("\\btask number to\\b", "task number two");
        input = input.replaceAll("\\btask number too\\b", "task number two");
        input = input.replaceAll("\\btask number won\\b", "task number one");

        return input;
    }

    private String extractTitle(String input) {
        input = input.toLowerCase();

        // Handle: with the title ...
        Pattern pattern1 = Pattern.compile("(?:title (?:of )?|with the title )(.*?)(?: with| and| to|$)");
        Matcher matcher1 = pattern1.matcher(input);
        if (matcher1.find()) {
            return capitalizeWords(matcher1.group(1).trim());
        }

        // NEW: Handle "with the name ..."
        Pattern patternName = Pattern.compile("with the name (.*?)(?: with| and| to|$)");
        Matcher matcherName = patternName.matcher(input);
        if (matcherName.find()) {
            return capitalizeWords(matcherName.group(1).trim());
        }

        // Handle: called/named ...
        Pattern pattern2 = Pattern.compile("(called|named) (.*?)($| with| and| to)");
        Matcher matcher2 = pattern2.matcher(input);
        if (matcher2.find()) {
            return capitalizeWords(matcher2.group(2).trim());
        }

        // Fallback: create task ...
        Pattern pattern3 = Pattern.compile("create (?:a )?(?:new )?task(?: called)? (.*?)($| with| and| to)");
        Matcher matcher3 = pattern3.matcher(input);
        if (matcher3.find()) {
            return capitalizeWords(matcher3.group(1).trim());
        }

        return "Untitled Task";
    }



    private String extractDescription(String input) {
        input = input.toLowerCase();
        if (input.contains("no description")) return "";

        Pattern pattern = Pattern.compile("description (?:of )?(.*)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return capitalizeWords(matcher.group(1).trim());
        }
        return "";
    }

    private String extractTextAfter(String input, String keyword) {
        int index = input.indexOf(keyword);
        if (index == -1) return "";

        String result = input.substring(index + keyword.length()).trim();

        // Remove soft starter phrases like "say", "that", etc.
        if (result.startsWith("say ")) result = result.substring(4).trim();
        if (result.startsWith("that ")) result = result.substring(5).trim();
        if (result.startsWith("this ")) result = result.substring(5).trim();
        if (result.startsWith("is ")) result = result.substring(3).trim();

        return capitalizeWords(result);
    }

    private String replaceNumberWords(String str) {
        return str
                .replaceAll("\\bone\\b", "1")
                .replaceAll("\\btwo\\b", "2")
                .replaceAll("\\bthree\\b", "3")
                .replaceAll("\\bfour\\b", "4")
                .replaceAll("\\bfive\\b", "5")
                .replaceAll("\\bsix\\b", "6")
                .replaceAll("\\bseven\\b", "7")
                .replaceAll("\\beight\\b", "8")
                .replaceAll("\\bnine\\b", "9")
                .replaceAll("\\bten\\b", "10");
    }
    private String capitalizeWords(String str) {
        str = replaceNumberWords(str);

        String[] words = str.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1)).append(" ");
        }
        return builder.toString().trim();
    }

    private String normalizePossessiveNumbers(String input) {
        input = input.replaceAll("’s", "").replaceAll("'s", ""); // handle possessives

        String[] numberWords = {
                "one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "ten", "eleven", "twelve", "thirteen",
                "fourteen", "fifteen", "sixteen", "seventeen", "eighteen",
                "nineteen", "twenty", "thirty", "forty", "fifty",
                "sixty", "seventy", "eighty", "ninety", "hundred", "thousand"
        };

        for (String word : numberWords) {
            // only replace word + "s" when it matches exactly
            input = input.replaceAll("\\b" + word + "s\\b", word);
        }

        return input;
    }


}

