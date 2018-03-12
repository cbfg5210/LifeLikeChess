package com.ue.chess_life.feature.game;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.ue.chess_life.R;
import com.ue.chess_life.util.AppUserUtils;
import com.ue.chess_life.util.EaseSmileUtils;
import com.ue.resource.model.AppUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hawk on 2017/6/17.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private static final int TYPE_RECEIVE = 0x1;
    private static final int TYPE_SEND = 0x2;

    private Context context;
    private List<EMMessage> mEMMessages;

    private SimpleDateFormat mSimpleDateFormat;
    private AppUser meUser;
    private AppUser oppoUser;

    public ChatAdapter(Context context, List<EMMessage> emMessages) {
        this.context = context;
        this.mEMMessages = emMessages == null ? new ArrayList<>() : emMessages;
        meUser = AppUserUtils.getCurrentUser();
        mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    public void setOppoUser(AppUser oppoUser) {
        this.oppoUser = oppoUser;
    }

    public void addMessage(EMMessage emMessage) {
        mEMMessages.add(emMessage);
        notifyItemInserted(mEMMessages.size() - 1);
    }

    public void clearMessages() {
        mEMMessages.clear();
    }

    @Override
    public int getItemViewType(int position) {
        return mEMMessages.get(position).direct() == EMMessage.Direct.SEND ? TYPE_SEND : TYPE_RECEIVE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(
                viewType == TYPE_RECEIVE ? R.layout.item_message_received : R.layout.item_message_sent,
                parent,
                false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EMMessage emMessage = mEMMessages.get(holder.getAdapterPosition());

        holder.mTvMsgTimestamp.setText(mSimpleDateFormat.format(emMessage.getMsgTime()));
        holder.mTvUserNick.setText(emMessage.getFrom());

        EMTextMessageBody txtBody = (EMTextMessageBody) emMessage.getBody();
        Spannable span = EaseSmileUtils.getSmiledText(context, txtBody.getMessage());
        // 设置内容
        holder.mTvMsgContent.setText(span, TextView.BufferType.SPANNABLE);

        if (emMessage.direct() == EMMessage.Direct.SEND) {
            holder.mIvUserAvatar.setSelected(meUser.isMale);
            switch (emMessage.status()) {
                case CREATE:
                    holder.mPbMsgSendProgress.setVisibility(View.VISIBLE);
                    holder.mIvMsgStatus.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    holder.mPbMsgSendProgress.setVisibility(View.GONE);
                    holder.mIvMsgStatus.setVisibility(View.GONE);
                    break;
                case FAIL:
                    holder.mPbMsgSendProgress.setVisibility(View.GONE);
                    holder.mIvMsgStatus.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS:
                    holder.mPbMsgSendProgress.setVisibility(View.VISIBLE);
                    holder.mIvMsgStatus.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        } else {
            if (oppoUser != null) {
                holder.mIvUserAvatar.setSelected(oppoUser.isMale);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mEMMessages == null ? 0 : mEMMessages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTvMsgTimestamp;
        ImageView mIvUserAvatar;
        TextView mTvUserNick;
        TextView mTvMsgContent;
        ImageView mIvMsgStatus;
        ProgressBar mPbMsgSendProgress;

        public ViewHolder(View itemView) {
            super(itemView);

            mTvMsgTimestamp = itemView.findViewById(R.id.tv_msg_timestamp);
            mIvUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            mTvUserNick = itemView.findViewById(R.id.tv_user_nick);
            mTvMsgContent = itemView.findViewById(R.id.tv_msg_content);

            if (itemView.findViewById(R.id.iv_msg_status) != null) {
                mIvMsgStatus = itemView.findViewById(R.id.iv_msg_status);
                mPbMsgSendProgress = itemView.findViewById(R.id.pb_msg_send_progress);
            }
        }
    }
}
