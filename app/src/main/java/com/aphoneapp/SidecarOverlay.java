package com.aphoneapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

class SidecarOverlay {

    interface OnDismissListener {
        void onDismiss();
    }

    static final int TAB_HISTORY  = 0;
    static final int TAB_CONTACTS = 1;
    static final int TAB_DIALPAD  = 2;
    private static final int TAB_COUNT = 3;

    private static final String DMD_REMOTE_ACTION = "com.thorkracing.wireddevices.keypress";
    private static final int KEYCODE_UP      = 19;
    private static final int KEYCODE_DOWN    = 20;
    private static final int KEYCODE_LEFT    = 21;
    private static final int KEYCODE_RIGHT   = 22;
    private static final int KEYCODE_BUTTON1 = 66;
    private static final int KEYCODE_BUTTON2 = 111;

    private final Context context;
    private final WindowManager windowManager;
    private final OnDismissListener dismissListener;

    private View overlayView;
    private TextView[] tabViews;
    private View tabIndicator;
    private FrameLayout contentFrame;

    private int currentTab = TAB_HISTORY;
    private boolean dialpadActive = false;
    private TabContent[] tabContents;

    private final BroadcastReceiver remoteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.hasExtra("key_press")) return;
            handleRemoteKey(intent.getIntExtra("key_press", 0));
        }
    };

    SidecarOverlay(Context context, WindowManager windowManager, OnDismissListener dismissListener) {
        this.context = context;
        this.windowManager = windowManager;
        this.dismissListener = dismissListener;
    }

    void show() {
        if (overlayView != null) return;

        Context themedContext = new ContextThemeWrapper(context, R.style.Theme_APhoneApp);
        overlayView = LayoutInflater.from(themedContext).inflate(R.layout.overlay_sidecar, null);

        tabViews = new TextView[]{
            overlayView.findViewById(R.id.tab_history),
            overlayView.findViewById(R.id.tab_contacts),
            overlayView.findViewById(R.id.tab_dialpad)
        };
        tabIndicator = overlayView.findViewById(R.id.tab_indicator);
        contentFrame = overlayView.findViewById(R.id.content_frame);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        tabContents = new TabContent[]{
            new HistoryTab(context),
            new ContactsTab(context),
            new DialpadTab(context),
        };

        windowManager.addView(overlayView, params);
        selectTab(TAB_HISTORY);
        registerRemoteReceiver();
    }

    void remove() {
        unregisterRemoteReceiver();
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    boolean isShown() {
        return overlayView != null;
    }

    int getCurrentTab() {
        return currentTab;
    }

    FrameLayout getContentFrame() {
        return contentFrame;
    }

    /** Called by PhoneCallService to dim the sidecar during an incoming call. */
    void setDimmed(boolean dimmed) {
        if (overlayView == null) return;
        overlayView.setAlpha(dimmed ? 0.3f : 1.0f);
    }

    private void selectTab(int tab) {
        currentTab = tab;

        int activeColor   = context.getColor(R.color.text_primary);
        int inactiveColor = context.getColor(R.color.text_secondary);
        for (int i = 0; i < TAB_COUNT; i++) {
            tabViews[i].setTextColor(i == tab ? activeColor : inactiveColor);
        }

        // Load tab content
        contentFrame.removeAllViews();
        if (tabContents[tab] != null) {
            contentFrame.addView(tabContents[tab].getView());
        }

        // Slide the indicator under the selected tab
        tabViews[tab].post(() -> {
            if (tabIndicator == null) return;
            ViewGroup.LayoutParams lp = tabIndicator.getLayoutParams();
            lp.width = tabViews[tab].getWidth();
            tabIndicator.setLayoutParams(lp);
            tabIndicator.setX(tabViews[tab].getLeft());
        });
    }

    private void handleRemoteKey(int keyCode) {
        if (dialpadActive) {
            if (keyCode == KEYCODE_BUTTON2) {
                dialpadActive = false;
                tabContents[TAB_DIALPAD].onDeactivated();
            } else {
                tabContents[TAB_DIALPAD].onKeyDown(keyCode);
            }
            return;
        }

        // Tab bar mode
        switch (keyCode) {
            case KEYCODE_LEFT:
                if (currentTab > 0) selectTab(currentTab - 1);
                break;
            case KEYCODE_RIGHT:
                if (currentTab < TAB_COUNT - 1) selectTab(currentTab + 1);
                break;
            case KEYCODE_UP:
            case KEYCODE_DOWN:
            case KEYCODE_BUTTON1:
                if (currentTab == TAB_DIALPAD) {
                    dialpadActive = true;
                    tabContents[TAB_DIALPAD].onActivated();
                } else if (tabContents[currentTab] != null) {
                    tabContents[currentTab].onKeyDown(keyCode);
                }
                break;
            case KEYCODE_BUTTON2:
                remove();
                if (dismissListener != null) dismissListener.onDismiss();
                break;
        }
    }

    private void registerRemoteReceiver() {
        IntentFilter filter = new IntentFilter(DMD_REMOTE_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(remoteReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(remoteReceiver, filter);
        }
    }

    private void unregisterRemoteReceiver() {
        try {
            context.unregisterReceiver(remoteReceiver);
        } catch (IllegalArgumentException ignore) {}
    }
}
