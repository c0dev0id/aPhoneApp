package com.aphoneapp;

class AppState {

    private static final AppState INSTANCE = new AppState();

    private SidecarOverlay sidecarOverlay;

    private AppState() {}

    static AppState get() {
        return INSTANCE;
    }

    void setSidecarOverlay(SidecarOverlay overlay) {
        sidecarOverlay = overlay;
    }

    SidecarOverlay getSidecarOverlay() {
        return sidecarOverlay;
    }
}
