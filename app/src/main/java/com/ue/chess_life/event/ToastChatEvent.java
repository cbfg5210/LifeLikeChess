package com.ue.chess_life.event;

/**
 * Created by hawk on 2017/6/18.
 */

public class ToastChatEvent {
    public boolean isSent;
    public String txt;

    public ToastChatEvent(boolean isSent, String txt) {
        this.isSent = isSent;
        this.txt = txt;
    }
}
