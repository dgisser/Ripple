package com.facebook.peepingtom.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AccountActivity extends AppCompatActivity implements DatabaseLayer.MainUser {
    LoginButton loginButton;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    private int loopCount = 0;
    private FirebaseAnalytics mTracker;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    boolean userSet = false;
    FirebaseUser fbUser;
    public static final int LOGOUT_REQUEST_CODE = 420;
    public static final int RESET_COLORS_CODE = 1122;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        mTracker = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        //TODO persistence
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                fbUser = firebaseAuth.getCurrentUser();
                if (fbUser != null) {
                    // User is signed in
                    //TODO:create a new user in firebase
                    final JSONObject[] userJSON = new JSONObject[1];

                    for (UserInfo profile : fbUser.getProviderData()) {
                        // Id of the provider (ex: google.com)
                        String providerId = profile.getProviderId();

                        // UID specific to the provider
                        String uid = profile.getUid();

                        // Name, email address, and profile photo Url
                        String name = profile.getDisplayName();
                        Uri photoUri = profile.getPhotoUrl();
                        String photoUrl = "";
                        if (photoUri != null) photoUrl = photoUri.toString();

                        GraphRequest request = GraphRequest.newMeRequest(
                                accessToken,
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        userJSON[0] = object;

                                        // Application code
                                        try {
                                            //TODO make this update asynchronously and update user
                                            User user = ((GlobalVars) getApplication()).getUser();
                                            boolean updateUser = false;
                                            if (object != null && object.has("picture") ) {
                                                user.setProfileUrl(object.getJSONObject("picture")
                                                        .getJSONObject("data").getString("url"));
                                                updateUser = true;
                                            }
                                            if (object != null && object.getString("gender") != null &&
                                                                        user.gender == User.Gender.NONE) {
                                                user.setGenderString(object.getString("gender"));
                                                updateUser = true;
                                            }
                                            if (object != null && object.getJSONObject("age_range") != null
                                                                                            && user.age == 0) {
                                                if(user.birthDay != null)
                                                user.setAge((object.getJSONObject("age_range")).getInt("max"));
                                                updateUser = true;
                                            }
                                            if (object != null && object.getString("first_name") != null
                                                                                    && user.firstName.isEmpty()) {
                                                user.setFirstName((object.getString("first_name")));
                                                updateUser = true;
                                            }
                                            if (object != null && object.getString("last_name") != null
                                                                                && user.lastName.isEmpty()) {
                                                user.setLastName((object.getString("last_name")));
                                                updateUser = true;
                                            }
                                            //if the user vars were just updated and the user was already set
                                            //the database doesn't contain these fresh numbers
                                            //However, if the database hasn't been contacted yet,
                                            //whenever it is it will have the fresh numbers

                                            if (updateUser && userSet) DatabaseLayer.submitUser(user);
                                            else DatabaseLayer.updateUserProfile(user); //update current fb profile photo
                                            //object.getString("name");
                                            //object.getString("timezone");
                                        } catch(JSONException e){
                                            e.printStackTrace();
                                        }

                                    }
                                });
                        ((GlobalVars) getApplication()).setUser(new User (uid, "", "", photoUrl, "", 0, null,
                                new Date(), null, null, null, User.Region.NONE, User.CommunityDensity.NONE,
                                User.Gender.NONE, User.SexualOrientation.NONE, User.Religion.NONE, User.Color.PURPLE));
                        DatabaseLayer.getMainUser(AccountActivity.this, uid);//populates user with firebase data
                        loopCount ++;

                        Bundle parameters = new Bundle();
                        parameters.putString("fields","age_range, gender, first_name, last_name, picture.type(large)");
                        request.setParameters(parameters);
                        request.executeAsync();
                        //newUser.gender = userJSON.get;
                    }
                    //Log.d("AccountActivity", userJSON.toString());
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);

                    i.putExtra("user", Parcels.wrap(((GlobalVars) getApplication()).getUser()));
                    startActivity(i);
                    Log.d("AccountActivity", "onAuthStateChanged:signed_in:" + fbUser.getUid());
                } else
                    Log.d("AccountActivity", "onAuthStateChanged:signed_out");
            }
        };

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Sets the access token using
                // currentAccessToken when it's loaded or set.
            }

        };

        accessToken = AccessToken.getCurrentAccessToken();
        //gets the current access token, if there is none this prompts the user to login and then
        // starts intent to profile activity
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("AccountActivity", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("AccountActivity", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("AccountActivity", "facebook:onError", error);
                // ...
            }
        });

        //TODO make this update periodically
        File file = new File(getBaseContext().getFilesDir(), "questionslist.csv");
        if (!file.exists()) {
            try {
                DatabaseLayer.downloadQuestions(getApplicationContext());
            } catch (IOException e) {
                Log.d("accountactivity", e.toString());
            }
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("AccountActivity", "handleFacebookAccessToken: " + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("AccountActivity", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("AccountActivity", "signInWithCredential", task.getException());
                            Toast.makeText(AccountActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (resultCode == RESET_COLORS_CODE) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);

                i.putExtra("user", Parcels.wrap(((GlobalVars) getApplication()).getUser()));
                i.putExtra("changeTab", 1);
                startActivity(i);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void mainUser(User user) {
        User mainUser = ((GlobalVars) getApplication()).getUser();
        loopCount --;
        if (user == null && loopCount == 0) {
            userSet = true;
            DatabaseLayer.submitUser(mainUser);
        } else if (user != null){
            loopCount = -1;
            ((GlobalVars) getApplication()).setUser(user);
        }
    }
}