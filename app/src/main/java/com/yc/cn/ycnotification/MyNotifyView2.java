package com.yc.cn.ycnotification;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.yc.notifymessage.CustomNotification;
import com.yc.notifymessage.NotificationView;

public class MyNotifyView2 extends NotificationView<Void> {

    public MyNotifyView2(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public int provideLayoutResourceId() {
        return R.layout.notify_custom_view2;
    }

    @Override
    public int[] provideClickableViewArray() {
        return new int[]{R.id.btn_click};
    }

    @Override
    protected boolean onClick(View view, int id) {
        switch (id) {
            case R.id.btn_click:
                Toast.makeText(view.getContext(),"点击吐司",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void bindNotification(CustomNotification<Void> notification) {
        super.bindNotification(notification);
    }
}
