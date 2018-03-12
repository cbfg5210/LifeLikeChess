package com.ue.chess_life.util;

import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.ue.library.util.SPUtils;
import com.ue.resource.model.AppUser;
import com.ue.resource.util.GsonHolder;

/**
 * Created by hawk on 2017/10/27.
 */

public class AppUserUtils {
    private static String LAST_USER_NAME = "LAST_USER_NAME";

    public static void setLastUserName(String username) {
        SPUtils.putString(LAST_USER_NAME, username);
    }

    public static String getLastUsername() {
        return SPUtils.getString(LAST_USER_NAME, null);
    }

    public static AppUser getCurrentUser() {
        String currentUserName = EMClient.getInstance().getCurrentUser();
        if (TextUtils.isEmpty(currentUserName)) {
            currentUserName = "wo";
        }
        return getUser(currentUserName);
    }

    public static AppUser getUser(String userName) {
        AppUser currentUser = null;

        String appUserStr = SPUtils.getString(userName, "");
        if (!TextUtils.isEmpty(appUserStr)) {
            currentUser = GsonHolder.getGson().fromJson(appUserStr, AppUser.class);
        }

        if (currentUser == null) {
            currentUser = new AppUser();
            currentUser.userName = userName;
            currentUser.userNick = userName;
            currentUser.isMale = true;
        }
        return currentUser;
    }

    public static void saveAppUser(AppUser appUser) {
        SPUtils.putString(appUser.userName, appUser.toString());
    }
}
