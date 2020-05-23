package com.sophieopenclass.go4lunch.base;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.FacebookSdk;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.controllers.activities.WorkmateDetailActivity;
import com.sophieopenclass.go4lunch.injection.Injection;
import com.sophieopenclass.go4lunch.listeners.Listeners;
import com.sophieopenclass.go4lunch.utils.ViewModelFactory;

import static android.content.Intent.EXTRA_UID;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;

public abstract class BaseActivity<T extends ViewModel> extends AppCompatActivity implements Listeners.OnWorkmateClickListener, Listeners.OnRestaurantClickListener {
    public T viewModel;
    public static Location currentLocation = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(this.getFragmentLayout());
        configureViewModel();
    }

    @SuppressWarnings("unchecked")
    protected void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory();
        viewModel = (T) new ViewModelProvider(this, viewModelFactory).get(getViewModelClass());
    }

    public abstract Class getViewModelClass();

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

    public T getViewModel() {
        return viewModel;
    }

    @Override
    public void onWorkmateClick(String uid) {
        Intent intent = new Intent(this, WorkmateDetailActivity.class);
        intent.putExtra(EXTRA_UID, uid);
        startActivity(intent);
    }
    @Override
    public void onRestaurantClick(String placeId) {
        Intent intent = new Intent(this, RestaurantDetailsActivity.class);
        intent.putExtra(PLACE_ID, placeId);
        startActivity(intent);
    }
}

