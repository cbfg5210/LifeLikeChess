package com.ue.chess_life.feature.rule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.chess_life.R;
import com.ue.chess_life.event.InvitedEvent;
import com.ue.chess_life.feature.common.InvitedMsgHandler;
import com.ue.resource.constant.GameConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GameRuleActivity extends RxAppCompatActivity {
    private static final String ARG_GAME_FLAG = "ARG_GAME_FLAG";
    @BindView(R.id.tv_rule_content)
    TextView mTvRuleContent;

    public static void start(Context context, int gameFlag) {
        Intent intent = new Intent(context, GameRuleActivity.class);
        intent.putExtra(ARG_GAME_FLAG, gameFlag);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_rule);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTvRuleContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        initContent();
    }

    private void initContent() {
        int whichGame = getIntent().getIntExtra(ARG_GAME_FLAG, GameConstants.GAME_GB);
        switch (whichGame) {
            case GameConstants.GAME_IC:
                setTitle(getString(R.string.game_rule_title, getString(R.string.game_ichess)));
                mTvRuleContent.setText(R.string.rule_chess);
                break;
            case GameConstants.GAME_CC:
                setTitle(getString(R.string.game_rule_title, getString(R.string.game_cchess)));
                mTvRuleContent.setText(R.string.rule_cnchess);
                break;
            case GameConstants.GAME_GB:
                setTitle(getString(R.string.game_rule_title, getString(R.string.game_gobang)));
                mTvRuleContent.setText(R.string.rule_gobang);
                break;
            case GameConstants.GAME_RV:
                setTitle(getString(R.string.game_rule_title, getString(R.string.game_reversi)));
                mTvRuleContent.setText(R.string.rule_reversi);
                break;
            case GameConstants.GAME_MO:
                setTitle(getString(R.string.game_rule_title, getString(R.string.game_moon)));
                mTvRuleContent.setText(R.string.rule_moon);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvitedEvent(InvitedEvent invitedEvent) {
        InvitedMsgHandler.handleInvitedMsg(this, invitedEvent.invitedMsg);
    }
}