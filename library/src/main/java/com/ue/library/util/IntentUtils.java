package com.ue.library.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.ue.library.R;

import java.util.Locale;

import static android.os.Looper.getMainLooper;

/**
 * Created by hawk on 2017/9/3.
 */
public final class IntentUtils {
    public static void share(Context context, String shareContent) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);//设置分享行为
        intent.setType("text/plain");//设置分享内容的类型
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share));//添加分享内容标题
        intent.putExtra(Intent.EXTRA_TEXT, shareContent);//添加分享内容
        intent = Intent.createChooser(intent, context.getString(R.string.share));
        try {
            context.startActivity(intent);
        } catch (Exception exp) {
            ToastUtils.showShort(context, context.getString(R.string.error_share));
        }
    }

    public static void sendEMail(Context context, String toEmail, String subject) {
        Uri uri = Uri.fromParts("mailto", toEmail, null);
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent = Intent.createChooser(intent, subject);
        try {
            context.startActivity(intent);
        } catch (Exception exp) {
            ToastUtils.showShort(context, context.getString(R.string.error_send_mail));
        }
    }

    public static void openBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception exp) {
            ToastUtils.showShort(context, context.getString(R.string.error_open_browser));
        }
    }

    public static void forwardAppDetailPage(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));

        boolean isZhLanguage = Locale.getDefault().getLanguage().equals("zh");
        String toastMsg = isZhLanguage ? "定位不到设置页面,请手动前往设置" : "Failed to open the setting page,please go to setting page manually.";

        safelyStartActivity(context, intent, toastMsg);
    }

    public static boolean safelyStartActivity(final Context context, Intent intent, final String errTip) {
        if (context == null) {
            return false;
        }
        if (intent == null) {
            return false;
        }
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception exp) {
            if (TextUtils.isEmpty(errTip)) {
                return false;
            }
            Looper mainLooper = getMainLooper();
            if (mainLooper == null) {
                return false;
            }
            new Handler(mainLooper).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, errTip, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return false;
    }
}