package com.okta.nativeauthdemo.oktanativeauthdemo.network;

import com.okta.nativeauthdemo.oktanativeauthdemo.network.model.AccessTokenResponse;
import com.okta.nativeauthdemo.oktanativeauthdemo.network.model.AuthnRequest;
import com.okta.nativeauthdemo.oktanativeauthdemo.network.model.AuthnResponse;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface OktaAuthService {
    @POST
    Single<AuthnResponse> login(@Url String url, @Body AuthnRequest authnRequest);

    @GET
    Single<Response<Object>> authorize(@Url String url,
                                       @Query("client_id") String clientId,
                                       @Query("response_type") String responseType,
                                       @Query("scope") String scope,
                                       @Query("redirect_uri") String redirectUri,
                                       @Query("state") String state,
                                       @Query("code_challenge_method") String codeChallengeMethod,
                                       @Query("code_challenge") String codeChallenge,
                                       @Query("sessionToken") String sessionToken
                                   );

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    Single<AccessTokenResponse> exchangeCode(@Url String url,
                                             @Query("grant_type") String grantType,
                                             @Query("client_id") String clientId,
                                             @Query("redirect_uri") String redirectUri,
                                             @Query("code") String code,
                                             @Query("code_verifier") String codeVerifier
                                      );

    @GET
    Single<ResponseBody> getUserInfo(@Url String url, @Header("Authorization") String access_token);
}
