package com.okta.nativeauthdemo.oktanativeauthdemo.legacy;

import android.content.Context;
import android.support.annotation.NonNull;

import com.okta.appauth.android.OAuthClientConfiguration;
import com.okta.appauth.android.OktaAppAuth;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

public class CustomOktaAppAuth extends OktaAppAuth {
    private static final AtomicReference<WeakReference<CustomOktaAppAuth>> CUSTOM_INSTANCE_REF = new AtomicReference(new WeakReference((CustomOktaAppAuth)null));

    private CustomOktaAppAuth(Context context) {
        super(context);
    }

    public static CustomOktaAppAuth getInstance(@NonNull Context context) {
        CustomOktaAppAuth oktaAppAuth = (CustomOktaAppAuth)((WeakReference)CUSTOM_INSTANCE_REF.get()).get();
        if (oktaAppAuth == null) {
            oktaAppAuth = new CustomOktaAppAuth(context.getApplicationContext());
            CUSTOM_INSTANCE_REF.set(new WeakReference(oktaAppAuth));
        }

        return oktaAppAuth;
    }

    public OAuthClientConfiguration getConfig() {
        return this.mConfiguration;
    }

}
