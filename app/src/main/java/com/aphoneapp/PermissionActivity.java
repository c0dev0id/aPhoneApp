package com.aphoneapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.widget.Button;
import android.widget.TextView;

public class PermissionActivity extends Activity {

    private static final int REQUEST_RUNTIME_PERMISSIONS = 1;
    private static final int REQUEST_OVERLAY_PERMISSION = 2;
    private static final int REQUEST_DEFAULT_DIALER = 3;

    private static final String[] RUNTIME_PERMISSIONS = {
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
    };

    private TextView textStatus;
    private Button btnGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        textStatus = findViewById(R.id.text_status);
        btnGrant = findViewById(R.id.btn_grant);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndProceed();
    }

    private void checkAndProceed() {
        if (!hasRuntimePermissions()) {
            textStatus.setText("aPhoneApp needs access to your contacts, call log, and phone to function.");
            btnGrant.setOnClickListener(v -> requestPermissions(RUNTIME_PERMISSIONS, REQUEST_RUNTIME_PERMISSIONS));
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            textStatus.setText("aPhoneApp needs permission to display over other apps.");
            btnGrant.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            });
            return;
        }

        if (!isDefaultDialer()) {
            textStatus.setText("aPhoneApp must be set as the default phone app to handle calls.");
            btnGrant.setOnClickListener(v -> {
                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, REQUEST_DEFAULT_DIALER);
            });
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // onResume will re-evaluate — no need to act here
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // onResume will re-evaluate — no need to act here
    }

    private boolean hasRuntimePermissions() {
        for (String permission : RUNTIME_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean isDefaultDialer() {
        TelecomManager telecom = getSystemService(TelecomManager.class);
        return getPackageName().equals(telecom.getDefaultDialerPackage());
    }
}
