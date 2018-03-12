package com.ue.chess_life.feature.ad;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.comm.util.AdError;
import com.ue.chess_life.BuildConfig;

/**
 * Created by hawk on 2017/10/25.
 */

public class ADManager {
    public static View getBannerView(Activity context, String bannerId) {
        BannerView bannerView = new BannerView(context, ADSize.BANNER, BuildConfig.GDT_APP_ID, bannerId);
        bannerView.setRefresh(30);
        bannerView.setADListener(new AbstractBannerADListener() {
            @Override
            public void onNoAD(AdError adError) {
                Log.e("ADManager", "onNoAD: msg=" + adError.getErrorMsg());
            }

            @Override
            public void onADReceiv() {
            }
        });
        bannerView.loadAD();

        return bannerView;
    }

    public static InterstitialAD getInterstitialAD(Activity context, String interstitialId) {
        final InterstitialAD iad = new InterstitialAD(context, BuildConfig.GDT_APP_ID, interstitialId);
        iad.setADListener(new AbstractInterstitialADListener() {
            @Override
            public void onADReceive() {
            }

            @Override
            public void onNoAD(AdError adError) {
                Log.i("AD_DEMO", "LoadInterstitialAd Fail:" + adError.getErrorMsg());
            }
        });
        iad.loadAD();

        return iad;
    }
}
