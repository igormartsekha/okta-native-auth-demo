package com.okta.nativeauthdemo.oktanativeauthdemo.di;

import android.content.Context;

import com.okta.appauth.android.OAuthClientConfiguration;
import com.okta.nativeauthdemo.oktanativeauthdemo.interactor.AuthNativeFlowInteractor;
import com.okta.nativeauthdemo.oktanativeauthdemo.legacy.CustomOktaAppAuth;
import com.okta.nativeauthdemo.oktanativeauthdemo.network.OktaAuthService;

import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceLocator {
    final private static ServiceLocator instance = new ServiceLocator();

    private AuthNativeFlowInteractor authNativeFlowInteractor;
    private CustomOktaAppAuth customOktaAppAuth;

    public static ServiceLocator getInstance() {
        return  instance;
    }

    private ServiceLocator() { }

    public CustomOktaAppAuth getCustomOktaAppAuth(Context context) {
        if(customOktaAppAuth == null) {
            customOktaAppAuth = CustomOktaAppAuth.getInstance(context);
        }
        return customOktaAppAuth;
    }

    public AuthNativeFlowInteractor getAuthNativeFlowInteractor(Context context) {
        return getAuthNativeFlowInteractor(getCustomOktaAppAuth(context).getConfig());
    }

    public AuthNativeFlowInteractor getAuthNativeFlowInteractor(OAuthClientConfiguration clientConfiguration) {
        if(authNativeFlowInteractor == null) {
            String redirectUri = clientConfiguration.getRedirectUri().toString();
            String clientId = clientConfiguration.getClientId();
            String baseUrl = clientConfiguration.getDiscoveryUri().getScheme()+"://"+clientConfiguration.getDiscoveryUri().getAuthority();
            Set<String> scopes = clientConfiguration.getScopes();
            authNativeFlowInteractor = new AuthNativeFlowInteractor(redirectUri, clientId, scopes, getOktaAuthService(baseUrl));
        }
        return authNativeFlowInteractor;
    }

    private OktaAuthService getOktaAuthService(String baseUrl) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit.create(OktaAuthService.class);
    }




}
