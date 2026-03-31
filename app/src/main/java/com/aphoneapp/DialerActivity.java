package com.aphoneapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Declared solely to satisfy the default dialer role qualification check.
 * The system requires a dedicated exported Activity with the DIAL/VIEW tel: intent filters.
 * All real work is delegated to PermissionActivity.
 */
public class DialerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, PermissionActivity.class));
        finish();
    }
}
