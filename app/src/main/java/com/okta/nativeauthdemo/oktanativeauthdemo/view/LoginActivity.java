package com.okta.nativeauthdemo.oktanativeauthdemo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.okta.appauth.android.OAuthClientConfiguration;
import com.okta.appauth.android.OktaAppAuth;
import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.authn.sdk.resource.AuthenticationResponse;
import com.okta.nativeauthdemo.oktanativeauthdemo.R;
import com.okta.nativeauthdemo.oktanativeauthdemo.di.ServiceLocator;
import com.okta.nativeauthdemo.oktanativeauthdemo.legacy.CustomOktaAppAuth;

import net.openid.appauth.AuthorizationException;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CustomOktaAppAuth oktaAppAuth;
    private boolean useOktaNativeSdk = true;

    private final String USERNAME = "";
    private final String PASSWORD = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mEmailView.setText(USERNAME);
        mPasswordView.setText(PASSWORD);


        // Init configuration from json file in raw folder
        if(!useOktaNativeSdk) {
            this.oktaAppAuth = ServiceLocator.getInstance().getCustomOktaAppAuth(this);
            initializeOktaAuth();
        }
    }

    private void initializeOktaAuth() {
        showProgress(true);

        this.oktaAppAuth.init(
                this,
                new OktaAppAuth.OktaAuthListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                            }
                        });
                    }

                    @Override
                    public void onTokenFailure(@NonNull AuthorizationException ex) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                showToast("Init error");
                            }
                        });
                    }
                },
                getResources().getColor(R.color.colorPrimary));
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            if(useOktaNativeSdk) {
                processAuthenticationNativeOkta(email, password);
            } else {
                processAuthentication(email, password);
            }

        }
    }

    private void processAuthenticationNativeOkta(final String email, final String password) {
//        showProgress(true);

//        try {
//            AuthenticationClient client = AuthenticationClients.builder()
//                    .setOrgUrl("https://lohika-um.oktapreview.com")
//                    .build();
//
//            client.authenticate(email, password.toCharArray(), null, new AuthenticationStateHandlerAdapter() {
//                @Override
//                public void handleUnknown(AuthenticationResponse unknownResponse) {
////                    emitter.onSuccess(false);
//                }
//
//                @Override
//                public void handleSuccess(AuthenticationResponse successResponse) {
//                    String token = successResponse.getSessionToken();
////                    emitter.onSuccess(true);
//                }
//            });
//        }catch (Exception e) {
//            e.printStackTrace();
//        }

        Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final SingleEmitter<Boolean> emitter) throws Exception {
                try {
                    AuthenticationClient client = AuthenticationClients.builder()
                            .setOrgUrl("https://lohika-um.oktapreview.com")
                            .build();

                    client.authenticate(email, password.toCharArray(), null, new AuthenticationStateHandlerAdapter() {
                        @Override
                        public void handleUnknown(AuthenticationResponse unknownResponse) {
                            emitter.onSuccess(false);
                        }

                        @Override
                        public void handleSuccess(AuthenticationResponse successResponse) {
                            String token = successResponse.getSessionToken();
                            emitter.onSuccess(true);
                        }
                    });
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    private void processAuthentication(String login, String password) {
        if(oktaAppAuth.getConfig() == null || !oktaAppAuth.getConfig().isValid()) {
            showToast("Configuration isn't valid");
            return;
        }

        showProgress(true);
        compositeDisposable.add(ServiceLocator.getInstance()
                .getAuthNativeFlowInteractor(oktaAppAuth.getConfig()).login(login, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String accessToken) throws Exception {
                        showProgress(false);
                        showUserInfoScreen(accessToken);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        showProgress(false);
                        showToast(throwable.getMessage());
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.dispose();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @MainThread
    private void showUserInfoScreen(String accessToken) {
        finish();
        startActivity(UserInfoActivity.createIntent(this, accessToken));
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @MainThread
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

