package com.ue.chess_life.feature.game;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxFragment;
import com.ue.chess_life.R;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.entity.EaseDefaultEmojiData;
import com.ue.chess_life.entity.EaseEmoji;
import com.ue.chess_life.entity.EaseEmojiGroup;
import com.ue.chess_life.event.ChangePlayerEvent;
import com.ue.chess_life.event.ToastChatEvent;
import com.ue.chess_life.util.EaseSmileUtils;
import com.ue.chess_life.widget.emoji.EmojiMenu;
import com.ue.chess_life.widget.emoji.EmojiMenuBase;
import com.ue.resource.model.AppUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by hawk on 2017/6/17.
 */

public class ChatFragment extends RxFragment {
    @BindView(R.id.tv_chat_title)
    TextView mTvChatTitle;
    @BindView(R.id.rv_chat_list)
    RecyclerView mRvChatList;
    @BindView(R.id.game_chat_input)
    EditText mGameChatInput;
    @BindView(R.id.game_chat_emoji)
    ImageView mGameChatEmoji;
    @BindView(R.id.game_chat_send)
    ImageButton mGameChatSend;
    @BindView(R.id.game_chat_panel)
    LinearLayout mGameChatPanel;
    @BindView(R.id.game_chat_emoji_panel)
    EmojiMenu mGameChatEmojiPanel;
    Unbinder unbinder;

    private String oppoUserName;
    private ChatAdapter mChatMessageAdapter;
    private InputMethodManager inputManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.fragment_chat, null);
        unbinder = ButterKnife.bind(this, layoutView);

        initEmojiMenu();
        mChatMessageAdapter = new ChatAdapter(getContext(), null);
        mRvChatList.setAdapter(mChatMessageAdapter);

        return layoutView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatMessageEvent(EMMessage emMessage) {
        mChatMessageAdapter.addMessage(emMessage);
        mRvChatList.scrollToPosition(mChatMessageAdapter.getItemCount() - 1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onPlayerChangeEvent(ChangePlayerEvent changePlayerEvent) {
        AppUser newPlayer = changePlayerEvent.newPlayer;
        oppoUserName = newPlayer.userName;
        mTvChatTitle.setText(newPlayer.userNick);
        if (mChatMessageAdapter != null) {
            mChatMessageAdapter.setOppoUser(newPlayer);
            mChatMessageAdapter.clearMessages();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.game_chat_panel,
            R.id.game_chat_input,
            R.id.game_chat_emoji,
            R.id.game_chat_send})
    public void onViewClicked(View view) {
        int viewId = view.getId();
        if (viewId == R.id.game_chat_input) {
            if (mGameChatEmojiPanel != null && mGameChatEmojiPanel.isShown()) {
                toggleEmojis();
            }
            setKeyboardVisible(true);
            return;
        }
        if (viewId == R.id.game_chat_emoji) {
            setKeyboardVisible(false);
            toggleEmojis();
            return;
        }
        if (viewId == R.id.game_chat_send) {
            String txt = mGameChatInput.getText().toString().trim();
            if (TextUtils.isEmpty(txt)) {
                return;
            }
            mGameChatInput.setText("");
            EventBus.getDefault().post(new ToastChatEvent(true, txt));

            if (TextUtils.isEmpty(oppoUserName)) {
                return;
            }
            EMMessage emMessage = EMMessage.createTxtSendMessage(txt, oppoUserName);
            emMessage.setAttribute(EaseConstants.ATTR_GAME_CHAT, "y");
            emMessage.setMessageStatusCallback(new EMCallBack() {
                @Override
                public void onSuccess() {
                    updateViews();
                }

                @Override
                public void onError(int i, String s) {
                    updateViews();
                }

                @Override
                public void onProgress(int i, String s) {
                }
            });
            EMClient.getInstance().chatManager().sendMessage(emMessage);
            mChatMessageAdapter.addMessage(emMessage);
            mRvChatList.scrollToPosition(mChatMessageAdapter.getItemCount() - 1);

            return;
        }
    }

    private void updateViews() {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(integer -> mChatMessageAdapter.notifyDataSetChanged());
    }

    private void initEmojiMenu() {
        mGameChatEmojiPanel.setTabBarVisibility(false);

        List<EaseEmojiGroup> emojiconGroupList = new ArrayList<>();
        emojiconGroupList.add(new EaseEmojiGroup(R.mipmap.ee_1, Arrays.asList(EaseDefaultEmojiData.getData())));

        mGameChatEmojiPanel.init(emojiconGroupList);
        mGameChatEmojiPanel.setEmojiconMenuListener(new EmojiMenuBase.EaseEmojiconMenuListener() {
            @Override
            public void onExpressionClicked(EaseEmoji emojicon) {
                if (emojicon.getEmojiText() != null) {
                    mGameChatInput.append(EaseSmileUtils.getSmiledText(getContext(), emojicon.getEmojiText()));
                }
            }

            @Override
            public void onDeleteImageClicked() {
                if (!TextUtils.isEmpty(mGameChatInput.getText())) {
                    KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    mGameChatInput.dispatchKeyEvent(event);
                }
            }
        });
    }

    /**
     * show or hide emojicon
     */
    public void toggleEmojis() {
        if (mGameChatEmojiPanel.getVisibility() == View.VISIBLE) {
            mGameChatEmojiPanel.setVisibility(View.GONE);
            mGameChatEmoji.setSelected(false);
        } else {
            mGameChatEmojiPanel.setVisibility(View.VISIBLE);
            mGameChatEmoji.setSelected(true);
        }
    }

    private void setKeyboardVisible(boolean isVisible) {
        if (inputManager == null) {
            inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (isVisible) {
            inputManager.showSoftInput(mGameChatInput, 0);
        } else {
            inputManager.hideSoftInputFromWindow(mGameChatInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
