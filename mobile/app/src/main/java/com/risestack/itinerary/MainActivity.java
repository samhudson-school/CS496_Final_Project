package com.risestack.itinerary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.risestack.itinerary.MainApplication.LOG_TAG;

public class MainActivity extends AppCompatActivity {

    private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
    private static final String AUTH_STATE = "AUTH_STATE";
    private static final String USED_INTENT = "USED_INTENT";

    String USER_SUB_KEY = "";

    MainApplication mMainApplication;

    // state
    AuthState mAuthState;

    // views
    AppCompatButton mAuthorize;
    AppCompatButton mSignOut;
    AppCompatButton mCreateItinerary;
    AppCompatButton mViewItineraries;
    AppCompatTextView mHeadingText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainApplication = (MainApplication) getApplication();
        mHeadingText = (AppCompatTextView) findViewById(R.id.heading_message);
        mAuthorize = (AppCompatButton) findViewById(R.id.authorize);
        mSignOut = (AppCompatButton) findViewById(R.id.signOut);
        mCreateItinerary = (AppCompatButton) findViewById(R.id.create_itinerary);
        mViewItineraries = (AppCompatButton) findViewById(R.id.view_itineraries);

        enablePostAuthorizationFlows();



    }

    private void enablePostAuthorizationFlows() {
        mAuthState = restoreAuthState();
        // wire click listeners
        mAuthorize.setOnClickListener(new AuthorizeListener());
        if (mAuthState != null && mAuthState.isAuthorized()) {
            GetUserInfo(this, mAuthState, new AuthorizationService(this));
            if (mCreateItinerary.getVisibility() == View.GONE) {
                mCreateItinerary.setVisibility(View.VISIBLE);
                mCreateItinerary.setOnClickListener(new CreateItineraryListener(this, mAuthState, new AuthorizationService(this)));
            }
            if (mViewItineraries.getVisibility() == View.GONE) {
                mViewItineraries.setVisibility(View.VISIBLE);
                mViewItineraries.setOnClickListener(new ViewItinerariesListener(this, mAuthState, new AuthorizationService(this)));
            }
            if (mSignOut.getVisibility() == View.GONE) {
                mSignOut.setVisibility(View.VISIBLE);
                mSignOut.setOnClickListener(new SignOutListener(this));
            }
            if (mAuthorize.getVisibility() == View.VISIBLE) {
                mAuthorize.setVisibility(View.GONE);
            }


        } else {
            mAuthorize.setVisibility(View.VISIBLE);
            mCreateItinerary.setVisibility(View.GONE);
            mViewItineraries.setVisibility(View.GONE);
            mSignOut.setVisibility(View.GONE);
        }
    }

    /**
     * Exchanges the code, for the {@link TokenResponse}.
     *
     * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
     */
    private void handleAuthorizationResponse(@NonNull Intent intent) {

        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);
        if (response != null) {
            Log.i(LOG_TAG, String.format("Handled Authorization Response %s ", authState.toJsonString()));
            AuthorizationService service = new AuthorizationService(this);
            service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                    if (exception != null) {
                        Log.w(LOG_TAG, "Token Exchange failed", exception);
                    } else {
                        if (tokenResponse != null) {
                            authState.update(tokenResponse, exception);
                            persistAuthState(authState);
                            Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
                        }
                    }
                }
            });
        }



    }

    private void persistAuthState(@NonNull AuthState authState) {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString(AUTH_STATE, authState.toJsonString())
                .commit();
        enablePostAuthorizationFlows();
    }

    private void clearAuthState() {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(AUTH_STATE)
                .apply();
    }

    @Nullable
    private AuthState restoreAuthState() {
        String jsonString = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(AUTH_STATE, null);
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return AuthState.fromJson(jsonString);
            } catch (JSONException jsonException) {
                // should never happen
            }
        }
        return null;
    }

    /**
     * Kicks off the authorization flow.
     */
    public static class AuthorizeListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {

            AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                    Uri.parse("https://accounts.google.com/o/oauth2/v2/auth") /* auth endpoint */,
                    Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token endpoint */
            );

            String clientId = "534320268121-r9u9eu30918p83hine3pg4i9919visdt.apps.googleusercontent.com";
            Uri redirectUri = Uri.parse("com.risestack.itinerary:/oauth2callback");
            AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                    serviceConfiguration,
                    clientId,
                    AuthorizationRequest.RESPONSE_TYPE_CODE,
                    redirectUri
            );
            builder.setScopes("profile");
            AuthorizationRequest request = builder.build();

            AuthorizationService authorizationService = new AuthorizationService(view.getContext());
            String action = "com.risestack.itinerary.HANDLE_AUTHORIZATION_RESPONSE";
            Intent postAuthorizationIntent = new Intent(action);
            PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), request.hashCode(), postAuthorizationIntent, 0);
            authorizationService.performAuthorizationRequest(request, pendingIntent);

        }
    }

    public static class SignOutListener implements Button.OnClickListener {

        private final MainActivity mMainActivity;

        public SignOutListener(@NonNull MainActivity mainActivity) {
            mMainActivity = mainActivity;
        }

        @Override
        public void onClick(View view) {
            mMainActivity.mAuthState = null;
            mMainActivity.clearAuthState();
            mMainActivity.enablePostAuthorizationFlows();
            TextView mHeadingText = (TextView) mMainActivity.findViewById(R.id.heading_message);
            mHeadingText.setText(R.string.heading_message);
        }
    }
    public static class CreateItineraryListener implements Button.OnClickListener {

        private final MainActivity mMainActivity;
        private AuthState mAuthState;
        private AuthorizationService mAuthorizationService;

        public CreateItineraryListener(@NonNull MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
            mMainActivity = mainActivity;
            mAuthState = authState;
            mAuthorizationService = authorizationService;}

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mMainActivity, CreateItinerary.class);
            intent.putExtra("sub", mMainActivity.USER_SUB_KEY);

            mMainActivity.startActivity(intent);
        }
    }
    public static class ViewItinerariesListener implements Button.OnClickListener {

        private final MainActivity mMainActivity;
        private AuthState mAuthState;
        private AuthorizationService mAuthorizationService;

        public ViewItinerariesListener(@NonNull MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
            mMainActivity = mainActivity;
            mAuthState = authState;
            mAuthorizationService = authorizationService;}

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mMainActivity, ViewItineraries.class);
            intent.putExtra("sub", mMainActivity.USER_SUB_KEY);

            mMainActivity.startActivity(intent);
        }
    }

        public void GetUserInfo(@NonNull MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
            final MainActivity mMainActivity = mainActivity;
            AuthState mAuthState = authState;
            AuthorizationService mAuthorizationService = authorizationService;
            mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
                    new AsyncTask<String, Void, JSONObject>() {
                        @Override
                        protected JSONObject doInBackground(String... tokens) {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("https://www.googleapis.com/oauth2/v3/userinfo")
                                    .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                    .build();

                            try {
                                Response response = client.newCall(request).execute();
                                String jsonBody = response.body().string();
                                Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
                                return new JSONObject(jsonBody);
                            } catch (Exception exception) {
                                Log.w(LOG_TAG, exception);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(JSONObject userInfo) {
                            if (userInfo != null) {
                                    mMainActivity.USER_SUB_KEY = userInfo.optString("sub", null);
                                    String first_name = userInfo.optString("given_name", null);
                                    mMainActivity.mHeadingText.setText("Hello "+first_name+", What would you like to plan today?");

                            }
                        }
                    }.execute(accessToken);
                }
            });

        }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
    }

    private void checkIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "com.risestack.itinerary.HANDLE_AUTHORIZATION_RESPONSE":
                    if (!intent.hasExtra(USED_INTENT)) {
                        handleAuthorizationResponse(intent);
                        intent.putExtra(USED_INTENT, true);
                    }
                    break;
                default:
                    // do nothing
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIntent(getIntent());
    }
}
