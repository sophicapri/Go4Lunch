package com.sophieopenclass.go4lunch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class WorkmatesListFragment extends Fragment{

    public static Fragment newInstance() {
        return new WorkmatesListFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workmates_list, container, false);
    }
}
