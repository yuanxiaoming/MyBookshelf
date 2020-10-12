//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.view.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.DocumentHelper;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.ReadBookPresenter;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

public class WelcomeActivity extends MBaseActivity {
    private static final String TAG = "WelcomeActivity";
    @BindView(R.id.iv_bg)
    ImageView ivBg;

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            Log.w(TAG, "onCreateActivity finish");
            finish();
            return;
        }
        Log.w(TAG, "onCreateActivity init");
        setContentView(R.layout.activity_welcome);
        AsyncTask.execute(DbHelper::getDaoSession);
        ButterKnife.bind(this);
        ivBg.setColorFilter(ThemeStore.accentColor(this));
        ValueAnimator welAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(800);
        welAnimator.setStartDelay(500);
        welAnimator.addUpdateListener(animation -> {
            float alpha = (Float) animation.getAnimatedValue();
            ivBg.setAlpha(alpha);
        });
        welAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        welAnimator.start();
        if (!preferences.getBoolean("initReader", false)) {
            initAssets();
        } else {
            join();
        }
    }

    private void startBookshelfActivity() {
        startActivityByAnim(new Intent(this, MainActivity.class), android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    private void startReadActivity() {
        Intent intent = new Intent(this, ReadBookActivity.class);
        intent.putExtra("openFrom", ReadBookPresenter.OPEN_FROM_APP);
        startActivity(intent);
    }

    @Override
    protected void initData() {

    }

    public void initAssets() {
        try {
            String text = DocumentHelper.readAssetsFile("myBookSource.txt");
            Log.w(TAG, "initAssets text " + text.length());
            Observable<List<BookSourceBean>> observable = BookSourceManager.importSource(text);
            if (observable != null) {
                observable.subscribe(new MyObserver<List<BookSourceBean>>() {
                    @Override
                    public void onNext(List<BookSourceBean> bookSourceBeans) {
                        Log.w(TAG,
                                "initAssets onNext bookSourceBeans size " + bookSourceBeans.size());
                        preferences.edit().putBoolean("initReader", true).apply();
                        join();

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.w(TAG, "initAssets onNext onError " + e.getMessage());
                        join();
                    }
                });
            } else {
                Log.w(TAG, "initAssets observable is null");
                join();
            }

        } catch (Throwable e1) {
            e1.printStackTrace();
            join();
        }
    }

    private void join() {
        boolean preferencesBoolean =
                preferences.getBoolean(getString(R.string.pk_default_read), false);
        Log.w(TAG, "join preferencesBoolean " + preferencesBoolean);
        if (preferencesBoolean) {
            startReadActivity();
        } else {
            startBookshelfActivity();
        }
        finish();
    }

}
