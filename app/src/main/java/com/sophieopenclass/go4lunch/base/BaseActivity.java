package com.sophieopenclass.go4lunch.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.utils.ViewModelFactory;
import com.sophieopenclass.go4lunch.injection.Injection;

public abstract class BaseActivity extends AppCompatActivity {
    public MyViewModel viewModel;
    private static final String USER_COLLECTION_NAME = "users";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureViewModel();

        this.setContentView(this.getFragmentLayout());
    }

    protected void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(USER_COLLECTION_NAME);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MyViewModel.class);
        Injection.setViewModel(viewModel);
    }

    protected abstract View getFragmentLayout();

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener() {
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    protected Boolean isCurrentUserLogged() {
        return (this.getCurrentUser() != null);
    }
}

