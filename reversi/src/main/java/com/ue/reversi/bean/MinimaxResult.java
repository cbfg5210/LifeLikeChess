package com.ue.reversi.bean;


import android.graphics.Point;

/**
 * 记录极小极大算法过程中的数据
 */
public class MinimaxResult {

	public int mark;
	
	public Point move;
	
	public MinimaxResult(int mark, Point move) {
		this.mark = mark;
		this.move = move;
	}
	
}
