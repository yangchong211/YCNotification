package com.yc.notifymessage;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.Window;

public final class NotificationUtils {


    public static int getNotificationLocationY(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            Window window = activity.getWindow();
            if (window == null) {
                return 0;
            }
            View view = window.findViewById(android.R.id.content);
            if (view == null) {
                return 0;
            }
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            return location[1];
        } else {
            return 0;
        }
    }

}
