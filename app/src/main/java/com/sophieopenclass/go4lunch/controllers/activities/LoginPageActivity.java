package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.api.UserHelper;
import com.sophieopenclass.go4lunch.databinding.ActivityLoginBinding;
import java.util.Arrays;


public class LoginPageActivity extends BaseActivity {
    private static final int RC_SIGN_IN = 124;

    @Override
    public View getFragmentLayout(){
        return ActivityLoginBinding.inflate(getLayoutInflater()).getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isCurrentUserLogged()) {
            startMainActivity();
        } else
            startSignInActivity();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void startSignInActivity() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(
                        Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(), //GOOGLE
                               new AuthUI.IdpConfig.FacebookBuilder().build()))// FACEBOOK
                .setIsSmartLockEnabled(false, true)
                //.setLogo(R.drawable.ic_logo_auth)
                .build(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK)
                handleResponseAfterSignIn(requestCode, resultCode, data);
        }
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                createUserInFirestore();
                startMainActivity();
            } else { // ERRORS
                startSignInActivity();
                if (response != null && response.getError() != null)
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                    } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        Toast.makeText(this, getString(R.string.error_unknown_error), Toast.LENGTH_SHORT).show();
                    }
            }
        }
    }

    private void createUserInFirestore() {
        if (this.getCurrentUser() != null) {
            viewModel.createUser(getCurrentUser()).addOnFailureListener(onFailureListener());
        }
    }
}
