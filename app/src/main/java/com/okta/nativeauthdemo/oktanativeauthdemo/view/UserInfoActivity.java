package com.okta.nativeauthdemo.oktanativeauthdemo.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.okta.nativeauthdemo.oktanativeauthdemo.R;
import com.okta.nativeauthdemo.oktanativeauthdemo.di.ServiceLocator;

import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class UserInfoActivity extends AppCompatActivity {
    private static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY";
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String accessToken;

    private TextView accessTokenView;
    private TextView userInfoView;

    public static Intent createIntent(Context context, String accessToken) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra(ACCESS_TOKEN_KEY, accessToken);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        this.accessToken = getIntent().getStringExtra(ACCESS_TOKEN_KEY);
        if(TextUtils.isEmpty(this.accessToken)) {
            finish();
        }
        initView();
        requestUserInfoData();
    }

    private void initView() {
        accessTokenView = (TextView) findViewById(R.id.access_token_value);
        userInfoView = (TextView) findViewById(R.id.user_info_field);

        accessTokenView.setText(accessToken);
    }

    private void requestUserInfoData() {
        compositeDisposable.add(ServiceLocator.getInstance()
                .getAuthNativeFlowInteractor(this)
                .getUserInfo(this.accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        userInfoView.setText(response);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.dispose();
    }
}
