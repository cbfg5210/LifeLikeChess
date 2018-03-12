package com.ue.resource.event;

/**
 * for chess board view
 */
public interface PlayListener {

    void onPlayed(boolean isIPlay, int[] data, boolean isMyTurn);

    void onGameOver(int resultFlag);

    void onGameDataReset(boolean isIFirst, boolean isMyTurn);

    void onUndo(boolean isMyTurn);
}
