package com.ue.library.util;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ue.library.base.LifecycleCallbacksAdapter;

/**
 * 回调判空
 */

public class CallbackUtils {

    /**
     * 显示DialogFragment
     * 避免NullPointerException和IllegalStateException
     *
     * @param context
     * @param dialog
     */
    public static void showDialogFragment(Fragment context, DialogFragment dialog) {
        if (dialog == null || dialog.isAdded()) {
            return;
        }
        if (!isFragmentValid(context)) {
            return;
        }
        if (LifecycleCallbacksAdapter.get().hasPaused(context.getActivity())) {
            return;
        }
        String tag = dialog.getClass().getSimpleName();
        Fragment fragment = context.getChildFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            DialogFragment dialogFragment = (DialogFragment) fragment;
            dialogFragment.dismiss();
        }
        dialog.show(context.getChildFragmentManager(), tag);
    }

    /**
     * 显示DialogFragment
     *
     * @param context
     * @param dialog
     */
    public static void showDialogFragment(AppCompatActivity context, DialogFragment dialog) {
        if (dialog == null || dialog.isAdded()) {
            return;
        }
        if (!isActivityValid(context)) {
            return;
        }
        if (LifecycleCallbacksAdapter.get().hasPaused(context)) {
            return;
        }
        String tag = dialog.getClass().getSimpleName();
        Fragment fragment = context.getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            DialogFragment dialogFragment = (DialogFragment) fragment;
            dialogFragment.dismiss();
        }
        dialog.show(context.getSupportFragmentManager(), tag);
    }

    /**
     * fragment内部回调调用
     * 备注：在onResume前调用无效
     *
     * @param fragment
     * @return
     */
    public static boolean isFragmentValid(Fragment fragment) {
        return (fragment != null && isActivityValid(fragment.getActivity()) && fragment.isAdded());
    }

    public static boolean isActivityValid(Activity activity) {
        return (activity != null && !activity.isFinishing());
    }

    public static boolean isContextValid(Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            return isActivityValid((Activity) context);
        }
        return true;
    }

    /**
     * onCreate时调用无效
     *
     * @param view
     * @return
     */
    public static boolean isViewValid(View view) {
        if (view == null) {
            return false;
        }
        if (!ViewCompat.isAttachedToWindow(view)) {
            return false;
        }
        return true;
    }
}