package com.ue.ichess.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.ue.ichess.R;

import static com.ue.ichess.entity.ChessID.B_BISHOP;
import static com.ue.ichess.entity.ChessID.B_KNIGHT;
import static com.ue.ichess.entity.ChessID.B_QUEEN;
import static com.ue.ichess.entity.ChessID.B_ROOK_N;
import static com.ue.ichess.entity.ChessID.W_BISHOP;
import static com.ue.ichess.entity.ChessID.W_KNIGHT;
import static com.ue.ichess.entity.ChessID.W_QUEEN;
import static com.ue.ichess.entity.ChessID.W_ROOK_N;

/**
 * Created by hawk on 2016/12/9.
 */

public class PromotionDialog extends DialogFragment {
    private RadioGroup promotionGroup;
    private OnRoleSelectListener mOnRoleSelectListener;
    private boolean isWhitePromote;
    private int selectedRoleFlag;

    public void setOnRoleSelectListener(OnRoleSelectListener onRoleSelectListener) {
        mOnRoleSelectListener = onRoleSelectListener;
    }

    public void setWhitePromote(boolean isWhitePromote) {
        this.isWhitePromote = isWhitePromote;
        selectedRoleFlag = isWhitePromote ? W_ROOK_N : B_ROOK_N;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layoutView = LayoutInflater.from(getContext()).inflate(R.layout.ic_promotion_dialog, null);

        promotionGroup = layoutView.findViewById(R.id.promotion_group);
        promotionGroup.check(R.id.promotion_rook);
        promotionGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            if (checkedId == R.id.promotion_rook) {
                selectedRoleFlag = isWhitePromote ? W_ROOK_N : B_ROOK_N;
                return;
            }
            if (checkedId == R.id.promotion_knight) {
                selectedRoleFlag = isWhitePromote ? W_KNIGHT : B_KNIGHT;
                return;
            }
            if (checkedId == R.id.promotion_bishop) {
                selectedRoleFlag = isWhitePromote ? W_BISHOP : B_BISHOP;
                return;
            }
            if (checkedId == R.id.promotion_queen) {
                selectedRoleFlag = isWhitePromote ? W_QUEEN : B_QUEEN;
                return;
            }
        });
        promotionGroup.findViewById(R.id.promotion_rook).setSelected(isWhitePromote);
        promotionGroup.findViewById(R.id.promotion_knight).setSelected(isWhitePromote);
        promotionGroup.findViewById(R.id.promotion_bishop).setSelected(isWhitePromote);
        promotionGroup.findViewById(R.id.promotion_queen).setSelected(isWhitePromote);

        return new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.promotion_chance))
                .setView(layoutView)
                .setPositiveButton(getString(R.string.sure), null)
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mOnRoleSelectListener.onRoleSelected(selectedRoleFlag);
    }

    public interface OnRoleSelectListener {
        void onRoleSelected(int roleFlag);
    }
}