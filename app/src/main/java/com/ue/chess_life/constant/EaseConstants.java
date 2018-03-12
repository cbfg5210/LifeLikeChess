/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ue.chess_life.constant;

public interface EaseConstants {
    String ACCOUNT_REMOVED = "account_removed";
    String ACCOUNT_CONFLICT = "conflict";
    String ACCOUNT_FORBIDDEN = "user_forbidden";
    String IS_CONFLICT = "isConflict";

    String ROOM_RV_1 = "272866786033009172";
    String ROOM_RV_2 = "272866788088218128";
    String ROOM_GB_1 = "272866446625735180";
    String ROOM_GB_2 = "272866448634806796";
    String ROOM_CC_1 = "272866652847079948";
    String ROOM_CC_2 = "272866654885511704";
    String ROOM_IC_1 = "272866872498586124";
    String ROOM_IC_2 = "272866874591543824";
    String ROOM_MO_1 = "275814435086402060";
    String ROOM_MO_2 = "275814437145805336";

    String GROUP_ID_REVERSI = "272427598913471000";
    String GROUP_ID_GOBANG = "272428707325411864";
    String GROUP_ID_CNCHESS = "272428800984220188";
    String GROUP_ID_CHESS = "272428886896149020";
    String GROUP_ID_MOON = "276138250085597712";
    String GROUP_ID_ARMY = "281386891289297420";

    String OPPO_USER_NAME = "OPPO_USER_NAME";
    String WHICH_GAME = "WHICH_GAME";

    int ROOM_PRIMARY = 0;
    int ROOM_HIGH = 1;

    int CODE_REFUSE = 0;
    int CODE_ACCEPT = 1;

    String ATTR_GAME_CHAT = "ATTR_GAME_CHAT";
    String ATTR_CODE = "ATTR_CODE";

    String ARG_GAME_FLAG = "arg_game_flag";
    String ARG_GAME_MODE = "arg_game_mode";

    int TIME_OUT_INVITE = 20;
    //请求收发时差，单位：秒，不同设备就有时差，这里设为0，不然接收方回复时间很短
//    int TIME_GAP_REQ_REP = 0;

    String GAME_DATA = "gameData";
    String FROM_PLAYER = "fromPlayer";
}