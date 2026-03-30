package com.aphoneapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends Activity implements SidecarOverlay.OnDismissListener {

    private SidecarOverlay sidecarOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        sidecarOverlay = new SidecarOverlay(this, wm, this);
        sidecarOverlay.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sidecarOverlay != null && sidecarOverlay.isShown()) {
            sidecarOverlay.remove();
        }
    }

    @Override
    public void onDismiss() {
        finish();
    }
}
