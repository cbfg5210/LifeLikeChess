package com.ue.chess_life.feature.game.emoji;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.ue.chess_life.R;
import com.ue.chess_life.entity.EaseEmoji;
import com.ue.chess_life.util.EaseSmileUtils;
import com.ue.library.util.ImageLoader;

import java.util.List;

public class EmojiGridAdapter extends ArrayAdapter<EaseEmoji> {

    public EmojiGridAdapter(Context context, int textViewResourceId, List<EaseEmoji> objects, EaseEmoji.Type emojiconType) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.layout_row_expression, null);
        }

        ImageView imageView = convertView.findViewById(R.id.iv_expression);
        EaseEmoji emojicon = getItem(position);

        if (EaseSmileUtils.DELETE_KEY.equals(emojicon.getEmojiText())) {
            imageView.setImageResource(R.mipmap.ic_delete_expression);
        } else {
            if (emojicon.getIcon() != 0) {
                imageView.setImageResource(emojicon.getIcon());
            } else if (emojicon.getIconPath() != null) {
                ImageLoader.displayImage(getContext(), emojicon.getIconPath(), imageView);
            }
        }


        return convertView;
    }

}
