package com.aphoneapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class HistoryTab implements TabContent {

    private static final int KEYCODE_UP      = 19;
    private static final int KEYCODE_DOWN    = 20;
    private static final int KEYCODE_BUTTON1 = 66;

    private static final class Entry {
        final int    type;
        final String number;
        final String name;

        Entry(int type, String number, String name) {
            this.type   = type;
            this.number = number;
            this.name   = name;
        }
    }

    private final Context context;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private int selectedPosition = 0;

    HistoryTab(Context context) {
        this.context = context;
    }

    @Override
    public View getView() {
        if (recyclerView != null) return recyclerView;

        recyclerView = new RecyclerView(context);
        adapter = new Adapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        loadEntries();
        return recyclerView;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (adapter == null || adapter.getItemCount() == 0) return;
        switch (keyCode) {
            case KEYCODE_UP:
                if (selectedPosition > 0) {
                    setSelected(selectedPosition - 1);
                }
                break;
            case KEYCODE_DOWN:
                if (selectedPosition < adapter.getItemCount() - 1) {
                    setSelected(selectedPosition + 1);
                }
                break;
            case KEYCODE_BUTTON1:
                callSelected();
                break;
        }
    }

    private void setSelected(int position) {
        int prev = selectedPosition;
        selectedPosition = position;
        adapter.notifyItemChanged(prev);
        adapter.notifyItemChanged(selectedPosition);
        recyclerView.scrollToPosition(selectedPosition);
    }

    private void callSelected() {
        if (adapter == null || adapter.entries.isEmpty()) return;
        Entry entry = adapter.entries.get(selectedPosition);
        placeCall(entry.number);
    }

    private void placeCall(String number) {
        Uri uri = Uri.fromParts("tel", number, null);
        TelecomManager tm = context.getSystemService(TelecomManager.class);
        Bundle extras = new Bundle();
        tm.placeCall(uri, extras);
    }

    private void loadEntries() {
        new Thread(() -> {
            List<Entry> entries = queryCallLog();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (adapter != null) {
                    adapter.setEntries(entries);
                    if (!entries.isEmpty()) {
                        setSelected(0);
                    }
                }
            });
        }).start();
    }

    private List<Entry> queryCallLog() {
        List<Entry> entries = new ArrayList<>();
        String[] projection = {
            CallLog.Calls.TYPE,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
        };
        try (Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null, null,
                CallLog.Calls.DATE + " DESC")) {
            if (cursor == null) return entries;
            int colType   = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE);
            int colNumber = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
            int colName   = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME);
            while (cursor.moveToNext()) {
                entries.add(new Entry(
                    cursor.getInt(colType),
                    cursor.getString(colNumber),
                    cursor.getString(colName)
                ));
            }
        }
        return entries;
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        final List<Entry> entries = new ArrayList<>();

        void setEntries(List<Entry> data) {
            entries.clear();
            entries.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Entry entry = entries.get(position);
            boolean selected = (position == selectedPosition);
            boolean missed   = (entry.type == CallLog.Calls.MISSED_TYPE);

            // Row background
            holder.itemView.setBackgroundColor(
                context.getColor(selected ? R.color.surface_card : android.R.color.transparent));

            // Icon
            String icon;
            int iconColor;
            switch (entry.type) {
                case CallLog.Calls.INCOMING_TYPE:
                    icon = "\u2198"; // ↘ incoming answered
                    iconColor = context.getColor(R.color.text_primary);
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    icon = "\u2715"; // ✕ missed
                    iconColor = context.getColor(R.color.missed_call);
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    icon = "\u2197"; // ↗ outgoing
                    iconColor = context.getColor(R.color.text_primary);
                    break;
                default:
                    icon = "\u2022"; // • other
                    iconColor = context.getColor(R.color.text_secondary);
            }
            holder.textIcon.setText(icon);
            holder.textIcon.setTextColor(iconColor);

            // Content: number + name
            String display = entry.number != null ? entry.number : "";
            if (entry.name != null && !entry.name.isEmpty()) {
                display += " (" + entry.name + ")";
            }
            holder.textContent.setText(display);
            holder.textContent.setTextColor(context.getColor(
                missed ? R.color.missed_call : R.color.text_primary));

            // Touch initiates call
            holder.itemView.setOnClickListener(v -> placeCall(entry.number));
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textIcon;
            final TextView textContent;

            ViewHolder(View v) {
                super(v);
                textIcon    = v.findViewById(R.id.text_icon);
                textContent = v.findViewById(R.id.text_content);
            }
        }
    }
}
