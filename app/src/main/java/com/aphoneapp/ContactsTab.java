package com.aphoneapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class ContactsTab implements TabContent {

    private static final int KEYCODE_UP      = 19;
    private static final int KEYCODE_DOWN    = 20;
    private static final int KEYCODE_BUTTON1 = 66;

    private static final class Entry {
        final String name;
        final String number;
        final String typeLabel;

        Entry(String name, String number, String typeLabel) {
            this.name      = name;
            this.number    = number;
            this.typeLabel = typeLabel;
        }
    }

    private final Context context;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private int selectedPosition = 0;

    ContactsTab(Context context) {
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
                if (selectedPosition > 0) setSelected(selectedPosition - 1);
                break;
            case KEYCODE_DOWN:
                if (selectedPosition < adapter.getItemCount() - 1) setSelected(selectedPosition + 1);
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
        placeCall(adapter.entries.get(selectedPosition).number);
    }

    private void placeCall(String number) {
        Uri uri = Uri.fromParts("tel", number, null);
        TelecomManager tm = context.getSystemService(TelecomManager.class);
        tm.placeCall(uri, new Bundle());
    }

    private void loadEntries() {
        new Thread(() -> {
            List<Entry> entries = queryContacts();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (adapter != null) {
                    adapter.setEntries(entries);
                    if (!entries.isEmpty()) setSelected(0);
                }
            });
        }).start();
    }

    private List<Entry> queryContacts() {
        List<Entry> entries = new ArrayList<>();
        String[] projection = {
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
        };
        // Favorites (STARRED=1) first, then alphabetical by display name
        String sortOrder = ContactsContract.CommonDataKinds.Phone.STARRED + " DESC, "
                + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null, sortOrder)) {
            if (cursor == null) return entries;
            int colName   = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int colNumber = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int colType   = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE);
            int colLabel  = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL);
            while (cursor.moveToNext()) {
                CharSequence typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                        context.getResources(),
                        cursor.getInt(colType),
                        cursor.getString(colLabel));
                entries.add(new Entry(
                        cursor.getString(colName),
                        cursor.getString(colNumber),
                        typeLabel.toString().toLowerCase()));
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
                    .inflate(R.layout.item_contact, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Entry entry = entries.get(position);
            boolean selected = (position == selectedPosition);

            holder.itemView.setBackgroundColor(
                context.getColor(selected ? R.color.surface_card : android.R.color.transparent));

            holder.textName.setText(entry.name != null ? entry.name : entry.number);
            holder.textNumber.setText(entry.typeLabel + ": " + entry.number);

            holder.itemView.setOnClickListener(v -> placeCall(entry.number));
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textName;
            final TextView textNumber;

            ViewHolder(View v) {
                super(v);
                textName   = v.findViewById(R.id.text_name);
                textNumber = v.findViewById(R.id.text_number);
            }
        }
    }
}
