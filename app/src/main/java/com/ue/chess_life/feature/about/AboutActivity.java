package com.ue.chess_life.feature.about;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import com.qq.e.ads.interstitial.InterstitialAD;
import com.tencent.bugly.beta.Beta;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.aboutpage.AboutPageView;
import com.ue.aboutpage.DetailItem;
import com.ue.chess_life.BuildConfig;
import com.ue.chess_life.R;
import com.ue.chess_life.event.InvitedEvent;
import com.ue.chess_life.feature.ad.ADManager;
import com.ue.chess_life.feature.common.InvitedMsgHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Reference:
 * Author:
 * Date:2016/10/8.
 */
public class AboutActivity extends RxAppCompatActivity {
    //    @BindView(R.id.tvAppDescription)
//    TextView tvAppDescription;
//    @BindView(R.id.tvVersion)
//    TextView mTvVersion;
//    @BindView(R.id.tvFeedback)
//    TextView mTvFeedback;
//    @BindView(R.id.tvFaqDetail)
//    TextView tvFaqDetail;
    @BindView(R.id.apvAboutPage)
    AboutPageView apvAboutPage;

    private Dialog supportDialog;
    private InterstitialAD iad;

    public static void start(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        setTitle(getString(R.string.about));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String shareContent = getString(R.string.share_content);
        apvAboutPage.setShareContent(shareContent);
        apvAboutPage.setFaqItems(getFaqItems());
        apvAboutPage.setVerNoteItems(getVerNoteItems());

        apvAboutPage.toggleAppDescDetail();

        apvAboutPage.setAboutItemClickListener(new AboutPageView.OnAboutItemClickListener() {
            @Override
            public void onVersionClicked() {
                Beta.checkUpgrade(true, false);
            }

            @Override
            public void onSupportClicked() {
                showSupportDialog();
            }
        });

        loadInterstitialAD();
        Observable.timer(1500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(aLong -> {
                    if (iad != null && !isFinishing()) {
                        iad.show();
                    }
                });
    }

    private List<DetailItem> getVerNoteItems() {
        List<DetailItem> verNoteItems = new ArrayList<>();
        verNoteItems.add(new DetailItem(getString(R.string.ver_2d5d9), getString(R.string.verNote_2d5d9)));
        verNoteItems.add(new DetailItem(getString(R.string.ver_2d5d8), getString(R.string.verNote_2d5d8)));
        verNoteItems.add(new DetailItem(getString(R.string.ver_2d5d5), getString(R.string.verNote_2d5d5)));
        verNoteItems.add(new DetailItem(getString(R.string.ver_1d0), getString(R.string.verNote_1d0)));
        return verNoteItems;
    }

    private List<DetailItem> getFaqItems() {
        List<DetailItem> faqItems = new ArrayList<>();
        faqItems.add(new DetailItem(getString(R.string.faq_que1), getString(R.string.faq_ans1)));
        faqItems.add(new DetailItem(getString(R.string.faq_que2), getString(R.string.faq_ans2)));
        faqItems.add(new DetailItem(getString(R.string.faq_que3), getString(R.string.faq_ans3)));
        faqItems.add(new DetailItem(getString(R.string.faq_que4), getString(R.string.faq_ans4)));
        faqItems.add(new DetailItem(getString(R.string.faq_que5), getString(R.string.faq_ans5)));
        return faqItems;
    }

    private void showSupportDialog() {
        if (supportDialog == null) {
            supportDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.support))
                    .setMessage(getString(R.string.support_tip))
                    .setPositiveButton(getString(R.string.sure), null)
                    .create();

            supportDialog.setOnDismissListener(dialogInterface -> {
                if (iad != null) {
                    //可能报npe,但不会crash
                    iad.show();
                }
            });
        }
        loadInterstitialAD();
        supportDialog.show();
    }

    private void loadInterstitialAD() {
        if (iad == null) {
            iad = ADManager.getInterstitialAD(this, BuildConfig.GDT_ABOUT_INTERSTITIAL_ID);
        } else {
            iad.loadAD();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvitedEvent(InvitedEvent invitedEvent) {
        InvitedMsgHandler.handleInvitedMsg(this, invitedEvent.invitedMsg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (iad != null) {
            iad.destroy();
            iad = null;
        }
    }
}