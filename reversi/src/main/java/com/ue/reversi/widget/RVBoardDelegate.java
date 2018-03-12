package com.ue.reversi.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.google.gson.reflect.TypeToken;
import com.ue.resource.GameBoardDelegate;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.constant.GameResults;
import com.ue.resource.event.PlayListener;
import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;
import com.ue.reversi.R;
import com.ue.reversi.bean.ChessRecord;
import com.ue.reversi.bean.ReversiRecordItem;
import com.ue.reversi.bean.Roles;
import com.ue.reversi.bean.Statistic;
import com.ue.reversi.util.AiPlayer;
import com.ue.reversi.util.Rule;
import com.ue.reversi.util.Util;

import java.util.ArrayList;
import java.util.List;

import static com.ue.resource.constant.GameConstants.MODE_SINGLE;

/**
 * 棋盘界面
 */
public class RVBoardDelegate extends GameBoardDelegate {
    private Paint mPaint;
    private float squareWidth;//棋格边长

    private byte[][] qzs;
    private int[][] qzsIndex;

    private Bitmap[] images;
    private Bitmap background;
    private byte myColor;
    private byte oppoColor;

    private ChessRecord lastMove;

    protected Thread renderThread;
    protected SurfaceHolder renderSHolder;
    protected boolean isToRender;//是否继续渲染

    private AiPlayer mAiPlayer;

    private SurfaceHolder.Callback surfaceCallback;

    public boolean isUndoable() {
        return lastMove != null;
    }

    /**
     * 返回上一步
     * <p>
     * 有时候是连下的，这种情况也要考虑进去
     */
    public boolean undoOnce(boolean isMyTurn) {
        if (lastMove == null) {
            return isMyTurn;
        }
        //连下的情况，连撤两次
        boolean backTwice = (isMyTurn && lastMove.c == myColor) || (!isMyTurn && lastMove.c == oppoColor);

        List<Point> killedPoints = new ArrayList<>();
        for (ReversiRecordItem reversiRecordItem : lastMove.mRecordItems) {
            qzs[reversiRecordItem.x][reversiRecordItem.y] = Roles.EMPTY;
            killedPoints.addAll(reversiRecordItem.changedChess);
        }
        undoList(killedPoints);

        Object lastRecord = gameRecordManager.delToGetLastRecord(lastMove);
        lastMove = lastRecord == null ? null : (ChessRecord) lastRecord;

        if (backTwice) {
            return undoOnce(isMyTurn);
        }

        return !isMyTurn;
    }

    @Override
    public int[] getScores() {
        Statistic statistic = Rule.analyse(qzs, myColor);
        return new int[]{statistic.PLAYER, statistic.AI};
    }

    private Runnable renderRunnable = new Runnable() {
        @Override
        public void run() {
            Canvas canvas = null;
            while (isToRender) {
                long startTime = System.currentTimeMillis();
                update();
                long endTime = System.currentTimeMillis();

                try {
                    canvas = renderSHolder.lockCanvas();
                    synchronized (renderSHolder) {
                        drawBoard(canvas);
                        drawPieces(canvas);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        renderSHolder.unlockCanvasAndPost(canvas);
                    }
                }
                try {
                    if ((endTime - startTime) <= 100) {
                        Thread.sleep(100 - (endTime - startTime));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private int updateIndex(int index, int color) {
        if (index == 0 || index == 11) {
            return index;
        } else if (index >= 1 && index <= 10 || index >= 12 && index <= 21) {
            return (index + 1) % 22;
        } else {
            return color == Roles.WHITE ? 11 : (color == Roles.BLACK ? 0 : -1);
        }
    }

    public void init(Context context, int gameFlag, int mGameMode, PlayListener playListener) {
        super.init(context, gameFlag, mGameMode, playListener);

        mPaint = new Paint();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        boardWidth = displayMetrics.widthPixels * 0.9f;

        squareWidth = boardWidth / 9;
        images = new Bitmap[22];
        loadChesses(context);

        background = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg_game);
        background = Bitmap.createScaledBitmap(background, (int) boardWidth + 1, (int) boardWidth + 1, false);
        initChessBoard(false);
    }

    @Override
    public int[] getAiMove() {
        int legalMoves = Rule.getLegalMoves(qzs, oppoColor).size();
        if (legalMoves <= 0) {
            return null;
        }
        Point move = mAiPlayer.getGoodMove(qzs, oppoColor);
        return new int[]{move.x, move.y};
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, boolean isMyTurn) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) (event.getY() / squareWidth - 0.5f);
            int y = (int) (event.getX() / squareWidth - 0.5f);

            if (x > 7 || y > 7) {
                return true;
            }

            playChess(isMyTurn, new int[]{x, y});
        }
        return true;
    }

    /**
     * 保存行棋记录
     *
     * @param x
     * @param y
     * @param color
     * @param moves
     */
    private void saveReversiRecord(int x, int y, byte color, List<Point> moves, boolean isMyTurn) {
        if (lastMove == null || lastMove.c != color) {
            lastMove = new ChessRecord(color, isMyTurn);
        }
        lastMove.addRecordItem(new ReversiRecordItem(x, y, moves));

        gameRecordManager.saveChessRecord(lastMove);
    }

    private void undoList(List<Point> changedChess) {
        for (Point p : changedChess) {
            if (lastMove.c == Roles.BLACK) {
                qzs[p.x][p.y] = Roles.WHITE;
                qzsIndex[p.x][p.y] = 11;
            } else {
                qzs[p.x][p.y] = Roles.BLACK;
                qzsIndex[p.x][p.y] = 0;
            }
        }
    }

    private void move(byte[][] chessBoard, List<Point> reversed, Point move) {
        Util.copyBinaryArray(chessBoard, this.qzs);
        int reversedSize = reversed.size();
        for (int i = 0; i < reversedSize; i++) {
            int reverseRow = reversed.get(i).x;
            int reverseCol = reversed.get(i).y;
            if (chessBoard[reverseRow][reverseCol] == Roles.WHITE) {
                qzsIndex[reverseRow][reverseCol] = 1;
            } else if (chessBoard[reverseRow][reverseCol] == Roles.BLACK) {
                qzsIndex[reverseRow][reverseCol] = 12;
            }
        }
        int row = move.x, col = move.y;
        if (chessBoard[row][col] == Roles.WHITE) {
            qzsIndex[row][col] = 11;
        } else if (chessBoard[row][col] == Roles.BLACK) {
            qzsIndex[row][col] = 0;
        }
    }

    private void update() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (qzs[i][j] != Roles.EMPTY) {
                    qzsIndex[i][j] = updateIndex(qzsIndex[i][j], qzs[i][j]);
                }
            }
        }
    }

    public void drawBoard(Canvas canvas) {
        canvas.drawBitmap(background, 0, 0, mPaint);

        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(3);
        for (int i = 0; i < 9; i++) {
            canvas.drawLine(0.5f * squareWidth, (i + 0.5f) * squareWidth, boardWidth - 0.5f * squareWidth, (i + 0.5f) * squareWidth, mPaint);
            canvas.drawLine((i + 0.5f) * squareWidth, 0.5f * squareWidth, (i + 0.5f) * squareWidth, boardWidth - 0.5f * squareWidth, mPaint);
        }
    }

    public void drawPieces(Canvas canvas) {
        for (int col = 0; col < Roles.LEN; col++) {
            for (int row = 0; row < Roles.LEN; row++) {
                if (qzs[row][col] != Roles.EMPTY) {
                    canvas.drawBitmap(images[qzsIndex[row][col]], (col + 0.5f) * squareWidth, (row + 0.5f) * squareWidth, mPaint);
                }
            }
        }

        if (lastMove != null) {
            //最后下的一只棋要特殊标注
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.BLUE);

            ReversiRecordItem lastItem = lastMove.mRecordItems.get(lastMove.mRecordItems.size() - 1);
            canvas.drawCircle((lastItem.y + 1) * squareWidth, (lastItem.x + 1) * squareWidth, 3 * squareWidth / 16, mPaint);
        }
    }

    private Bitmap loadBitmap(float width, float height, Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, (int) width, (int) height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 加载棋子图片
     */
    private void loadChesses(Context context) {
        images[0] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black1));
        images[1] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black2));
        images[2] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black3));
        images[3] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black4));
        images[4] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black5));
        images[5] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black6));
        images[6] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black7));
        images[7] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black8));
        images[8] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black9));
        images[9] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black10));
        images[10] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_black11));
        images[11] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white1));
        images[12] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white2));
        images[13] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white3));
        images[14] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white4));
        images[15] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white5));
        images[16] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white6));
        images[17] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white7));
        images[18] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white8));
        images[19] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white9));
        images[20] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white10));
        images[21] = loadBitmap(squareWidth, squareWidth, context.getResources().getDrawable(R.mipmap.rv_white11));
    }

    private static final String MY_COLOR = "myColor";
    private static final String OPPO_COLOR = "oppoColor";
    private static final String QZS = "qzs";
    private static final String QZS_INDEX = "qzsIndex";
    private static final String LAST_MOVE = "lastMove";

    @Override
    public Parcelable onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putByte(MY_COLOR, myColor);
        bundle.putByte(OPPO_COLOR, oppoColor);
        if (QZS != null) {
            bundle.putString(QZS, GsonHolder.getGson().toJson(qzs));
        }
        if (qzsIndex != null) {
            bundle.putString(QZS_INDEX, GsonHolder.getGson().toJson(qzsIndex));
        }
        if (lastMove != null) {
            bundle.putString(LAST_MOVE, lastMove.toString());
        }
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        myColor = bundle.getByte(MY_COLOR);
        oppoColor = bundle.getByte(OPPO_COLOR);
        if (bundle.containsKey(QZS)) {
            qzs = GsonHolder.getGson().fromJson(bundle.getString(QZS), new TypeToken<byte[][]>() {
            }.getType());
        }
        if (bundle.containsKey(QZS_INDEX)) {
            qzsIndex = GsonHolder.getGson().fromJson(bundle.getString(QZS_INDEX), new TypeToken<int[][]>() {
            }.getType());
        }
        if (bundle.containsKey(LAST_MOVE)) {
            lastMove = GsonHolder.getGson().fromJson(bundle.getString(LAST_MOVE), ChessRecord.class);
        }
    }

    @Override
    public SurfaceHolder.Callback getSurfaceCallback() {
        if (surfaceCallback == null) {
            surfaceCallback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    renderSHolder = holder;
                    renderThread = new Thread(renderRunnable);
                    isToRender = true;
                    renderThread.start();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    isToRender = false;
                    try {
                        renderThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                }
            };
        }
        return surfaceCallback;
    }

    public void playChess(boolean isMyTurn, int[] data) {
        byte ownerColor = isMyTurn ? myColor : oppoColor;
        int x = data[0];
        int y = data[1];
        if (!Rule.isLegalMove(qzs, new Point(x, y), ownerColor)) {
            return;
        }
        /**
         * 玩家走步
         */
        Point move = new Point(x, y);
        List<Point> moves = Rule.move(qzs, move, ownerColor);
        move(qzs, moves, move);

        //保存一个记录
        saveReversiRecord(x, y, ownerColor, moves, isMyTurn);

        boolean isIPlay = isMyTurn;
        isMyTurn = !isMyTurn;

        int legalMovesOfAI = Rule.getLegalMoves(qzs, oppoColor).size();
        int legalMovesOfPlayer = Rule.getLegalMoves(qzs, myColor).size();

        if (legalMovesOfAI == 0 && legalMovesOfPlayer > 0) {
            isMyTurn = true;
        } else if (legalMovesOfAI == 0 && legalMovesOfPlayer == 0) {
            gameRecordManager.clearGameRecords();

            Statistic statistic = Rule.analyse(qzs, myColor);
            int winOrLoseOrDraw = statistic.PLAYER - statistic.AI;
            int resultFlag = winOrLoseOrDraw == 0 ? GameResults.DRAW : (winOrLoseOrDraw > 0 ? GameResults.I_WON : GameResults.OPPO_WON);
            mPlayListener.onGameOver(resultFlag);
        } else if (legalMovesOfAI > 0 && legalMovesOfPlayer == 0) {
            isMyTurn = false;
        }

        mPlayListener.onPlayed(isIPlay, new int[]{x, y}, isMyTurn);
    }

    @Override
    public void enterGame() {
        gameRecordManager.getChessRecords(ChessRecord.class)
                .subscribe(gameRecords -> {
                    if (mContext == null || ((Activity) mContext).isFinishing()) {
                        return;
                    }
                    boolean noHisRecords = gameRecords.size() == 0;

                    boolean isIRunFirst = noHisRecords ? true : (((ChessRecord) gameRecords.get(0)).isMyTurn);
                    boolean isMyTurn = isIRunFirst;

                    initColor(isIRunFirst);
                    initChessBoard(isIRunFirst);

                    if (!noHisRecords) {
                        ChessRecord move;
                        List<Point> killedPoints = new ArrayList<>();
                        for (IChessRecord chessRecord : gameRecords) {
                            move = (ChessRecord) chessRecord;
                            killedPoints.clear();

                            for (ReversiRecordItem chessMove : move.mRecordItems) {
                                qzs[chessMove.x][chessMove.y] = move.c;
                                qzsIndex[chessMove.x][chessMove.y] = move.c == Roles.BLACK ? 0 : 11;
                                killedPoints.addAll(chessMove.changedChess);
                            }
                            for (Point p : killedPoints) {
                                qzs[p.x][p.y] = move.c;
                                qzsIndex[p.x][p.y] = move.c == Roles.BLACK ? 0 : 11;
                            }

                            isMyTurn = !isMyTurn;
                        }

                        if (Rule.getLegalMoves(qzs, isMyTurn ? myColor : oppoColor).size() == 0) {
                            //如果轮到某玩家，但是该玩家没有下棋的位置，那么轮回到另一玩家
                            isMyTurn = !isMyTurn;
                        }

                        lastMove = (ChessRecord) gameRecords.get(gameRecords.size() - 1);
                    }

                    mPlayListener.onGameDataReset(isIRunFirst, isMyTurn);
                    if (mGameMode == GameConstants.MODE_SINGLE) {
                        mAiPlayer = new AiPlayer();
                        mAiPlayer.setAiLevel(mAiLevel + 1);
                        mPlayListener.onPlayed(!isMyTurn, null, isMyTurn);
                    }
                });
    }

    private void initColor(boolean isIFirst) {
        if (isIFirst) {
            myColor = Roles.BLACK;
            oppoColor = Roles.WHITE;
        } else {
            oppoColor = Roles.BLACK;
            myColor = Roles.WHITE;
        }
    }

    public void startGame(boolean isIFirst) {
        gameRecordManager.clearGameRecords();
        initColor(isIFirst);
        initChessBoard(isIFirst);

        if (mGameMode == MODE_SINGLE) {
            mAiPlayer = mAiPlayer == null ? new AiPlayer() : mAiPlayer;
            mAiPlayer.setAiLevel(mAiLevel + 1);
        }
    }

    @Override
    public void initChessBoard(boolean isIFirst) {
        qzs = new byte[Roles.LEN][Roles.LEN];
        qzsIndex = new int[Roles.LEN][Roles.LEN];

        for (int i = 0; i < Roles.LEN; i++) {
            for (int j = 0; j < Roles.LEN; j++) {
                qzs[i][j] = Roles.EMPTY;
            }
        }
        qzs[3][3] = Roles.WHITE;
        qzs[3][4] = Roles.BLACK;
        qzs[4][3] = Roles.BLACK;
        qzs[4][4] = Roles.WHITE;

        qzsIndex[3][3] = 11;
        qzsIndex[3][4] = 0;
        qzsIndex[4][3] = 0;
        qzsIndex[4][4] = 11;

        lastMove = null;
    }
}
