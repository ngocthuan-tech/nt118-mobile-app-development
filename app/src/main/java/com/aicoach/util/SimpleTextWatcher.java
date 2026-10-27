package com.aicoach.util;

import android.text.Editable;
import android.text.TextWatcher;

/** TextWatcher chỉ cần viết afterTextChanged — dùng để xoá lỗi khi người dùng gõ lại. */
public abstract class SimpleTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public static SimpleTextWatcher after(Runnable action) {
        return new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                action.run();
            }
        };
    }
}
