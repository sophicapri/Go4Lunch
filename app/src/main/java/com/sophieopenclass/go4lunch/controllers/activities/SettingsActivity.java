package com.sophieopenclass.go4lunch.controllers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BaseActivity <MyViewModel> {

    public static Activity newInstance() {
        return new SettingsActivity();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(ActivitySettingsBinding.inflate(getLayoutInflater()).getRoot());
    }

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    public View getFragmentLayout() {
        return ActivitySettingsBinding.inflate(getLayoutInflater()).getRoot();
    }
}
