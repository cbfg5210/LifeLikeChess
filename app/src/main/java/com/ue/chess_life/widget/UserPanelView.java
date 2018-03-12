package com.ue.chess_life.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ue.chess_life.R;
import com.ue.resource.model.AppUser;
import com.ue.resource.util.GsonHolder;

/**
 * Created by hawk on 2016/12/24.
 */

public class UserPanelView extends FrameLayout {
    private ViewGroup game_my_panel;
    private TextView game_my_name;
    private ImageView playerOneAvatar;
    private ViewGroup game_oppo_panel;
    private TextView game_oppo_name;
    private ImageView playerTwoAvatar;
    private TextView myExtraTxt;
    private TextView oppoExtraTxt;

    private AppUser playerOne;
    private AppUser playerTwo;
    private String myScoreInfo;
    private String oppoScoreInfo;

    public UserPanelView(Context context) {
        this(context, null);
    }

    public UserPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_user_panel, this);
        game_my_panel = findViewById(R.id.vgMyPanel);
        game_my_name = findViewById(R.id.tvMyName);
        playerOneAvatar = findViewById(R.id.playerOneAvatar);
        game_oppo_panel = findViewById(R.id.vgOppoPanel);
        game_oppo_name = findViewById(R.id.tvOppoName);
        playerTwoAvatar = findViewById(R.id.playerTwoAvatar);
        myExtraTxt = findViewById(R.id.game_my_chesses);
        oppoExtraTxt = findViewById(R.id.game_oppo_chesses);
    }

    public void setMePlayer(AppUser mePlayer) {
        this.playerOne = mePlayer;

        game_my_name.setText(mePlayer.userNick);
        playerOneAvatar.setImageResource(mePlayer.isMale ? R.drawable.svg_boy : R.drawable.svg_girl);

        myExtraTxt.setText("");
        oppoExtraTxt.setText("");
    }

    public void setOppoPlayer(AppUser oppoPlayer) {
        this.playerTwo = oppoPlayer;

        myExtraTxt.setText("");
        oppoExtraTxt.setText("");

        if (oppoPlayer == null) {
            game_oppo_name.setText("");
            playerTwoAvatar.setImageResource(0);
        } else {
            game_oppo_name.setText(oppoPlayer.userNick);
            playerTwoAvatar.setImageResource(oppoPlayer.isMale ? R.drawable.svg_boy : R.drawable.svg_girl);
        }
    }

    public void setRobotPlayer(AppUser robotPlayer) {
        game_oppo_name.setText(robotPlayer.userNick);
        playerTwoAvatar.setImageResource(R.drawable.svg_robot);
    }

    public void updateMyScoreInfo(String myScoreInfo) {
        if (TextUtils.isEmpty(myScoreInfo)) {
            myExtraTxt.setText("");
            return;
        }
        myExtraTxt.setText(myScoreInfo);
    }

    public void updateOppoScoreInfo(String oppoScoreInfo) {
        if (TextUtils.isEmpty(oppoScoreInfo)) {
            myExtraTxt.setText("");
            return;
        }
        oppoExtraTxt.setText(oppoScoreInfo);
    }

    public void updateFocus(boolean isMyTurn) {
        game_my_panel.setSelected(isMyTurn);
        game_oppo_panel.setSelected(!isMyTurn);
    }

    private static final String INSTANCE = "instance";
    private static final String PLAYER_ONE = "playerOne";
    private static final String PLAYER_TWO = "playerTwo";
    private static final String MY_SCORE_INFO = "myScoreInfo";
    private static final String OPPO_SCORE_INFO = "oppoScoreInfo";

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        if (playerOne != null) {
            bundle.putString(PLAYER_ONE, playerOne.toString());
        }
        if (playerTwo != null) {
            bundle.putString(PLAYER_TWO, playerTwo.toString());
        }
        if (!TextUtils.isEmpty(myScoreInfo)) {
            bundle.putString(MY_SCORE_INFO, myScoreInfo);
        }
        if (!TextUtils.isEmpty(oppoScoreInfo)) {
            bundle.putString(OPPO_SCORE_INFO, oppoScoreInfo);
        }

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));

            if (bundle.containsKey(PLAYER_ONE)) {
                playerOne = GsonHolder.getGson().fromJson(bundle.getString(PLAYER_ONE), AppUser.class);
            }
            if (bundle.containsKey(PLAYER_TWO)) {
                playerTwo = GsonHolder.getGson().fromJson(bundle.getString(PLAYER_TWO), AppUser.class);
            }
            if (bundle.containsKey(MY_SCORE_INFO)) {
                myScoreInfo = bundle.getString(MY_SCORE_INFO);
            }
            if (bundle.containsKey(OPPO_SCORE_INFO)) {
                oppoScoreInfo = bundle.getString(OPPO_SCORE_INFO);
            }

            return;
        }
        super.onRestoreInstanceState(state);
    }
}