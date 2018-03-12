package com.ue.chess_life.util;

import android.content.Context;
import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.ue.cchess.widget.CCBoardDelegate;
import com.ue.chess_life.R;
import com.ue.gobang.widget.GBBoardDelegate;
import com.ue.ichess.widget.ICBoardDelegate;
import com.ue.moon.widget.MOBoardDelegate;
import com.ue.resource.GameBoardDelegate;
import com.ue.resource.constant.GameConstants;
import com.ue.reversi.widget.RVBoardDelegate;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by hawk on 2016/12/3.
 */

public class GameUtils {
    public static void sendCMDMessage(String receipt, int actionVal, Map<String, Object> attrs) {
        if (TextUtils.isEmpty(receipt)) {
            return;
        }
        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        EMCmdMessageBody cmdBody = new EMCmdMessageBody("" + actionVal);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo(receipt);
        if (attrs != null) {
            Iterator<String> attrsKeyIterator = attrs.keySet().iterator();
            while (attrsKeyIterator.hasNext()) {
                String key = attrsKeyIterator.next();
                Object value = attrs.get(key);
                if (value instanceof Integer) {
                    cmdMsg.setAttribute(key, (Integer) attrs.get(key));
                } else if (value instanceof String) {
                    cmdMsg.setAttribute(key, (String) attrs.get(key));
                }
            }
        }
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);
    }

    public static String getGameName(Context context, int gameFlag) {
        switch (gameFlag) {
            case GameConstants.GAME_CC:
                return context.getString(R.string.game_cchess);
            case GameConstants.GAME_MO:
                return context.getString(R.string.game_moon);
            case GameConstants.GAME_IC:
                return context.getString(R.string.game_ichess);
            case GameConstants.GAME_GB:
                return context.getString(R.string.game_gobang);
            case GameConstants.GAME_RV:
                return context.getString(R.string.game_reversi);
            default:
                return context.getString(R.string.game_unknown);
        }
    }

    public static GameBoardDelegate getGameBoardDelegate(int gameFlag) {
        switch (gameFlag) {
            case GameConstants.GAME_CC:
                return new CCBoardDelegate();
            case GameConstants.GAME_IC:
                return new ICBoardDelegate();
            case GameConstants.GAME_MO:
                return new MOBoardDelegate();
            case GameConstants.GAME_GB:
                return new GBBoardDelegate();
            case GameConstants.GAME_RV:
                return new RVBoardDelegate();
        }
        return null;
    }
}