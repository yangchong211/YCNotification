package com.yc.notifymessage;

import android.animation.Animator;

public interface NotificationService<T> {

    /**
     * 有新通知需要展示
     */
    void show(T notification);

    /**
     * 需要移除指定的通知
     */
    void cancel(T notification , Animator.AnimatorListener listener);

    /**
     * 判断是否在展示状态
     * @return
     */
    boolean isShowing();

    /**
     * 设置是否展示状态
     * @param isShowing
     */
    void changeIsShowing(boolean isShowing);

}
