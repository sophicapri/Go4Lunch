package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends BaseActivity<MyViewModel> {
    @Override
    public View getFragmentLayout() {
        return ActivitySplashScreenBinding.inflate(getLayoutInflater()).getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isCurrentUserLogged())
            startMainActivity();
        else
            startLoginActivity();
    }

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginPageActivity.class);
        startActivity(intent);
        finish();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}