package com.aphoneapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telecom.Call;
import android.telecom.InCallService;
import android.view.WindowManager;

public class PhoneCallService extends InCallService {

    private static final String DMD_REMOTE_ACTION = "com.thorkracing.wireddevices.keypress";
    private static final int KEYCODE_BUTTON1 = 66;
    private static final int KEYCODE_BUTTON2 = 111;

    private CallOverlay callOverlay;

    private final Call.Callback callCallback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            if (callOverlay != null) {
                callOverlay.update(call);
            }
            if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {
                call.unregisterCallback(this);
            }
        }
    };

    private final BroadcastReceiver remoteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.hasExtra("key_press")) return;
            int keyCode = intent.getIntExtra("key_press", 0);
            Call call = getFirstCall();
            if (callOverlay == null || call == null) return;
            if (keyCode == KEYCODE_BUTTON1) {
                callOverlay.onButton1(call);
            } else if (keyCode == KEYCODE_BUTTON2) {
                callOverlay.onButton2(call);
            }
        }
    };

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);

        SidecarOverlay sidecar = AppState.get().getSidecarOverlay();
        if (sidecar != null && sidecar.isShown()) {
            sidecar.setDimmed(true);
        }

        if (callOverlay == null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            callOverlay = new CallOverlay(this, wm);
            callOverlay.show();
            registerRemoteReceiver();
        }

        call.registerCallback(callCallback);
        callOverlay.update(call);
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        call.unregisterCallback(callCallback);

        if (getCalls().isEmpty()) {
            unregisterRemoteReceiver();
            if (callOverlay != null) {
                callOverlay.remove();
                callOverlay = null;
            }

            SidecarOverlay sidecar = AppState.get().getSidecarOverlay();
            if (sidecar != null && sidecar.isShown()) {
                sidecar.setDimmed(false);
            }
        }
    }

    private void registerRemoteReceiver() {
        IntentFilter filter = new IntentFilter(DMD_REMOTE_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(remoteReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(remoteReceiver, filter);
        }
    }

    private void unregisterRemoteReceiver() {
        try {
            unregisterReceiver(remoteReceiver);
        } catch (IllegalArgumentException ignore) {}
    }

    private Call getFirstCall() {
        if (getCalls().isEmpty()) return null;
        // Prefer ringing call (incoming takes priority)
        for (Call c : getCalls()) {
            if (c.getState() == Call.STATE_RINGING) return c;
        }
        return getCalls().get(0);
    }
}
