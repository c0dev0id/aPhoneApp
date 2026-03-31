package com.aphoneapp;

import android.view.View;

interface TabContent {
    View getView();
    void onKeyDown(int keyCode);
    default void onActivated()   {}
    default void onDeactivated() {}
}
