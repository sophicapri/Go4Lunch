package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivityLoginBinding;
import com.sophieopenclass.go4lunch.models.User;

import java.util.Collections;


public class LoginActivity extends BaseActivity<MyViewModel> {
    private static final int RC_SIGN_IN = 124;
    private ActivityLoginBinding binding;

    @Override
    public View getLayout() {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding.facebookSignInBtn.setOnClickListener(v -> startSignInWithFacebook());
        binding.googleSignInBtn.setOnClickListener(v -> startSignInWithGoogle());
    }

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    public void startSignInWithGoogle() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build())) //GOOGLE
                .setIsSmartLockEnabled(false, true)
                .build(), RC_SIGN_IN);
    }

    public void startSignInWithFacebook() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.FacebookBuilder().build()))
                .setTheme(R.style.com_facebook_auth_dialog)// FACEBOOK
                .setIsSmartLockEnabled(false, true)
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
                    checkIfUserExistInFirestore();
            } else { // ERRORS
                if (response != null && response.getError() != null) {
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                    } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        Toast.makeText(this, getString(R.string.error_unknown_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void checkIfUserExistInFirestore() {
        if (getCurrentUser()!= null)
            viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
                if (user == null)
                    createUserInFirestore();
                else
                    startMainActivity();
            });
    }

    private void createUserInFirestore() {
        if (this.getCurrentUser() != null) {
            String urlPicture = (getCurrentUser().getPhotoUrl() != null) ? getCurrentUser().getPhotoUrl().toString() : null;
            String username = getCurrentUser().getDisplayName();
            String uid = getCurrentUser().getUid();
            String email = getCurrentUser().getEmail();
            User currentUser = new User(uid, username, urlPicture, email);

            viewModel.createUser(currentUser);
            viewModel.getCreatedUserLiveData().observe(this, user -> {
                if (user == null)
                    onFailureListener();
                else
                    startMainActivity();
            });
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
