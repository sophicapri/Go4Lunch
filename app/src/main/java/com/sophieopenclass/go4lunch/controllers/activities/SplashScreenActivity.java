package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends BaseActivity<MyViewModel> {
    @Override
    public View getFragmentLayout() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return ActivitySplashScreenBinding.inflate(getLayoutInflater()).getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(() -> {
            if (isCurrentUserLogged()) {
                startMainActivity();
            } else
                startLoginActivity();
        }, 1000);
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