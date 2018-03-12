package com.ue.chess_life.feature.main;

import android.app.Activity;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ue.adapterdelegate.BaseAdapterDelegate;
import com.ue.adapterdelegate.DelegationAdapter;
import com.ue.adapterdelegate.Item;
import com.ue.adapterdelegate.OnDelegateClickListener;
import com.ue.chess_life.R;
import com.ue.chess_life.entity.GameItem;
import com.ue.chess_life.feature.game.GameActivity;
import com.ue.chess_life.feature.rule.GameRuleActivity;
import com.ue.library.util.ImageLoader;
import com.ue.resource.constant.GameConstants;

import java.util.List;

/**
 * Created by hawk on 2017/10/25.
 */
class GameItemAdapter extends DelegationAdapter<Item> implements OnDelegateClickListener {

    private Activity activity;

    GameItemAdapter(Activity activity, List<Item> items) {
        this.activity = activity;
        this.items = items;

        GameItemDelegate delegate = new GameItemDelegate(activity);
        delegate.setOnDelegateClickListener(this);
        this.addDelegate(delegate);
    }

    @Override
    public void onClick(View view, int position) {
        if (position < 0 || position >= getItemCount()) {
            return;
        }
        int viewId = view.getId();
        GameItem gameItem = (GameItem) items.get(position);
        if (viewId == R.id.igae_game_rule) {
            GameRuleActivity.start(activity, gameItem.gameFlag);
            return;
        }
        if (viewId == R.id.igae_double_mode) {
            GameActivity.start(activity, gameItem.gameFlag, GameConstants.MODE_DOUBLE, gameItem.gameModes[0]);
            return;
        }
        if (viewId == R.id.igae_single_mode) {
            GameActivity.start(activity, gameItem.gameFlag, GameConstants.MODE_SINGLE, gameItem.gameModes[1]);
            return;
        }
        if (viewId == R.id.igae_invite_mode) {
            GameActivity.start(activity, gameItem.gameFlag, GameConstants.MODE_INVITE, gameItem.gameModes[2]);
            return;
        }
        if (viewId == R.id.igae_online_mode) {
            GameActivity.start(activity, gameItem.gameFlag, GameConstants.MODE_ONLINE, gameItem.gameModes[3]);
            return;
        }
    }

    /**
     * Delegate
     */
    private static class GameItemDelegate extends BaseAdapterDelegate<Item> {
        public GameItemDelegate(Activity activity) {
            super(activity, R.layout.item_game);
        }

        @NonNull
        @Override
        protected RecyclerView.ViewHolder onCreateViewHolder(@NonNull View itemView) {
            final ViewHolder vHolder = new ViewHolder(itemView);
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onDelegateClickListener != null) {
                        onDelegateClickListener.onClick(view, vHolder.getAdapterPosition());
                    }
                }
            };
            vHolder.mIgaeGameRule.setOnClickListener(onClickListener);
            vHolder.mIgaeSingleMode.setOnClickListener(onClickListener);
            vHolder.mIgaeDoubleMode.setOnClickListener(onClickListener);
            vHolder.mIgaeInviteMode.setOnClickListener(onClickListener);
            vHolder.mIgaeOnlineMode.setOnClickListener(onClickListener);
            return vHolder;
        }

        @Override
        public boolean isForViewType(@NonNull Item item) {
            return item instanceof GameItem;
        }

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @NonNull Item item, @NonNull List payloads) {
            ViewHolder vHolder = (ViewHolder) holder;
            GameItem gameItem = (GameItem) item;

            ImageLoader.displayImage(holder.itemView.getContext(), gameItem.gameIcon, vHolder.ivGameIcon);
            vHolder.mIgaeGameRule.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            vHolder.mTvGameName.setText(gameItem.gameName);

            vHolder.mIgaeSingleMode.setVisibility(TextUtils.isEmpty(gameItem.gameModes[1]) ? View.INVISIBLE : View.VISIBLE);
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivGameIcon;
            private TextView mTvGameName;
            private TextView mIgaeGameRule;
            private TextView mIgaeOnlineMode;
            private TextView mIgaeInviteMode;
            private TextView mIgaeDoubleMode;
            private TextView mIgaeSingleMode;

            ViewHolder(View itemView) {
                super(itemView);
                ivGameIcon = itemView.findViewById(R.id.ivGameIcon);
                mTvGameName = itemView.findViewById(R.id.tv_game_name);
                mIgaeGameRule = itemView.findViewById(R.id.igae_game_rule);
                mIgaeOnlineMode = itemView.findViewById(R.id.igae_online_mode);
                mIgaeInviteMode = itemView.findViewById(R.id.igae_invite_mode);
                mIgaeDoubleMode = itemView.findViewById(R.id.igae_double_mode);
                mIgaeSingleMode = itemView.findViewById(R.id.igae_single_mode);
            }
        }
    }
}