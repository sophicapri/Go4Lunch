package com.sophieopenclass.go4lunch.base;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookGraphResponseException;
import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.controllers.activities.WorkmateDetailActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.ListViewAdapter;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.injection.Injection;
import com.sophieopenclass.go4lunch.listeners.Listeners;
import com.sophieopenclass.go4lunch.utils.ViewModelFactory;

import javax.annotation.CheckForSigned;

import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;
import static com.sophieopenclass.go4lunch.utils.Constants.UID;

public abstract class BaseActivity<T extends ViewModel> extends AppCompatActivity implements Listeners.OnWorkmateClickListener, Listeners.OnRestaurantClickListener {
    public T viewModel;
    public static Location currentLocation = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setAdvertiserIDCollectionEnabled(false);
        FacebookSdk.setAutoLogAppEventsEnabled(false);
        configureViewModel();

        this.setContentView(this.getFragmentLayout());
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
        intent.putExtra(UID, uid);
        startActivity(intent);
    }
    @Override
    public void onRestaurantClick(String placeId) {
        Intent intent = new Intent(this, RestaurantDetailsActivity.class);
        intent.putExtra(PLACE_ID, placeId);
        startActivity(intent);
    }
}

