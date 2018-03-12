package com.ue.chess_life.widget.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ue.chess_life.entity.EaseEmoji;

public class EmojiMenuBase extends LinearLayout{
    protected EaseEmojiconMenuListener listener;
    
    public EmojiMenuBase(Context context) {
        super(context);
    }
    
    public EmojiMenuBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public EmojiMenuBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    
    /**
     * set emojicon menu listener
     * @param listener
     */
    public void setEmojiconMenuListener(EaseEmojiconMenuListener listener){
        this.listener = listener;
    }
    
    public interface EaseEmojiconMenuListener{
        /**
         * on emojicon clicked
         * @param emojicon
         */
        void onExpressionClicked(EaseEmoji emojicon);
        /**
         * on delete image clicked
         */
        void onDeleteImageClicked();
    }
}
