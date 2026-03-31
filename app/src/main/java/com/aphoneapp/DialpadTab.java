package com.aphoneapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

class DialpadTab implements TabContent {

    private static final int KEYCODE_UP      = 19;
    private static final int KEYCODE_DOWN    = 20;
    private static final int KEYCODE_LEFT    = 21;
    private static final int KEYCODE_RIGHT   = 22;
    private static final int KEYCODE_BUTTON1 = 66;

    // Grid: rows 0-3 have 3 columns, row 4 has 2 columns (DEL, CALL)
    private static final String[][] DIGITS = {
        {"1", "2", "3"},
        {"4", "5", "6"},
        {"7", "8", "9"},
        {"*", "0", "#"},
    };
    private static final String LABEL_DEL  = "\u232B"; // ⌫
    private static final String LABEL_CALL = "Call";

    private final Context context;

    private View rootView;
    private TextView display;
    private TextView[][] digitButtons; // [0..3][0..2]
    private TextView btnDel;
    private TextView btnCall;

    private final StringBuilder dialedNumber = new StringBuilder();
    private int selRow = 0;
    private int selCol = 0;
    private boolean active = false; // whether dialpad has DMD focus

    DialpadTab(Context context) {
        this.context = context;
    }

    @Override
    public View getView() {
        if (rootView != null) return rootView;

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(16), dp(16), dp(16), dp(16));

        // Display
        display = new TextView(context);
        display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        display.setTextColor(context.getColor(R.color.text_primary));
        display.setGravity(Gravity.CENTER);
        display.setMinHeight(dp(60));
        display.setHint("Enter number");
        display.setHintTextColor(context.getColor(R.color.text_secondary));
        LinearLayout.LayoutParams displayParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        displayParams.bottomMargin = dp(16);
        container.addView(display, displayParams);

        // Digit rows
        digitButtons = new TextView[4][3];
        for (int r = 0; r < 4; r++) {
            LinearLayout row = makeRow();
            for (int c = 0; c < 3; c++) {
                final String label = DIGITS[r][c];
                final int fr = r, fc = c;
                TextView btn = makeButton(label);
                btn.setOnClickListener(v -> activateDigit(label));
                digitButtons[r][c] = btn;
                row.addView(btn, buttonParams());
            }
            container.addView(row, rowParams());
        }

        // Bottom row: DEL + CALL
        LinearLayout bottomRow = makeRow();

        btnDel = makeButton(LABEL_DEL);
        btnDel.setOnClickListener(v -> activateDel());
        bottomRow.addView(btnDel, buttonParams());

        btnCall = makeButton(LABEL_CALL);
        btnCall.setOnClickListener(v -> activateCall());
        bottomRow.addView(btnCall, buttonParams());

        container.addView(bottomRow, rowParams());

        rootView = container;
        return rootView;
    }

    @Override
    public void onKeyDown(int keyCode) {
        switch (keyCode) {
            case KEYCODE_UP:    move(-1, 0); break;
            case KEYCODE_DOWN:  move(1, 0);  break;
            case KEYCODE_LEFT:  move(0, -1); break;
            case KEYCODE_RIGHT: move(0, 1);  break;
            case KEYCODE_BUTTON1: activateSelected(); break;
        }
    }

    @Override
    public void onActivated() {
        active = true;
        selRow = 0;
        selCol = 0;
        updateSelection();
    }

    @Override
    public void onDeactivated() {
        active = false;
        updateSelection(); // clear highlights
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private void move(int dRow, int dCol) {
        int newRow = selRow + dRow;
        int newCol = selCol + dCol;

        // Clamp row
        newRow = Math.max(0, Math.min(newRow, 4));

        // Clamp col for target row
        int maxCol = (newRow == 4) ? 1 : 2;
        newCol = Math.max(0, Math.min(newCol, maxCol));

        selRow = newRow;
        selCol = newCol;
        updateSelection();
    }

    private void updateSelection() {
        // Reset all button backgrounds
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 3; c++) {
                setButtonSelected(digitButtons[r][c], false);
            }
        }
        setButtonSelected(btnDel, false);
        setButtonSelected(btnCall, false);

        if (!active) return;

        // Highlight selected
        if (selRow == 4) {
            setButtonSelected(selCol == 0 ? btnDel : btnCall, true);
        } else {
            setButtonSelected(digitButtons[selRow][selCol], true);
        }
    }

    private void setButtonSelected(TextView btn, boolean selected) {
        if (btn == null) return;
        btn.setBackgroundColor(context.getColor(
                selected ? R.color.button_active_body : R.color.surface_dark));
        btn.setTextColor(context.getColor(R.color.text_primary));
    }

    private void activateSelected() {
        if (selRow == 4) {
            if (selCol == 0) activateDel();
            else activateCall();
        } else {
            activateDigit(DIGITS[selRow][selCol]);
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void activateDigit(String digit) {
        dialedNumber.append(digit);
        display.setText(dialedNumber.toString());
    }

    private void activateDel() {
        if (dialedNumber.length() > 0) {
            dialedNumber.deleteCharAt(dialedNumber.length() - 1);
            display.setText(dialedNumber.toString());
        }
    }

    private void activateCall() {
        String number = dialedNumber.toString().trim();
        if (number.isEmpty()) return;
        Uri uri = Uri.fromParts("tel", number, null);
        TelecomManager tm = context.getSystemService(TelecomManager.class);
        tm.placeCall(uri, new Bundle());
    }

    // ── View helpers ─────────────────────────────────────────────────────────

    private LinearLayout makeRow() {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        return row;
    }

    private TextView makeButton(String label) {
        TextView btn = new TextView(context);
        btn.setText(label);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        btn.setTextColor(context.getColor(R.color.text_primary));
        btn.setGravity(Gravity.CENTER);
        btn.setBackgroundColor(context.getColor(R.color.surface_dark));
        return btn;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(dp(4), dp(4), dp(4), dp(4));
        p.height = dp(64);
        return p;
    }

    private LinearLayout.LayoutParams rowParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }
}
