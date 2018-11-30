package com.okta.nativeauthdemo.oktanativeauthdemo.network.model;

public class AuthnRequest {
    public AuthnRequest(String username, String password, String relayState, boolean multiOptionalFactorEnroll, boolean warnBeforePasswordExpired) {
        this.username = username;
        this.password = password;
        this.relayState = relayState;
        this.options = new AuthnOptions(multiOptionalFactorEnroll, warnBeforePasswordExpired);
    }

    String username;
    String password;
    String relayState;
    AuthnOptions options;
}
