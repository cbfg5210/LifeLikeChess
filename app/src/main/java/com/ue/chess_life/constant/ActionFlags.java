package com.ue.chess_life.constant;

/**
 * Created by hawk on 2017/10/30.
 */

public interface ActionFlags {
    //以后的更新当中，值不要改变
    int REQ_DRAW = 10;
    int REQ_UNDO = 11;
    int REP_DRAW = 12;
    int REP_UNDO = 13;
    int LEAVE = 14;
    int DATA = 16;
    int R_INVITE = 17;
    int INVITE = 19;
    int ACCEPT = 20;
    int REFUSE = 21;
    int ADOPT = 22;
    int READY = 23;
    int START = 24;
    int SURRENDER = 25;
}
