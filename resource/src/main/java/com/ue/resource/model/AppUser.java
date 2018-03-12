package com.ue.resource.model;

import com.ue.resource.util.GsonHolder;

/**
 * Created by hawk on 2017/6/3.
 */

public class AppUser {
    public String userName;
    public String userNick;
    public boolean isMale;

    public int gbGameCount;
    public int gbGameWinCount;
    public int ccGameCount;
    public int ccGameWinCount;
    public int icGameCount;
    public int icGameWinCount;
    public int rvGameCount;
    public int rvGameWinCount;
    public int mcGameCount;
    public int mcGameWinCount;

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
