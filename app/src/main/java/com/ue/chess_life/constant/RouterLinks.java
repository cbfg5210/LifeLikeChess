package com.ue.chess_life.constant;

import com.ue.chess_life.feature.game.OnlineModeFragment;
import com.ue.chess_life.feature.game.OfflineModeFragment;

/**
 * Created by hawk on 2017/6/7.
 */

public final class RouterLinks {
    private static final String EMPTY = "";
    public static final String FRAGMENT_ONLINE_LINK = OnlineModeFragment.class.getName();
    public static final String FRAGMENT_OFFLINE_LINK = OfflineModeFragment.class.getName();

    public static String[] getGameFragments(boolean hasSingleMode) {
        if (hasSingleMode) {
            return new String[]{
                    FRAGMENT_OFFLINE_LINK,
                    FRAGMENT_OFFLINE_LINK,
                    FRAGMENT_ONLINE_LINK,
                    FRAGMENT_ONLINE_LINK,
            };
        }
        return new String[]{
                FRAGMENT_OFFLINE_LINK,
                EMPTY,
                FRAGMENT_ONLINE_LINK,
                FRAGMENT_ONLINE_LINK,
        };
    }
}
