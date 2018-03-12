package com.ue.gobang.util;

/**
 * State类的zobristKeys数组和BLACK、WHITE值有关联，
 * 为了避免这里的BLACK、WHITE值改变造成影响引入GB_DIFF值:
 * DIFF=BLACK>WHITE?WHITE:BLACK 且 abs(BLACK-WHITE)=1
 */
public interface Roles {
    int DIFF = 2;
    int EMPTY = 0;//如果不为0的话第一步的结果不同，后续需要再研究修改
    int BLACK = 2;
    int WHITE = 3;
    int ERROR = 4;
    //not terminal
    int ING = 5;
    int LEN = 15;
}
