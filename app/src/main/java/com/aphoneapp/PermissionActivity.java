package com.aphoneapp;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

public class PermissionActivity extends Activity {

    private static final int REQUEST_RUNTIME_PERMISSIONS = 1;
    private static final int REQUEST_OVERLAY_PERMISSION  = 2;
    private static final int REQUEST_DEFAULT_DIALER      = 3;

    private static final String[] RUNTIME_PERMISSIONS = {
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Transparent — no layout, no UI of our own.
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndProceed();
    }

    private void checkAndProceed() {
        if (!hasRuntimePermissions()) {
            requestPermissions(RUNTIME_PERMISSIONS, REQUEST_RUNTIME_PERMISSIONS);
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())),
                    REQUEST_OVERLAY_PERMISSION);
            return;
        }

        if (!isDefaultDialer()) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                startActivityForResult(
                        roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER),
                        REQUEST_DEFAULT_DIALER);
            }
            return;
        }

        startForegroundService(new Intent(this, SidecarService.class));
        finish();
    }

    private boolean isDefaultDialer() {
        return getSystemService(RoleManager.class).isRoleHeld(RoleManager.ROLE_DIALER);
    }

    private boolean hasRuntimePermissions() {
        for (String permission : RUNTIME_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // onResume re-evaluates on return
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // onResume re-evaluates on return
    }
}
