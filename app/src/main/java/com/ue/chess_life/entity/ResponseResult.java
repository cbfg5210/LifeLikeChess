package com.ue.chess_life.entity;

/**
 * Created by hawk on 2017/11/16.
 */

public class ResponseResult {
    public static final int CODE_SUCCESS = 100;
    public static final int CODE_FAILURE = 110;

    public int code;
    public String msg;

    public ResponseResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
