package com.ue.ichess.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.ue.ichess.R;
import com.ue.ichess.entity.ChessID;
import com.ue.ichess.entity.ChessPoint;
import com.ue.ichess.entity.ChessRecord;
import com.ue.ichess.entity.MoveGenerator;
import com.ue.ichess.util.ChessUtils;
import com.ue.resource.GameBoardDelegate;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.constant.GameResults;
import com.ue.resource.event.PlayListener;
import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

import static com.ue.ichess.entity.ChessID.B_BISHOP;
import static com.ue.ichess.entity.ChessID.B_KING;
import static com.ue.ichess.entity.ChessID.B_KING_N;
import static com.ue.ichess.entity.ChessID.B_KNIGHT;
import static com.ue.ichess.entity.ChessID.B_PAWN;
import static com.ue.ichess.entity.ChessID.B_PAWN_N;
import static com.ue.ichess.entity.ChessID.B_QUEEN;
import static com.ue.ichess.entity.ChessID.B_ROOK;
import static com.ue.ichess.entity.ChessID.B_ROOK_N;
import static com.ue.ichess.entity.ChessID.EMPTY;
import static com.ue.ichess.entity.ChessID.W_BISHOP;
import static com.ue.ichess.entity.ChessID.W_KING;
import static com.ue.ichess.entity.ChessID.W_KING_N;
import static com.ue.ichess.entity.ChessID.W_KNIGHT;
import static com.ue.ichess.entity.ChessID.W_PAWN;
import static com.ue.ichess.entity.ChessID.W_PAWN_N;
import static com.ue.ichess.entity.ChessID.W_QUEEN;
import static com.ue.ichess.entity.ChessID.W_ROOK;
import static com.ue.ichess.entity.ChessID.W_ROOK_N;

/**
 * Reference:
 * Author:
 * Date:2016/9/13.
 */
public class ICBoardDelegate extends GameBoardDelegate {
    private float mSquareWidth;//棋盘格子宽高
    private Paint mPaint;
    private float pieceWidth;

    private LruCache<Integer, Bitmap> mMemoryCache;
    private Bitmap piece;
    private int[][] qzs;
    private ChessRecord lastMove;
    private ChessPoint selectedChess;

    private MoveGenerator mMoveGenerator;
    private boolean isIRunFirst;
    private boolean isOnlineMode;
    private boolean isGameOver;

    public void addBitmapToMemoryCache(int key, Bitmap bitmap) {
        if (mMemoryCache.get(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void init(Context context, int gameFlag, int mGameMode, PlayListener playListener) {
        super.init(context, gameFlag, mGameMode, playListener);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);//抖动处理，平滑处理

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/16th of the available memory for this memory cache.
        int cacheSize = maxMemory / 16;
        mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        boardWidth = displayMetrics.widthPixels * 0.9f;

        mSquareWidth = boardWidth / 8;
        pieceWidth = mSquareWidth - 10;

        qzs = ChessUtils.getChessBoard(false, true);
        mMoveGenerator = new MoveGenerator();

        isOnlineMode = (mGameMode == GameConstants.MODE_INVITE || mGameMode == GameConstants.MODE_ONLINE);
    }

    @Override
    public int[] getAiMove() {
        return null;
    }

    public boolean isUndoable() {
        return lastMove != null;
    }

    public void notifyUndo() {
        resetStatus();
    }

    public void drawPieces(Canvas canvas) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (qzs[i][j] != EMPTY) {
                    drawPiece(canvas, i, j);
                }
            }
        }
        if (lastMove != null) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(3);
            mPaint.setColor(Color.BLUE);

            canvas.drawRect(lastMove.fromX * mSquareWidth, lastMove.fromY * mSquareWidth, (lastMove.fromX + 1) * mSquareWidth, (lastMove.fromY + 1) * mSquareWidth, mPaint);

            mPaint.setColor(Color.GREEN);
            canvas.drawRect(lastMove.toX * mSquareWidth, lastMove.toY * mSquareWidth, (lastMove.toX + 1) * mSquareWidth, (lastMove.toY + 1) * mSquareWidth, mPaint);
        }
    }

    private void drawPiece(Canvas canvas, int i, int j) {
        int tempInt;
        //can go or eat
        if (qzs[i][j] < 0) {
            tempInt = Math.abs(qzs[i][j]);
            //can go
            if (tempInt == EMPTY) {
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Color.GREEN);
                canvas.drawCircle((i + 0.5f) * mSquareWidth, (j + 0.5f) * mSquareWidth, mSquareWidth / 4, mPaint);
                return;
            }
            //can eat
            int chessImage = ChessUtils.getChessImage(tempInt);
            piece = mMemoryCache.get(chessImage);
            if (piece == null) {
                piece = BitmapFactory.decodeResource(mContext.getResources(), chessImage);
                piece = Bitmap.createScaledBitmap(piece, (int) boardWidth + 1, (int) boardWidth + 1, false);
                addBitmapToMemoryCache(chessImage, piece);
            }
            canvas.drawBitmap(piece, i * mSquareWidth + 5, j * mSquareWidth + 5, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.GREEN);
            canvas.drawCircle((i + 0.5f) * mSquareWidth, (j + 0.5f) * mSquareWidth, mSquareWidth / 4, mPaint);
            return;
        }
        //exchange
        if (qzs[i][j] > EMPTY) {
            tempInt = qzs[i][j] - EMPTY;
            int tempInt2 = ChessUtils.getChessImage(tempInt);
            piece = mMemoryCache.get(tempInt2);

            if (piece == null) {
                piece = BitmapFactory.decodeResource(mContext.getResources(), tempInt2);
                piece = Bitmap.createScaledBitmap(piece, (int) pieceWidth, (int) pieceWidth, false);
                addBitmapToMemoryCache(tempInt2, piece);
            }
            canvas.drawBitmap(piece, i * mSquareWidth + 5, j * mSquareWidth + 5, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.BLUE);
            canvas.drawCircle((i + 0.5f) * mSquareWidth, (j + 0.5f) * mSquareWidth, mSquareWidth / 4, mPaint);
            return;
        }
        //normal
        int chessImage = ChessUtils.getChessImage(qzs[i][j]);
        piece = mMemoryCache.get(chessImage);
        if (piece == null) {
            piece = BitmapFactory.decodeResource(mContext.getResources(), chessImage);
            piece = Bitmap.createScaledBitmap(piece, (int) pieceWidth, (int) pieceWidth, false);
            addBitmapToMemoryCache(chessImage, piece);
        }
        canvas.drawBitmap(piece, i * mSquareWidth + 5, j * mSquareWidth + 5, mPaint);
    }

    public void drawBoard(Canvas canvas) {
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);//描边

        float endWidth = boardWidth;
        float tempFloat;
        for (int i = 0; i < 9; i++) {
            tempFloat = i * mSquareWidth;
            //橫线
            canvas.drawLine(0, tempFloat, endWidth, tempFloat, mPaint);
            //纵线
            canvas.drawLine(tempFloat, 0, tempFloat, endWidth, mPaint);
        }
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    mPaint.setColor(Color.WHITE);
                } else {
                    mPaint.setColor(Color.parseColor("#9AAE9B"));
                }
                canvas.drawRect(i * mSquareWidth, j * mSquareWidth, (i + 1) * mSquareWidth, (j + 1) * mSquareWidth, mPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, boolean isMyTurn) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) (event.getX() / mSquareWidth);
            int y = (int) (event.getY() / mSquareWidth);
            //越界
            if (x > 7 || y > 7) {
                return true;
            }

            if (qzs[x][y] == EMPTY) {
                //当前位置不可下,要<0才行
                selectedChess = null;
                resetStatus();
                return true;
            }
            if (selectedChess == null) {
                //还没选中棋子，要先选中棋子
                if ((isMyTurn
                        && ChessUtils.isMyChess(qzs[x][y], isIRunFirst))
                        || (!isMyTurn && !ChessUtils.isMyChess(qzs[x][y], isIRunFirst))) {
                    //玩家和棋要一致
                    selectedChess = new ChessPoint(qzs[x][y], x, y);
                    mMoveGenerator.showValidMoves(qzs, selectedChess.x, selectedChess.y);
                    return true;
                }
                return true;
            }
            if (qzs[x][y] > 0 && qzs[x][y] < EMPTY) {
                //如果是己方棋子则设为选中，否则取消选中
                resetStatus();
                if (ChessUtils.isSameSide(selectedChess.f, qzs[x][y])) {
                    selectedChess = new ChessPoint(qzs[x][y], x, y);
                    mMoveGenerator.showValidMoves(qzs, selectedChess.x, selectedChess.y);
                } else {
                    selectedChess = null;
                }
                return true;
            }
            playChess(isMyTurn, new int[]{x, y});
        }
        return true;
    }

    private boolean isPawnPromoted(int chessId) {
        return (chessId == W_BISHOP || chessId == B_BISHOP
                || chessId == W_KNIGHT || chessId == B_KNIGHT
                || chessId == W_QUEEN || chessId == B_QUEEN
                || chessId == W_ROOK || chessId == W_ROOK_N
                || chessId == B_ROOK || chessId == B_ROOK_N);
    }

    private void completeOneStep(boolean isMyTurn, int x, int y, int extraFlag) {
        boolean isPawnPromoted = isPawnPromoted(extraFlag);

        if (extraFlag == ChessUtils.FLAG_EXCHANGE) {
            //王车易位：王向车方向移动两格，车绕过王停在王的旁边
            if (selectedChess.x > x) {
                //车在王的左边
                qzs[selectedChess.x - 2][y] = selectedChess.f;//王易位
                qzs[selectedChess.x - 1][y] = qzs[x][y];//车易位
            } else {
                //车在王的右边
                qzs[selectedChess.x + 2][y] = selectedChess.f;
                qzs[selectedChess.x + 1][y] = qzs[x][y];
            }
            ChessRecord chessRecord = new ChessRecord();
            chessRecord.setFXY(true, selectedChess.f, selectedChess.x, selectedChess.y);
            chessRecord.setFXY(false, qzs[x][y], x, y);
            chessRecord.setExchange(true);
            saveRecord(chessRecord);
            qzs[x][y] = EMPTY;
        } else {
            ChessRecord chessRecord = new ChessRecord();
            if (isPawnPromoted) {
                //如果升兵了的话需要更新flag
                selectedChess.f = extraFlag;
                chessRecord.setPawnPromoted(true);
            }
            chessRecord.setFXY(true, selectedChess.f, selectedChess.x, selectedChess.y);
            chessRecord.setFXY(false, qzs[x][y], x, y);
            chessRecord.setExchange(false);
            saveRecord(chessRecord);
            qzs[x][y] = selectedChess.f;
        }
        updateMoveFlag(x, y);
        qzs[lastMove.fromX][lastMove.fromY] = EMPTY;//清除走前的位置
        selectedChess = null;//清除选中
        resetStatus();

        int[] data = isMyTurn ?
                ((isPawnPromoted || extraFlag == ChessUtils.FLAG_EXCHANGE) ?
                        new int[]{lastMove.fromX, lastMove.fromY, lastMove.toX, lastMove.toY, extraFlag} :
                        new int[]{lastMove.fromX, lastMove.fromY, lastMove.toX, lastMove.toY}) :
                null;
        mPlayListener.onPlayed(isMyTurn, data, !isMyTurn);
    }

    /**
     * 如果是兵、车、王的话，更新标记为不是第一步
     *
     * @param x
     * @param y
     */
    private void updateMoveFlag(int x, int y) {
        switch (qzs[x][y]) {
            case B_PAWN:
                qzs[x][y] = B_PAWN_N;
                break;
            case W_PAWN:
                qzs[x][y] = W_PAWN_N;
                break;
            case B_ROOK:
                qzs[x][y] = B_ROOK_N;
                break;
            case W_ROOK:
                qzs[x][y] = W_ROOK_N;
                break;
            case B_KING:
                qzs[x][y] = B_KING_N;
                break;
            case W_KING:
                qzs[x][y] = W_KING_N;
                break;
        }
    }

    private void saveRecord(ChessRecord record) {
        lastMove = record;
        if (!isGameOver) {
            gameRecordManager.saveChessRecord(lastMove);
        }
    }

    public int[] translateData(int[] data) {
        int fromX = data[0], fromY = data[1], toX = data[2], toY = data[3];
        if (isOnlineMode) {
            fromX = 7 - fromX;
            fromY = 7 - fromY;
            toX = 7 - toX;
            toY = 7 - toY;
        }
        selectedChess = new ChessPoint(qzs[fromX][fromY], fromX, fromY);
        //王车易位
        if (data.length == 5 && data[4] == ChessUtils.FLAG_EXCHANGE) {
            qzs[toX][toY] += EMPTY;
        } else {
            qzs[toX][toY] = -qzs[toX][toY];
        }
        return data.length == 5 ? new int[]{toX, toY, data[4]} : new int[]{toX, toY};//data[4]升兵角色/王车易位标识
    }

    private void resetStatus() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (qzs[i][j] == EMPTY) {
                    continue;
                }
                if (qzs[i][j] < 0) {
                    qzs[i][j] = -qzs[i][j];
                    continue;
                }
                if (qzs[i][j] > EMPTY) {
                    qzs[i][j] -= EMPTY;
                }
            }
        }
    }

    private void showPromotionWin(boolean isMyTurn, int x, int y, boolean isWhitePromote) {
        PromotionDialog mPromotionDialog = new PromotionDialog();
        mPromotionDialog.setWhitePromote(isWhitePromote);
        //这里要注意一下，不要把以下设置监听器放在以上的判断中
        mPromotionDialog.setOnRoleSelectListener(roleFlag -> {
            completeOneStep(isMyTurn, x, y, roleFlag);
        });
        FragmentActivity fragmentActivity = (FragmentActivity) mContext;
        mPromotionDialog.show(fragmentActivity.getSupportFragmentManager(), "PromotionDialog");
    }

    @Override
    public void startGame(boolean isIRunFirst) {
        gameRecordManager.clearGameRecords();
        initChessBoard(isIRunFirst);
        isGameOver = false;
    }

    @Override
    public void playChess(boolean isMyTurn, int[] oData) {
        int[] data = (isMyTurn || mGameMode == GameConstants.MODE_DOUBLE) ? oData : translateData(oData);

        int fromX = selectedChess.x;
        int fromY = selectedChess.y;
        int toX = data[0];
        int toY = data[1];

        if (qzs[toX][toY] > 0 && qzs[toX][toY] < EMPTY) {
            //该位置不能下
            return;
        }

        //<-------判断是否王车易位------
        if (qzs[toX][toY] > EMPTY) {
            completeOneStep(isMyTurn, toX, toY, ChessUtils.FLAG_EXCHANGE);
            return;
        }
        //------------end-------->
        if (qzs[toX][toY] < 0) {
            qzs[toX][toY] = -qzs[toX][toY];
        }
        //游戏结束
        if (ChessUtils.isKing(qzs[toX][toY])) {
            gameRecordManager.clearGameRecords();
            isGameOver = true;
            mPlayListener.onGameOver(ChessUtils.isMyChess(qzs[toX][toY], isIRunFirst) ? GameResults.OPPO_WON : GameResults.I_WON);
            completeOneStep(isMyTurn, toX, toY, -1);
            return;
        }
        //游戏没结束的话判断是否是兵
        if (ChessUtils.isPawn(qzs[fromX][fromY])) {
            if (toY == 0) {
                //我方升兵
                showPromotionWin(isMyTurn, toX, toY, isIRunFirst);
                return;
            } else if (toY == 7) {
                //对方升兵
                if (!isOnlineMode) {
                    showPromotionWin(isMyTurn, toX, toY, !isIRunFirst);
                    return;
                } else {
                    //对方升兵
                    completeOneStep(isMyTurn, toX, toY, data[2]);
                    Toast.makeText(mContext, mContext.getString(R.string.oppo_promote_pawn), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        //游戏没结束或者没升兵的话就会执行到这里
        completeOneStep(isMyTurn, toX, toY, -1);
    }

    @Override
    public void enterGame() {
        isGameOver = false;
        gameRecordManager.getChessRecords(ChessRecord.class)
                .subscribe(gameRecords -> {
                    if (mContext == null || ((Activity) mContext).isFinishing()) {
                        return;
                    }
                    boolean noHisRecords = gameRecords.size() == 0;
                    boolean isMyTurn = noHisRecords ? true : (((ChessRecord) gameRecords.get(0)).fromY > 3);

                    initChessBoard(isMyTurn);

                    if (!noHisRecords) {
                        ChessRecord move;
                        for (IChessRecord chessRecord : gameRecords) {
                            move = (ChessRecord) chessRecord;
                            qzs[move.toX][move.toY] = move.fromF;
                            qzs[move.fromX][move.fromY] = EMPTY;
                            updateMoveFlag(move.toX, move.toY);
                            isMyTurn = !isMyTurn;
                        }
                        lastMove = (ChessRecord) gameRecords.get(gameRecords.size() - 1);
                    }

                    mPlayListener.onGameDataReset(isIRunFirst, isMyTurn);
                });
    }

    public void initChessBoard(boolean isIFirst) {
        this.isIRunFirst = isIFirst;
        //如果是在线模式并且我是后手的话要更改棋局
        qzs = ChessUtils.getChessBoard(isIFirst, isOnlineMode);
        mMoveGenerator.setMyFirst(isIFirst);

        lastMove = null;
    }

    public boolean undoOnce(boolean isMyTurn) {
        if (lastMove == null) {
            return isMyTurn;
        }
        qzs[lastMove.fromX][lastMove.fromY] = lastMove.fromF;
        qzs[lastMove.toX][lastMove.toY] = lastMove.toF;

        if (lastMove.isExchange) {
            //如果是王车易位的话要移除王车之间的棋子
            int fromX, toX;
            if (lastMove.fromX > lastMove.toX) {//车在王的左侧
                fromX = lastMove.toX;
                toX = lastMove.fromX;
            } else {//车在王的右侧
                fromX = lastMove.fromX;
                toX = lastMove.toX;
            }
            for (int i = fromX + 1; i < toX; i++) {
                if (qzs[i][lastMove.fromY] == EMPTY) {
                    continue;
                }
                qzs[i][lastMove.fromY] = EMPTY;
            }
        } else if (lastMove.isPawnPromoted) {
            //升兵的话进行还原
            qzs[lastMove.fromX][lastMove.fromY] = lastMove.fromF < ChessID.B_PAWN ? ChessID.W_PAWN_N : ChessID.B_PAWN_N;
        }

        Object lastRecord = gameRecordManager.delToGetLastRecord(lastMove);
        lastMove = lastRecord == null ? null : (ChessRecord) lastRecord;

        return !isMyTurn;
    }

    private static final String LAST_MOVE = "lastMove";
    private static final String SELECTED_CHESS = "selectedChess";
    private static final String CHESS_ARRAY = "qzs";

    @Nullable
    @Override
    public Parcelable onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(CHESS_ARRAY, GsonHolder.getGson().toJson(qzs));

        if (lastMove != null) {
            bundle.putString(LAST_MOVE, lastMove.toString());
        }
        if (selectedChess != null) {
            bundle.putString(SELECTED_CHESS, selectedChess.toString());
        }

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        qzs = GsonHolder.getGson().fromJson(bundle.getString(CHESS_ARRAY), new TypeToken<int[][]>() {
        }.getType());

        if (bundle.containsKey(LAST_MOVE)) {
            lastMove = GsonHolder.getGson().fromJson(bundle.getString(LAST_MOVE), ChessRecord.class);
        }
        if (bundle.containsKey(SELECTED_CHESS)) {
            selectedChess = GsonHolder.getGson().fromJson(bundle.getString(SELECTED_CHESS), ChessPoint.class);
        }
    }
}
