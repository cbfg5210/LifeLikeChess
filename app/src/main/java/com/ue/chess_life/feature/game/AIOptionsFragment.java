package com.ue.chess_life.feature.game;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.ue.chess_life.R;
import com.ue.chess_life.event.AIChangedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hawk on 2017/1/26.
 */

public class AIOptionsFragment extends DialogFragment {
    private static final String ARG_AI_NUM = "arg_ai_num";
    private static final String ARG_CURRENT_AI = "arg_current_ai";

    private AIChangedListener aiChangedListener;
    private AIOptionsAdapter mAIOptionsAdapter;

    private int currentAiLevel;
    private int aiNum;
    private List<String> aiOptions;

    public void setAiChangedListener(AIChangedListener aiChangedListener) {
        this.aiChangedListener = aiChangedListener;
    }

    public static AIOptionsFragment newInstance(int aiNum, int currentAiLevel) {
        AIOptionsFragment aiLevelOptionsFragment = new AIOptionsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_CURRENT_AI, currentAiLevel);
        arguments.putInt(ARG_AI_NUM, aiNum);
        aiLevelOptionsFragment.setArguments(arguments);
        return aiLevelOptionsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        aiNum = arguments.getInt(ARG_AI_NUM);
        currentAiLevel = arguments.getInt(ARG_CURRENT_AI);

        String[] options = getResources().getStringArray(R.array.ai_options);
        aiOptions = new ArrayList<>(aiNum);
        for (int i = 0; i < aiNum; i++) {
            aiOptions.add(options[i]);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layoutView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_ai_options, null);

        RecyclerView rvAIOptions = layoutView.findViewById(R.id.rvAIOptions);
        mAIOptionsAdapter = new AIOptionsAdapter(getContext(), aiOptions);
        rvAIOptions.setAdapter(mAIOptionsAdapter);

        mAIOptionsAdapter.setCurrentSelectedIndex(currentAiLevel);

        return new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.choose_ai_level))
                .setView(layoutView)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        aiChangedListener.onAIChanged(mAIOptionsAdapter.getCurrentSelectedIndex());
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setCancelable(true)
                .create();
    }
}