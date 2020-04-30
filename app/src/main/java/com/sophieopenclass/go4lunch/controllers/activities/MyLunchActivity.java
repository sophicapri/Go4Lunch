package com.sophieopenclass.go4lunch.controllers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivityMyLunchBinding;

public class MyLunchActivity extends BaseActivity {

    public static Activity newInstance() {
        return new MyLunchActivity();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(ActivityMyLunchBinding.inflate(getLayoutInflater()).getRoot());
    }

    @Override
    public View getFragmentLayout() {
        return ActivityMyLunchBinding.inflate(getLayoutInflater()).getRoot();
    }
}
