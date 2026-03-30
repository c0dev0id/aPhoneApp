package com.aphoneapp;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.telecom.VideoProfile;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

class CallOverlay {

    private final Context context;
    private final WindowManager windowManager;
    private View overlayView;

    private TextView textCallerName;
    private TextView textPhoneNumber;
    private TextView textStatus;
    private Button btnAccept;
    private Button btnDecline;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long callStartTime;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - callStartTime;
            long seconds = (elapsed / 1000) % 60;
            long minutes = (elapsed / 60000) % 60;
            long hours = elapsed / 3600000;
            String duration = hours > 0
                    ? String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
                    : String.format(Locale.US, "%d:%02d", minutes, seconds);
            textStatus.setText(duration);
            timerHandler.postDelayed(this, 1000);
        }
    };

    CallOverlay(Context context, WindowManager windowManager) {
        this.context = context;
        this.windowManager = windowManager;
    }

    void show() {
        if (overlayView != null) return;

        Context themedContext = new ContextThemeWrapper(context, R.style.Theme_APhoneApp);
        overlayView = LayoutInflater.from(themedContext).inflate(R.layout.overlay_calling_card, null);

        textCallerName = overlayView.findViewById(R.id.text_caller_name);
        textPhoneNumber = overlayView.findViewById(R.id.text_phone_number);
        textStatus = overlayView.findViewById(R.id.text_status);
        btnAccept = overlayView.findViewById(R.id.btn_accept);
        btnDecline = overlayView.findViewById(R.id.btn_decline);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;

        windowManager.addView(overlayView, params);
    }

    void remove() {
        timerHandler.removeCallbacks(timerRunnable);
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    boolean isShown() {
        return overlayView != null;
    }

    void update(Call call) {
        if (overlayView == null) return;

        String number = getPhoneNumber(call);
        String name = ContactHelper.getName(context, number);

        textCallerName.setText(name != null ? name : (number != null ? number : "Unknown"));
        textPhoneNumber.setText(number != null ? number : "");

        int state = call.getState();
        timerHandler.removeCallbacks(timerRunnable);

        switch (state) {
            case Call.STATE_RINGING:
                textStatus.setText("Incoming Call");
                btnAccept.setVisibility(View.VISIBLE);
                btnDecline.setText("Decline");
                btnAccept.setOnClickListener(v -> call.answer(VideoProfile.STATE_AUDIO_ONLY));
                btnDecline.setOnClickListener(v -> call.reject(false, null));
                break;

            case Call.STATE_DIALING:
            case Call.STATE_CONNECTING:
                textStatus.setText("Calling\u2026");
                btnAccept.setVisibility(View.GONE);
                btnDecline.setText("End Call");
                btnDecline.setOnClickListener(v -> call.disconnect());
                break;

            case Call.STATE_ACTIVE:
                btnAccept.setVisibility(View.GONE);
                btnDecline.setText("End Call");
                btnDecline.setOnClickListener(v -> call.disconnect());
                callStartTime = System.currentTimeMillis();
                timerHandler.post(timerRunnable);
                break;

            default:
                break;
        }
    }

    void onButton1(Call call) {
        if (call == null) return;
        if (call.getState() == Call.STATE_RINGING) {
            call.answer(VideoProfile.STATE_AUDIO_ONLY);
        }
    }

    void onButton2(Call call) {
        if (call == null) return;
        if (call.getState() == Call.STATE_RINGING) {
            call.reject(false, null);
        } else {
            call.disconnect();
        }
    }

    private String getPhoneNumber(Call call) {
        Call.Details details = call.getDetails();
        if (details != null && details.getHandle() != null) {
            return details.getHandle().getSchemeSpecificPart();
        }
        return null;
    }
}
