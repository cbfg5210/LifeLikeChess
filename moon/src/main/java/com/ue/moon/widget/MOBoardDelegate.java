package com.ue.moon.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.google.gson.reflect.TypeToken;
import com.ue.moon.R;
import com.ue.moon.entity.ChessRecord;
import com.ue.moon.entity.MoonPoint;
import com.ue.moon.entity.Roles;
import com.ue.moon.util.MoonChessUtil;
import com.ue.resource.GameBoardDelegate;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.constant.GameResults;
import com.ue.resource.event.PlayListener;
import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hawk on 2016/12/13.
 */

public class MOBoardDelegate extends GameBoardDelegate {
    private Paint mPaint;
    private float halfPanelWidth;
    private float mSquareWidth;//棋盘格子宽高
    private float halfSquareWidth;
    private float width_15;
    private float width_11;
    private float width_41;
    private float width_13;
    private float width_1;
    private float width_3;
    private float width_6;
    private float width_5;
    private float width_9;
    private int chessWidth;
    private float halfChessWidth;

    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;
    private Bitmap background;

    private Bitmap myBitmap;
    private Bitmap oppoBitmap;
    private int myColor;
    private int oppoColor;

    private MoonPoint[] mMoonPoints = new MoonPoint[21];

    private MoonPoint selectedPoint;
    private ChessRecord lastMove;

    private boolean isOnlineMode;

    public void init(Context context, int gameFlag, int mGameMode, PlayListener playListener) {
        super.init(context, gameFlag, mGameMode, playListener);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5f);

        mWhitePiece = BitmapFactory.decodeResource(context.getResources(), R.mipmap.mc_white);
        mBlackPiece = BitmapFactory.decodeResource(context.getResources(), R.mipmap.mc_black);
        background = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg_game);

        myBitmap = mBlackPiece;
        oppoBitmap = mWhitePiece;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        boardWidth = displayMetrics.widthPixels * 0.95f;

        mSquareWidth = boardWidth / 7;

        halfPanelWidth = boardWidth * 0.5f;
        halfSquareWidth = mSquareWidth * 0.5f;

        width_1 = boardWidth * 1f / 14;
        width_3 = boardWidth * 3f / 14;
        width_5 = boardWidth * 5f / 14;
        width_6 = boardWidth * 6f / 7;
        width_9 = boardWidth * 9f / 14;
        width_11 = boardWidth * 11f / 14;
        width_13 = boardWidth * 13f / 14;
        width_15 = boardWidth * 15f / 56;
        width_41 = boardWidth * 41f / 56;

        chessWidth = (int) (mSquareWidth * 0.75f);
        halfChessWidth = mSquareWidth * 0.375f;
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, chessWidth, chessWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, chessWidth, chessWidth, false);
        background = Bitmap.createScaledBitmap(background, (int) boardWidth + 1, (int) boardWidth + 1, false);

        initMoonPoints();
        isOnlineMode = (mGameMode == GameConstants.MODE_INVITE || mGameMode == GameConstants.MODE_ONLINE);
    }

    @Override
    public int[] getAiMove() {
        return null;
    }

    public boolean isUndoable() {
        return lastMove != null;
    }

    private void checkIsGameOver() {
        int myChessCount = 0, oppoChessCount = 0;
        for (int i = 0, len = mMoonPoints.length; i < len; i++) {
            if (mMoonPoints[i].c == Roles.EMPTY) {
                continue;
            }
            if (mMoonPoints[i].c == myColor) {
                myChessCount += 1;
            } else {
                oppoChessCount += 1;
            }
            if (myChessCount > 2 && oppoChessCount > 2) {
                break;
            }
        }
        if (myChessCount < 3) {
            gameRecordManager.clearGameRecords();
            mPlayListener.onGameOver(GameResults.OPPO_WON);
        } else if (oppoChessCount < 3) {
            gameRecordManager.clearGameRecords();
            mPlayListener.onGameOver(GameResults.I_WON);
        }
    }

    public boolean undoOnce(boolean isMyTurn) {
        if (lastMove == null) {
            return isMyTurn;
        }
        mMoonPoints[lastMove.fromF].c = lastMove.fromC;
        mMoonPoints[lastMove.toF].c = Roles.EMPTY;

        List<Integer> killedFs = lastMove.killedFs;
        if (killedFs != null && killedFs.size() > 0) {
            int killedColor = lastMove.fromC == myColor ? oppoColor : myColor;
            for (int i = 0, len = killedFs.size(); i < len; i++) {
                mMoonPoints[killedFs.get(i)].c = killedColor;
            }
        }

        Object lastRecord = gameRecordManager.delToGetLastRecord(lastMove);
        lastMove = lastRecord == null ? null : (ChessRecord) lastRecord;

        possiblyKilledPoints.clear();
        selectedPoint = null;

        return !isMyTurn;
    }

    @Override
    public void initChessBoard(boolean isIFirst) {
        if (isIFirst) {
            myColor = Roles.BLACK;
            oppoColor = Roles.WHITE;
            myBitmap = mBlackPiece;
            oppoBitmap = mWhitePiece;
        } else {
            myColor = Roles.WHITE;
            oppoColor = Roles.BLACK;
            myBitmap = mWhitePiece;
            oppoBitmap = mBlackPiece;
        }

        mMoonPoints[0].c = oppoColor;
        mMoonPoints[1].c = oppoColor;
        mMoonPoints[2].c = oppoColor;
        mMoonPoints[3].c = oppoColor;
        mMoonPoints[5].c = oppoColor;
        mMoonPoints[13].c = oppoColor;

        mMoonPoints[7].c = myColor;
        mMoonPoints[8].c = myColor;
        mMoonPoints[9].c = myColor;
        mMoonPoints[10].c = myColor;
        mMoonPoints[11].c = myColor;
        mMoonPoints[15].c = myColor;

        mMoonPoints[4].c = Roles.EMPTY;
        mMoonPoints[6].c = Roles.EMPTY;
        mMoonPoints[12].c = Roles.EMPTY;
        mMoonPoints[14].c = Roles.EMPTY;
        mMoonPoints[16].c = Roles.EMPTY;
        mMoonPoints[17].c = Roles.EMPTY;
        mMoonPoints[18].c = Roles.EMPTY;
        mMoonPoints[19].c = Roles.EMPTY;
        mMoonPoints[20].c = Roles.EMPTY;

        possiblyKilledPoints.clear();
        lastMove = null;
        selectedPoint = null;
    }

    @Override
    public void startGame(boolean isIFirst) {
        gameRecordManager.clearGameRecords();
        initChessBoard(isIFirst);
    }

    @Override
    public void playChess(boolean isMyTurn, int[] oData) {
        int[] data;
        if (isMyTurn || !isOnlineMode) {
            data = oData;
        } else {
            //在线模式需要对对方的数据进行转换
            data = new int[2];
            data[0] = MoonChessUtil.getOppositePointFlag(oData[0]);
            data[1] = MoonChessUtil.getOppositePointFlag(oData[1]);
            //
//            KLog.e("aPlayChess,oData0="+oData[0]+",oData1="+oData[1]+",data0="+data[0]+",data1="+data[1]);
        }

        int fromColor = mMoonPoints[data[0]].c;

        //移动
        mMoonPoints[data[1]].c = mMoonPoints[data[0]].c;
        mMoonPoints[data[0]].c = Roles.EMPTY;

        selectedPoint = null;

        checkHasChessKilled();

        List<Integer> killedFs = new ArrayList<>();
        if (possiblyKilledPoints.size() > 0) {
            for (MoonPoint moonPoint : possiblyKilledPoints) {
                killedFs.add(moonPoint.f);
            }
        }
        saveMoveToRecord(data[0], fromColor, data[1], killedFs);

        checkIsGameOver();

        mPlayListener.onPlayed(isMyTurn, data, !isMyTurn);
    }

    @Override
    public void enterGame() {
        gameRecordManager.getChessRecords(ChessRecord.class)
                .subscribe(gameRecords -> {
                    if (mContext == null || ((Activity) mContext).isFinishing()) {
                        return;
                    }
                    boolean noHisRecords = gameRecords.size() == 0;

                    boolean isIRunFirst = noHisRecords ? true : (((ChessRecord) gameRecords.get(0)).fromF > 6);
                    boolean isMyTurn = isIRunFirst;

                    initChessBoard(isIRunFirst);

                    if (!noHisRecords) {
                        ChessRecord move;
                        for (IChessRecord chessRecord : gameRecords) {
                            move = (ChessRecord) chessRecord;
                            mMoonPoints[move.toF].c = move.fromC;
                            mMoonPoints[move.fromF].c = Roles.EMPTY;

                            List<Integer> killedFs = move.killedFs;
                            if (killedFs != null && killedFs.size() > 0) {
                                for (int i = 0, len = killedFs.size(); i < len; i++) {
                                    mMoonPoints[killedFs.get(i)].c = Roles.EMPTY;
                                }
                            }
                            isMyTurn = !isMyTurn;
                        }
                        lastMove = (ChessRecord) gameRecords.get(gameRecords.size() - 1);
                    }

                    mPlayListener.onGameDataReset(isIRunFirst, isMyTurn);
                });
    }

    private void initMoonPoints() {
        mMoonPoints[0] = new MoonPoint(width_15, mSquareWidth, MoonChessUtil.POINT_0, oppoColor);
        mMoonPoints[1] = new MoonPoint(halfPanelWidth, halfSquareWidth, MoonChessUtil.POINT_1, oppoColor);
        mMoonPoints[2] = new MoonPoint(width_41, mSquareWidth, MoonChessUtil.POINT_2, oppoColor);
        mMoonPoints[3] = new MoonPoint(halfPanelWidth, width_3, MoonChessUtil.POINT_3, oppoColor);
        mMoonPoints[5] = new MoonPoint(width_6, width_15, MoonChessUtil.POINT_5, oppoColor);
        mMoonPoints[13] = new MoonPoint(mSquareWidth, width_15, MoonChessUtil.POINT_13, oppoColor);

        mMoonPoints[7] = new MoonPoint(width_6, width_41, MoonChessUtil.POINT_7, myColor);
        mMoonPoints[8] = new MoonPoint(width_15, width_6, MoonChessUtil.POINT_8, myColor);
        mMoonPoints[9] = new MoonPoint(halfPanelWidth, width_11, MoonChessUtil.POINT_9, myColor);
        mMoonPoints[10] = new MoonPoint(width_41, width_6, MoonChessUtil.POINT_10, myColor);
        mMoonPoints[11] = new MoonPoint(halfPanelWidth, width_13, MoonChessUtil.POINT_11, myColor);
        mMoonPoints[15] = new MoonPoint(mSquareWidth, width_41, MoonChessUtil.POINT_15, myColor);

        mMoonPoints[4] = new MoonPoint(width_11, halfPanelWidth, MoonChessUtil.POINT_4, Roles.EMPTY);
        mMoonPoints[6] = new MoonPoint(width_13, halfPanelWidth, MoonChessUtil.POINT_6, Roles.EMPTY);
        mMoonPoints[12] = new MoonPoint(halfSquareWidth, halfPanelWidth, MoonChessUtil.POINT_12, Roles.EMPTY);
        mMoonPoints[14] = new MoonPoint(width_3, halfPanelWidth, MoonChessUtil.POINT_14, Roles.EMPTY);
        mMoonPoints[16] = new MoonPoint(width_5, halfPanelWidth, MoonChessUtil.POINT_16, Roles.EMPTY);
        mMoonPoints[17] = new MoonPoint(halfPanelWidth, width_5, MoonChessUtil.POINT_17, Roles.EMPTY);
        mMoonPoints[18] = new MoonPoint(width_9, halfPanelWidth, MoonChessUtil.POINT_18, Roles.EMPTY);
        mMoonPoints[19] = new MoonPoint(halfPanelWidth, width_9, MoonChessUtil.POINT_19, Roles.EMPTY);
        mMoonPoints[20] = new MoonPoint(halfPanelWidth, halfPanelWidth, MoonChessUtil.POINT_20, Roles.EMPTY);
    }

    public void drawPieces(Canvas canvas) {
        MoonPoint tempPoint;
        for (int i = 0, len = mMoonPoints.length; i < len; i++) {
            tempPoint = mMoonPoints[i];
            if (tempPoint.c == myColor) {
                canvas.drawBitmap(myBitmap, tempPoint.x - halfChessWidth, tempPoint.y - halfChessWidth, null);
            } else if (tempPoint.c == oppoColor) {
                canvas.drawBitmap(oppoBitmap, tempPoint.x - halfChessWidth, tempPoint.y - halfChessWidth, null);
            }
        }

        if (selectedPoint != null) {
            mPaint.setColor(Color.parseColor("#66cc99"));
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(selectedPoint.x, selectedPoint.y, 0.5f * halfChessWidth, mPaint);
        }

        if (lastMove != null) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.parseColor("#cd0b17"));
            canvas.drawCircle(mMoonPoints[lastMove.fromF].x, mMoonPoints[lastMove.fromF].y, halfChessWidth, mPaint);
            mPaint.setColor(Color.parseColor("#0066ff"));
            canvas.drawCircle(mMoonPoints[lastMove.toF].x, mMoonPoints[lastMove.toF].y, halfChessWidth, mPaint);
        }
        //死亡的棋子
        if (possiblyKilledPoints.size() > 0) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("#9b73e6"));
            for (MoonPoint deadPoint : possiblyKilledPoints) {
                canvas.drawCircle(deadPoint.x, deadPoint.y, halfChessWidth, mPaint);
            }
        }
    }

    public void drawBoard(Canvas canvas) {
        canvas.drawBitmap(background, 0, 0, null);

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);//描边

        canvas.drawLine(halfSquareWidth, halfPanelWidth, width_13, halfPanelWidth, mPaint);//横
        canvas.drawLine(halfPanelWidth, halfSquareWidth, halfPanelWidth, width_13, mPaint);//竖

        canvas.drawCircle(halfPanelWidth, halfPanelWidth, 3f / 7 * boardWidth, mPaint);//外圆
        canvas.drawCircle(halfPanelWidth, halfPanelWidth, mSquareWidth, mPaint);//内圆

        RectF rectF = new RectF(width_15, width_11, width_41, width_13);//下
        canvas.drawArc(rectF, 180, 180, false, mPaint);

        rectF = new RectF(width_15, width_1, width_41, width_3);//上
        canvas.drawArc(rectF, 0, 180, false, mPaint);

        rectF = new RectF(width_1, width_15, width_3, width_41);//左
        canvas.drawArc(rectF, -90, 180, false, mPaint);

        rectF = new RectF(width_11, width_15, width_13, width_41);//右
        canvas.drawArc(rectF, 90, 180, false, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, boolean isMyTurn) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            MoonPoint theMoonPoint = MoonChessUtil.getMoonPoint(mMoonPoints, event.getX(), event.getY(), halfChessWidth);
            if (theMoonPoint == null) {
                return true;
            }
            //如果点击的是自己的棋子，则标记为选中
            if ((isMyTurn && (theMoonPoint.c == myColor)) || (!isMyTurn && (theMoonPoint.c == oppoColor))) {
                selectedPoint = theMoonPoint;
            } else if (theMoonPoint.c == Roles.EMPTY) {//点击的是空白处
                if (selectedPoint != null) {
                    MoonPoint[] aroundPoints = MoonChessUtil.getAroundPoints(mMoonPoints, selectedPoint.f);
                    MoonPoint tempPoint;
                    for (int i = 0, len = aroundPoints.length; i < len; i++) {
                        tempPoint = aroundPoints[i];
                        if (MoonChessUtil.isEqualPoints(theMoonPoint, tempPoint, halfChessWidth)) {
                            playChess(isMyTurn, new int[]{selectedPoint.f, theMoonPoint.f});
                            break;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void saveMoveToRecord(int fromFlag, int fromColor, int toFlag, List<Integer> killedFlags) {
        lastMove = new ChessRecord();
        lastMove.fromF = fromFlag;
        lastMove.fromC = fromColor;
        lastMove.toF = toFlag;
        lastMove.killedFs = killedFlags;

        gameRecordManager.saveChessRecord(lastMove);
    }

    private void checkHasChessKilled() {
        possiblyKilledPoints.clear();
        for (int i = 0, len = mMoonPoints.length; i < len; i++) {
            if (mMoonPoints[i].c == Roles.EMPTY) {
                continue;
            }
            //这里要对检查过了的棋子进行过滤，避免内存溢出
            if (checkedPoints.contains(mMoonPoints[i])) {
                continue;
            }
//            KLog.e( "checkHasChessKilled,flag=" + mMoonPoints[i].f);
            checkThisChessIsKilled(mMoonPoints[i]);
            //如果列表中有棋子，表示这些棋子要死亡
            if (possiblyKilledPoints.size() > 0) {
                for (MoonPoint moonPoint : possiblyKilledPoints) {
                    mMoonPoints[moonPoint.f].c = Roles.EMPTY;
                }
                //检查到棋子死亡就停止检查
                break;
            }
        }
        checkedPoints.clear();
    }

    private List<MoonPoint> checkedPoints = new ArrayList<>();//检查过了的棋子
    //记录检查过的棋子以及其是否周边有空位，最后如果都没有空位的话，这个列表的点都会去掉
    private List<MoonPoint> possiblyKilledPoints = new ArrayList<>();

//    private int iteratorTime = 0;

    //关键：判断是否形成包围
    private void checkThisChessIsKilled(MoonPoint targetPoint) {
//        KLog.e( "*******************************checkThisChessIsKilled start*****************" + iteratorTime++);
//        KLog.e("target point flag="+targetPoint.f);
        checkedPoints.add(targetPoint);//将该点加入已检查列表
        MoonPoint[] aroundPoints = MoonChessUtil.getAroundPoints(mMoonPoints, targetPoint.f);
        //第一次判断:棋子周边是否有空位，如果有，不死
        boolean hasNullAround = false;
        for (int i = 0, len = aroundPoints.length; i < len; i++) {
            if (aroundPoints[i].c == Roles.EMPTY) {
                hasNullAround = true;
                break;
            }
        }
//        KLog.e( "checkThisChessIsKilled,hasNullAround=" + hasNullAround);
        //检查到棋子周边有空位的话，对于单棋子的情况该棋子不死亡，对于多棋子的情况，列表中的棋子都不死亡
        if (hasNullAround) {
            possiblyKilledPoints.clear();
            return;
        }
        //第一次判断结果是棋子周边没有空位的话，进行第二次判断
        //判断周围是否有自己的棋子，如果没有，该棋子死亡;这种判断是针对单棋子的，即该棋子周围都唯有自己的棋子，
        //  如果是有两个或多个棋子相连的话，这里的判断跳过
        boolean hasOwnChessAround = false;
        for (int i = 0, len = aroundPoints.length; i < len; i++) {
            if (aroundPoints[i].c == targetPoint.c) {
                hasOwnChessAround = true;
                break;
            }
        }
//        KLog.e( "checkThisChessIsKilled,hasOwnChessAround=" + hasOwnChessAround);
        if (!hasOwnChessAround) {
            possiblyKilledPoints.add(targetPoint);
//            mMoonPoints[targetPoint.f].c = Roles.EMPTY;
            //检查到棋子死亡则停止检查，这种情况属于单棋子的情况；多棋子的话是在checkHasPointKilled中判断的
            return;
        }

        //第二次判断结果是棋子周围有自己的棋子的话，进行第三次判断
        //迭代判断连接的己方棋子[周边的棋子的周边]是否有空位，没有的话放进列表中，有的话列表被清空，最后判断列表是否有
        //  棋子，有的话，这些棋子都死亡了
        possiblyKilledPoints.add(targetPoint);
        for (int i = 0, len = aroundPoints.length; i < len; i++) {
            if (targetPoint.c != aroundPoints[i].c) {//这颗棋子不是己方的，跳过
                continue;
            }
            //
//            KLog.e( "checkThisChessIsKilled,flag=" + aroundPoints[i].f + ",checkedPoints contain=" + checkedPoints.contains(aroundPoints[i]) + ";possiblyKilledPoints contain=" + possiblyKilledPoints.contains(aroundPoints[i]));
            if (checkedPoints.contains(aroundPoints[i])) {
                //如果检查过了且死亡列表没有该点，说明死亡列表上的棋子死不了，可以停止该for循环
                if (!possiblyKilledPoints.contains(aroundPoints[i])) {
                    possiblyKilledPoints.clear();
                    break;
                }
                //如果检查过了且死亡列表有该点，跳到下一个继续判断
                continue;//!!!!!!如果去掉这个会死循环，checkHasChessKilled()死循环，target point flag的值不断在0和1切换
                //死循环描述：
                //检查POINT0，没有空点，有己方，将POINT0加入已查列表和死亡列表；检查POINT1确认POINT0是否死亡，POINT1，没有空点，有己方，将POINT0加入已查列表和死亡列表；检查POINT0确认POINT1是否死亡....
            }
            checkThisChessIsKilled(aroundPoints[i]);
//            KLog.e( "checkThisChessIsKilled,*******size=" + possiblyKilledPoints.size());
            //如果possiblyKilledPoints没有数据，说明检查到了空点，数据才被清除了，所以可以停止本轮判断，不必要进行多余的判断了
            if (possiblyKilledPoints.size() == 0) {
                break;
            }
        }
//        KLog.e( "*******************************checkThisChessIsKilled end*****************" + (--iteratorTime));
    }

    private static final String MY_COLOR = "myColor";
    private static final String OPPO_COLOR = "oppoColor";
    private static final String LAST_MOVE = "lastMove";
    private static final String SELECTED_POINT = "selectedPoint";
    private static final String POINTS = "mMoonPoints";

    @Override
    public Parcelable onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putInt(MY_COLOR, myColor);
        bundle.putInt(OPPO_COLOR, oppoColor);
        if (selectedPoint != null) {
            bundle.putString(SELECTED_POINT, selectedPoint.toString());
        }
        if (lastMove != null) {
            bundle.putString(LAST_MOVE, lastMove.toString());
        }
        bundle.putString(POINTS, mMoonPoints.toString());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        myColor = bundle.getInt(MY_COLOR);
        oppoColor = bundle.getInt(OPPO_COLOR);
        if (myColor == Roles.BLACK) {
            myBitmap = mBlackPiece;
            oppoBitmap = mWhitePiece;
        } else {
            myBitmap = mWhitePiece;
            oppoBitmap = mBlackPiece;
        }

        if (bundle.containsKey(SELECTED_POINT)) {
            String selectedPointJson = bundle.getString(SELECTED_POINT);
            selectedPoint = GsonHolder.getGson().fromJson(selectedPointJson, MoonPoint.class);
        }

        if (bundle.containsKey(LAST_MOVE)) {
            String lastChessMoveJson = bundle.getString(LAST_MOVE);
            lastMove = GsonHolder.getGson().fromJson(lastChessMoveJson, ChessRecord.class);
        }

        String moonPointsJson = bundle.getString(POINTS);
        mMoonPoints = GsonHolder.getGson().fromJson(moonPointsJson, new TypeToken<MoonPoint[]>() {
        }.getType());
    }
}
