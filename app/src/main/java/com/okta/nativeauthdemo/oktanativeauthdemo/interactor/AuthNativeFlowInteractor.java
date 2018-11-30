package com.okta.nativeauthdemo.oktanativeauthdemo.interactor;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.okta.nativeauthdemo.oktanativeauthdemo.network.OktaAuthService;
import com.okta.nativeauthdemo.oktanativeauthdemo.network.model.AccessTokenResponse;
import com.okta.nativeauthdemo.oktanativeauthdemo.network.model.AuthnRequest;
import com.okta.nativeauthdemo.oktanativeauthdemo.network.model.AuthnResponse;

import net.openid.appauth.CodeVerifierUtil;
import java.security.SecureRandom;
import java.util.Set;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

public class AuthNativeFlowInteractor {
    private final String AUTHN_API = "api/v1/authn";
    private final String AUTHORIZE_API = "oauth2/default/v1/authorize";
    private final String EXCHANGE_API = "oauth2/default/v1/token";
    private final String USERINFO_API = "oauth2/default/v1/userinfo";
    private static final int STATE_LENGTH = 16;

    private String redirectUri;
    private String clientId;
    private OktaAuthService oktaAuthService;
    private Set<String> scopes;

    public AuthNativeFlowInteractor(String redirectUri, String clientId, Set<String> scopes, OktaAuthService oktaAuthService) {
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.oktaAuthService = oktaAuthService;
        this.scopes = scopes;
    }

    private static String generateRandomState() {
        SecureRandom sr = new SecureRandom();
        byte[] random = new byte[STATE_LENGTH];
        sr.nextBytes(random);
        return Base64.encodeToString(random, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    public Single<String> getUserInfo(String accessToken) {
        return oktaAuthService.getUserInfo(USERINFO_API, "Bearer "+accessToken).map(new Function<ResponseBody, String>() {
            @Override
            public String apply(ResponseBody responseBody) throws Exception {
                return responseBody.string();
            }
        });
    }

    public Single<String> login(String login, String password) {
        final AuthnRequest authnRequest = new AuthnRequest(login, password, redirectUri, false,false);
        final String codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier();
        final String codeChallenge = CodeVerifierUtil.deriveCodeVerifierChallenge(codeVerifier);
        final String codeChallengeMethod = CodeVerifierUtil.getCodeVerifierChallengeMethod();


        return oktaAuthService
                .login(AUTHN_API, authnRequest)
                .flatMap(new Function<AuthnResponse, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(AuthnResponse authnResponse) throws Exception {
                        return getAuthorizationCode(authnResponse.sessionToken, codeChallenge, codeChallengeMethod);
                    }
                })
                .flatMap(new Function<String, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(String code) throws Exception {
                        return exchangeCodeForTokens(code, codeVerifier)
                                .map(new Function<AccessTokenResponse, String>() {
                                    @Override
                                    public String apply(AccessTokenResponse accessTokenResponse) throws Exception {
                                        return accessTokenResponse.access_token;
                                    }
                                });
                    }
                });

    }

    private Single<String> getAuthorizationCode(String sessionToken, String codeChallenge, String codeChallengeMethod) {
        return oktaAuthService.authorize(AUTHORIZE_API,
                clientId,
                "code",
                TextUtils.join(" ", this.scopes),
                redirectUri,
                generateRandomState(),
                codeChallengeMethod,
                codeChallenge,
                sessionToken)
                .map(new Function<retrofit2.Response<Object>, String>() {
                    @Override
                    public String apply(retrofit2.Response<Object> responseBody) throws Exception {
                        Exception badResponseException = new RuntimeException("Error to get code from oauth2/default/v1/authorize API");
                        if(responseBody.code() != 302)
                            throw badResponseException;

                        String location = responseBody.headers().get("Location");
                        if(location == null || location.length() == 0)
                            throw badResponseException;

                        Uri locationUri = Uri.parse(location);
                        String code = locationUri.getQueryParameter("code");
                        if(TextUtils.isEmpty(code))
                            throw badResponseException;

                        return code;
                    }
                });
    }

    private Single<AccessTokenResponse> exchangeCodeForTokens(String code, String codeVerifier) {
         return oktaAuthService.exchangeCode(EXCHANGE_API,
                "authorization_code",
                 clientId,
                 redirectUri,
                 code,
                 codeVerifier);
    }
}
