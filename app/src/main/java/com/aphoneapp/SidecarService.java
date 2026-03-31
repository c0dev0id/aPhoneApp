package com.aphoneapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

public class SidecarService extends Service implements SidecarOverlay.OnDismissListener {

    private static final String CHANNEL_ID  = "sidecar";
    private static final int    NOTIF_ID    = 1;

    private SidecarOverlay sidecarOverlay;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Sidecar", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        startForeground(NOTIF_ID, new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build());

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        sidecarOverlay = new SidecarOverlay(this, wm, this);
        sidecarOverlay.show();
        AppState.get().setSidecarOverlay(sidecarOverlay);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppState.get().setSidecarOverlay(null);
        if (sidecarOverlay != null && sidecarOverlay.isShown()) {
            sidecarOverlay.remove();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDismiss() {
        stopSelf();
    }
}
