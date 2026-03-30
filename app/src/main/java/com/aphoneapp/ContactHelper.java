package com.aphoneapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

class ContactHelper {

    static String getName(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return null;
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        try (Cursor cursor = context.getContentResolver().query(
                uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }
        return null;
    }
}
