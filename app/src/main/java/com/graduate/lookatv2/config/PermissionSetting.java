package com.graduate.lookatv2.config;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class PermissionSetting {
//  [setting] declared permission list in AndroidManifest.xml
    private static final int PERMISSIONS_REQUEST_CODE = 1000;
    private static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO
    };

    private static boolean hasPermissions(Activity activity, String[] permissions) {
        int result;
        for (String perms : permissions) {
            result = ContextCompat.checkSelfPermission(activity.getApplicationContext(), perms);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

//  [setting] Assume that the user will accept all permissions.
    public static void setPermissionOnRuntime(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(activity, PERMISSIONS)) {
                activity.requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }
}
