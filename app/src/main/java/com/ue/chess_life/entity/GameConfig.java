package com.ue.chess_life.entity;

import android.content.Context;

import com.ue.chess_life.R;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.constant.SPKeys;
import com.ue.resource.constant.GameConstants;

/**
 * Created by hawk on 2017/11/9.
 */

public class GameConfig {
    private int gameFlag;
    private String firstColor;
    private String lastColor;
    private String roomOne;
    private String roomTwo;
    private int aiNum;
    private String spAILevelKey;
    private String spRoomLevelKey;

    public GameConfig(Context context, int gameFlag) {
        this.gameFlag = gameFlag;

        switch (gameFlag) {
            case GameConstants.GAME_CC:
                firstColor = context.getString(R.string.red);
                lastColor = context.getString(R.string.black);
                roomOne = EaseConstants.ROOM_CC_1;
                roomTwo = EaseConstants.ROOM_CC_2;
                spAILevelKey = SPKeys.AI_LEVEL_CC;
                spRoomLevelKey = SPKeys.ROOM_LEVEL_CC;
                aiNum = 3;
                break;
            case GameConstants.GAME_IC:
                firstColor = context.getString(R.string.white);
                lastColor = context.getString(R.string.black);
                roomOne = EaseConstants.ROOM_IC_1;
                roomTwo = EaseConstants.ROOM_IC_2;
                spAILevelKey = SPKeys.AI_LEVEL_IC;
                spRoomLevelKey = SPKeys.ROOM_LEVEL_IC;
                break;
            case GameConstants.GAME_MO:
                firstColor = context.getString(R.string.black);
                lastColor = context.getString(R.string.white);
                roomOne = EaseConstants.ROOM_MO_1;
                roomTwo = EaseConstants.ROOM_MO_2;
                spAILevelKey = SPKeys.AI_LEVEL_MO;
                spRoomLevelKey = SPKeys.ROOM_LEVEL_MO;
                break;
            case GameConstants.GAME_GB:
                firstColor = context.getString(R.string.black);
                lastColor = context.getString(R.string.white);
                roomOne = EaseConstants.ROOM_GB_1;
                roomTwo = EaseConstants.ROOM_GB_2;
                spAILevelKey = SPKeys.AI_LEVEL_GB;
                spRoomLevelKey = SPKeys.ROOM_LEVEL_GB;
                aiNum = 7;
                break;
            case GameConstants.GAME_RV:
                firstColor = context.getString(R.string.black) + " × ";
                lastColor = context.getString(R.string.white) + " × ";
                roomOne = EaseConstants.ROOM_RV_1;
                roomTwo = EaseConstants.ROOM_RV_2;
                spAILevelKey = SPKeys.AI_LEVEL_RV;
                spRoomLevelKey = SPKeys.ROOM_LEVEL_RV;
                aiNum = 6;
                break;
        }
    }

    public int getAiNum() {
        return aiNum;
    }

    public int getGameFlag() {
        return gameFlag;
    }

    public String getFirstColor() {
        return firstColor;
    }

    public String getLastColor() {
        return lastColor;
    }

    public String getRoomOne() {
        return roomOne;
    }

    public String getRoomTwo() {
        return roomTwo;
    }

    public String getSpAILevelKey() {
        return spAILevelKey;
    }

    public String getSpRoomLevelKey() {
        return spRoomLevelKey;
    }
}