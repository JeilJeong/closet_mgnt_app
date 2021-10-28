package com.graduate.lookatv2.keyfunction;

import android.app.Activity;
import android.widget.Toast;

public class BackButton {
    private long        backKeyPressedTime;
    private long        TIME_INTERVAL;
    private Toast       toast;
    private Activity    activity;

    public BackButton(Activity activity) {
        this.activity = activity;
        this.backKeyPressedTime = 0;
        this.TIME_INTERVAL = 2000;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + TIME_INTERVAL) {
            backKeyPressedTime = System.currentTimeMillis();
            showMsg();
        }
        else {
            if (toast != null) toast.cancel();
            activity.finish();
        }
    }

    public void showMsg() {
        toast = Toast.makeText(activity, "'뒤로' 한 번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT);
        toast.show();
    }
}
