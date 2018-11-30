package com.okta.nativeauthdemo.oktanativeauthdemo.network.model;

public class AuthnOptions {
    public AuthnOptions(boolean multiOptionalFactorEnroll, boolean warnBeforePasswordExpired) {
        this.multiOptionalFactorEnroll = multiOptionalFactorEnroll;
        this.warnBeforePasswordExpired = warnBeforePasswordExpired;
    }

    boolean multiOptionalFactorEnroll;
    boolean warnBeforePasswordExpired;
}
