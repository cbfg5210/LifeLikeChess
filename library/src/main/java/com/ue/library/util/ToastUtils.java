package com.ue.library.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.text.Spannable;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.ue.library.R;

/**
 * Created by hawk on 2016/11/21.
 */

public class ToastUtils {
    private static Toast toast;

    private ToastUtils() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    private static Toast getToast(Context context) {
        if (context == null) {
            return null;
        }
        if (toast == null) {
            Context applicationContext = context.getApplicationContext();
            toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT);
        }
        return toast;
    }


    @UiThread
    public static void showShort(Context context, String msg) {
        Toast toast = getToast(context);
        if (toast != null) {
            toast.setText(msg);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @UiThread
    public static void showShort(Context context, @StringRes int msgRes) {
        Toast toast = getToast(context);
        if (toast != null) {
            toast.setText(msgRes);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @UiThread
    public static void showLong(Context context, String message) {
        Toast toast = getToast(context);
        if (toast != null) {
            toast.setText(message);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public static void showLong(Context context, @StringRes int messageRes) {
        showLong(context, context.getString(messageRes));
    }

    @UiThread
    public static void showChatToast(Context context, Spannable spanTxt, boolean isMyTxt, int deviation) {
        TextView contentView = new TextView(context);
        contentView.setText(spanTxt, TextView.BufferType.SPANNABLE);
        contentView.setGravity(Gravity.CENTER_VERTICAL);
        contentView.setTextColor(Color.BLACK);
        contentView.setTextSize(16f);
        contentView.setBackgroundResource(isMyTxt ? R.drawable.ic_left_bubble : R.drawable.ic_right_bubble);

        Toast chatToast = new Toast(context);
        chatToast.setDuration(Toast.LENGTH_LONG);
        deviation += 20;
        chatToast.setGravity(isMyTxt ? Gravity.TOP | Gravity.LEFT : Gravity.TOP | Gravity.RIGHT, 10, deviation);
        chatToast.setView(contentView);

        chatToast.show();
    }
}