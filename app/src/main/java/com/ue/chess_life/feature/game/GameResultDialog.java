package com.ue.chess_life.feature.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ue.chess_life.R;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.constant.GameResults;

/**
 * Created by hawk on 2017/12/6.
 */

public class GameResultDialog extends DialogFragment {
    private static final String ARG_GAME_MODE = "arg_game_mode";
    private static final String ARG_RESULT_FLAG = "arg_result_flag";

    public static GameResultDialog newInstance(int gameMode, int resultFlag) {
        GameResultDialog dialog = new GameResultDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_GAME_MODE, gameMode);
        bundle.putInt(ARG_RESULT_FLAG, resultFlag);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.GameResultDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_game_result, null);

        Bundle arguments = getArguments();
        if (arguments != null) {
            int gameMode = arguments.getInt(ARG_GAME_MODE);
            int resultFlag = arguments.getInt(ARG_RESULT_FLAG);

            int gameResultImgRes = resultFlag == GameResults.DRAW ?
                    R.drawable.svg_result_draw :
                    (gameMode == GameConstants.MODE_DOUBLE ?
                            (resultFlag == GameResults.I_WON ? R.drawable.svg_result_1_won : R.drawable.svg_result_2_won) :
                            ((resultFlag == GameResults.I_WON || resultFlag == GameResults.OPPO_SURRENDER) ? R.drawable.svg_result_victory : R.drawable.svg_result_defeat));

            ((ImageView) view.findViewById(R.id.ivGameResultImg)).setImageResource(gameResultImgRes);
        }

        return view;
    }
}
