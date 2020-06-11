package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends BaseActivity<MyViewModel, ActivitySplashScreenBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_splash_screen;
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
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}