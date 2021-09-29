package com.yc.notifymessage;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2018/11/9
 *     desc  : 通知栏 通知链表节点
 *     revise:
 * </pre>
 */
public class NotificationNode {

    static final int EQUALS = 0;
    static final int ERROR = -1;
    static final int GREATER = 1;
    static final int SMALLER = 2;
    public static final int ANIM_DURATION = 200;

    protected CustomNotification mNotification;
    public boolean mIsShowing; // 用于标志改 Notification 是否展示了
    @Nullable
    private NotifyContainerView mNotificationContainerView;
    @Nullable
    private WindowManager mWindowManager;
    @Nullable
    private WindowManager.LayoutParams mLayoutParams;
    private final NotificationManager mNotificationManager;


    public NotificationNode(CustomNotification notification,
                            NotificationManager notificationManager) {
        mNotification = notification;
        mNotificationManager = notificationManager;
    }

    private int getNotificationLocationY(Context context) {
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

    int getPriority() {
        return mNotification == null ? -1 : mNotification.mPriority;
    }

    void setShowing(boolean showing) {
        mIsShowing = showing;
    }

    boolean isShowing() {
        return mIsShowing;
    }

    CustomNotification getNotification() {
        return mNotification;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NotificationNode)) {
            return false;
        }
        CustomNotification notification = ((NotificationNode) obj).mNotification;
        return notification != null && getNotification() != null && notification.mType == getNotification().mType || super.equals(obj);
    }

    @Override
    public int hashCode() {
        return mNotification == null ? -1 : mNotification.mType;
    }

    int compareTo(@NonNull NotificationNode o) {
        if (getNotification() == null || o.getNotification() == null) {
            return ERROR;
        }
        int result = getNotification().mPriority - o.getNotification().mPriority;
        return result > 0 ? GREATER : result < 0 ? SMALLER : EQUALS;
    }

    protected void handleShow() {
        try {
            if (mNotification == null || mNotification.getActivity() == null) {
                LoggerUtils.log("handleShow returned: mNotification == null || mNotification.getActivity() == null");
                return;
            }
            initNotificationView(mNotification.getActivity());
            if (mNotificationContainerView == null
                    || mNotificationContainerView.getParent() != null
                    || mWindowManager == null
                    || mLayoutParams == null) {
                String reason = "unknown";
                if (mNotificationContainerView == null) {
                    reason = "mNotificationContainerView == null";
                } else if (mNotificationContainerView.getParent() != null) {
                    reason = "mNotificationContainerView.getParent() != null";
                } else if (mWindowManager == null) {
                    reason = "mWindowManager == null";
                } else if (mLayoutParams == null) {
                    reason = "mLayoutParams == null";
                }
                LoggerUtils.log("handleShow returned: " + reason);
                return;
            }
            if (isActivityNotAlive(mNotificationContainerView.getActivity())) {
                LoggerUtils.log("handleShow returned: activity is finishing or destroyed!");
                return;
            }
            LoggerUtils.log("handleShow before addView: mLayoutParams.token" + mNotificationContainerView.getWindowToken());
            mLayoutParams.token = mNotificationContainerView.getWindowToken();
            mWindowManager.addView(mNotificationContainerView, mLayoutParams);
            LoggerUtils.log("handleShow after addView");
            setShowing(true);
            mNotificationContainerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (mNotificationContainerView == null) {
                        LoggerUtils.log("handleShow animation: mNotificationContainerView == null");
                        return;
                    } else if (isActivityNotAlive(mNotificationContainerView.getActivity())) {
                        LoggerUtils.log("handleShow animation: mNotificationContainerView.getActivity() is not alive : "
                                + mNotificationContainerView.getActivity());
                        return;
                    } else if (mNotification == null) {
                        LoggerUtils.log("handleShow animation: mNotification == null");
                        return;
                    }
                    resetAnimation(mNotificationContainerView);
                    mNotificationContainerView.animate().translationY(0).setDuration(ANIM_DURATION).start();
                    mNotificationManager.startTimeout(mNotification.mType, mNotification.getTimeout());
                    mNotificationContainerView.removeOnLayoutChangeListener(this);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isActivityNotAlive(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !(context instanceof Activity) || ((Activity) context).isFinishing()
                    || ((Activity) context).isDestroyed();
        }
        return !(context instanceof Activity) || ((Activity) context).isFinishing();
    }

    private void initNotificationView(@NonNull Context context) {
        if (mNotification.getNotificationView().getView() == null
                || mNotification.getNotificationView().getView().getParent() != null) {
            return;
        }
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.TOP;
        mLayoutParams.x = 0;
        mLayoutParams.y = getNotificationLocationY(context);

        mNotificationContainerView = new NotifyContainerView(context);
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mNotificationContainerView.setLayoutParams(vl);
        mNotificationContainerView.setOnDismissListener(new NotifyContainerView.OnDismissListener() {
            @Override
            public void onDismiss() {
                mNotificationManager.hideNotification();
            }
        });
        mNotificationContainerView.addView(mNotification.getNotificationView().getView());
        mNotificationContainerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mNotificationContainerView.setTranslationY(-mNotificationContainerView.getHeight());
                mNotificationContainerView.removeOnLayoutChangeListener(this);
            }
        });
        mNotificationContainerView.setCollapsible(mNotification.mIsCollapsible);
    }

    protected void handleHide(final Animator.AnimatorListener listener) {
        if (mNotificationContainerView == null) {
            return;
        }
        resetAnimation(mNotificationContainerView);
        mNotificationContainerView.animate().translationY(-mNotificationContainerView.getHeight()).setDuration(ANIM_DURATION);
        mNotificationContainerView.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) {
                    listener.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mWindowManager != null && mNotificationContainerView != null
                        && mNotificationContainerView.getParent() != null) {
                    mWindowManager.removeViewImmediate(mNotificationContainerView);
                }
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (listener != null) {
                    listener.onAnimationCancel(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (listener != null) {
                    listener.onAnimationRepeat(animation);
                }
            }
        });
        mNotificationContainerView.animate().start();
        setShowing(false);
    }

    /**
     * 取消当前的动画
     */
    private void resetAnimation(View view) {
        if (view == null) {
            return;
        }
        view.animate().cancel();
        view.animate().setListener(null);
    }

}
