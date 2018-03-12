package com.ue.chess_life.entity;

import android.content.Context;

import com.ue.adapterdelegate.Item;
import com.ue.chess_life.R;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.constant.RouterLinks;
import com.ue.resource.constant.GameConstants;

/**
 * Created by hawk on 2016/11/22.
 */

public class GameItem implements Item {
    public String gameName;
    public int gameFlag;
    public String groupId;
    public int gameIcon;
    public String[] gameModes;

    public GameItem(Context context, int gameFlag) {
        this.gameFlag = gameFlag;
        switch (gameFlag) {
            case GameConstants.GAME_GB:
                gameName = context.getString(R.string.game_gobang);
                groupId = EaseConstants.GROUP_ID_GOBANG;
                gameIcon = R.mipmap.ic_logo_gobang;
                gameModes = RouterLinks.getGameFragments(true);
                break;
            case GameConstants.GAME_MO:
                gameName = context.getString(R.string.game_moon);
                groupId = EaseConstants.GROUP_ID_MOON;
                gameIcon = R.mipmap.ic_logo_moon;
                gameModes = RouterLinks.getGameFragments(false);
                break;
            case GameConstants.GAME_RV:
                gameName = context.getString(R.string.game_reversi);
                groupId = EaseConstants.GROUP_ID_REVERSI;
                gameIcon = R.mipmap.ic_logo_reversi;
                gameModes = RouterLinks.getGameFragments(true);
                break;
            case GameConstants.GAME_CC:
                gameName = context.getString(R.string.game_cchess);
                groupId = EaseConstants.GROUP_ID_CNCHESS;
                gameIcon = R.mipmap.ic_logo_cchess;
                gameModes = RouterLinks.getGameFragments(true);
                break;
            case GameConstants.GAME_IC:
                gameName = context.getString(R.string.game_ichess);
                groupId = EaseConstants.GROUP_ID_CHESS;
                gameIcon = R.mipmap.ic_logo_ichess;
                gameModes = RouterLinks.getGameFragments(false);
                break;
        }
    }
}