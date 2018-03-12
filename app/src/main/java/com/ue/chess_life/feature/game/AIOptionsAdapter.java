package com.ue.chess_life.feature.game;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ue.chess_life.R;

import java.util.List;

/**
 * Created by hawk on 2017/5/14.
 */

public class AIOptionsAdapter extends RecyclerView.Adapter<AIOptionsAdapter.AiLevelOptionsVHolder> {
    private Context context;
    private List<String> aiNamesOptions;
    private int currentSelectedIndex = 0;

    public AIOptionsAdapter(Context context, List<String> aiNamesOptions) {
        this.context = context;
        this.aiNamesOptions = aiNamesOptions;
    }

    public void setCurrentSelectedIndex(int currentSelectedIndex) {
        this.currentSelectedIndex = currentSelectedIndex;
    }

    public int getCurrentSelectedIndex() {
        return currentSelectedIndex;
    }

    @Override
    public AiLevelOptionsVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(context).inflate(R.layout.item_ai_option, parent, false);
        return new AiLevelOptionsVHolder(layoutView);
    }

    @Override
    public int getItemCount() {
        return aiNamesOptions == null ? 0 : aiNamesOptions.size();
    }

    @Override
    public void onBindViewHolder(final AiLevelOptionsVHolder holder, int position) {
        holder.tvAiName.setText(aiNamesOptions.get(position));
        holder.tvAiName.setSelected(currentSelectedIndex == position);
        holder.tvAiName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSelectedIndex == holder.getAdapterPosition()) {
                    return;
                }
                int lastIndex = currentSelectedIndex;
                currentSelectedIndex = holder.getAdapterPosition();
                notifyItemChanged(lastIndex);
                notifyItemChanged(currentSelectedIndex);
            }
        });
    }

    static class AiLevelOptionsVHolder extends RecyclerView.ViewHolder {
        TextView tvAiName;

        public AiLevelOptionsVHolder(View itemView) {
            super(itemView);
            tvAiName = itemView.findViewById(R.id.tv_ai_name);
        }
    }
}