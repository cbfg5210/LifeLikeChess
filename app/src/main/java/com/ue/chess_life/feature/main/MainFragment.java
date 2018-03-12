package com.ue.chess_life.feature.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle2.components.support.RxFragment;
import com.ue.adapterdelegate.Item;
import com.ue.chess_life.BuildConfig;
import com.ue.chess_life.R;
import com.ue.chess_life.entity.GameItem;
import com.ue.chess_life.feature.ad.ADManager;
import com.ue.library.util.BackPressedUtils;
import com.ue.recommend.RecommendSheetView;
import com.ue.resource.constant.GameConstants;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;

/**
 * Created by hawk on 2017/10/25.
 */

public class MainFragment extends RxFragment {
    @BindView(R.id.dsvGamePager)
    DiscreteScrollView mDsvGamePager;
    @BindView(R.id.mbsRecommendSheet)
    RecommendSheetView mbsRecommendSheet;
    @BindView(R.id.vgMainBanner)
    ViewGroup vgMainBanner;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        List<Item> gameItemList = new ArrayList<>();
        gameItemList.add(new GameItem(getContext(), GameConstants.GAME_GB));
        gameItemList.add(new GameItem(getContext(), GameConstants.GAME_MO));
        gameItemList.add(new GameItem(getContext(), GameConstants.GAME_RV));
        gameItemList.add(new GameItem(getContext(), GameConstants.GAME_CC));
        gameItemList.add(new GameItem(getContext(), GameConstants.GAME_IC));

        GameItemAdapter adapter = new GameItemAdapter(getActivity(), gameItemList);
        mDsvGamePager.setAdapter(adapter);
        mDsvGamePager.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.85f)
                .build());

        View bannerView = ADManager.getBannerView(getActivity(), BuildConfig.GDT_MAIN_BANNER_ID);
        vgMainBanner.addView(bannerView);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void onBackPressed() {
        if (mbsRecommendSheet.getState() == STATE_EXPANDED) {
            mbsRecommendSheet.hideBottomSheet();
            return;
        }
        BackPressedUtils.exitIfBackTwice(getActivity(), getString(R.string.tap_again_to_exit));
    }
}
