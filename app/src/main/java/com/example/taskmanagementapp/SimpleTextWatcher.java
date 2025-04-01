package com.example.taskmanagementapp;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class SimpleTextWatcher implements TextWatcher {
    private Runnable onChange;

    public SimpleTextWatcher(Runnable onChange) {
        this.onChange = onChange;
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override public void afterTextChanged(Editable s) {
        onChange.run();
    }
}
