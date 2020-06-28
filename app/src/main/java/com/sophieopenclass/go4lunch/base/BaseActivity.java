package com.sophieopenclass.go4lunch.base;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.AppController;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.controllers.activities.LoginActivity;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.controllers.activities.UserDetailActivity;
import com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment;
import com.sophieopenclass.go4lunch.controllers.fragments.RestaurantListFragment;
import com.sophieopenclass.go4lunch.injection.Injection;
import com.sophieopenclass.go4lunch.listeners.Listeners;
import com.sophieopenclass.go4lunch.notifications.NotificationWorker;
import com.sophieopenclass.go4lunch.utils.PreferenceHelper;
import com.sophieopenclass.go4lunch.utils.ViewModelFactory;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Intent.EXTRA_UID;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_MAP_VIEW;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_RESTAURANT_LIST_VIEW;
import static com.sophieopenclass.go4lunch.utils.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.sophieopenclass.go4lunch.utils.Constants.LOCATION_REQUEST_CODE;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;
import static com.sophieopenclass.go4lunch.utils.Constants.WORK_REQUEST_NAME;

public abstract class BaseActivity<T extends ViewModel> extends AppCompatActivity implements Listeners.OnWorkmateClickListener,
        Listeners.OnRestaurantClickListener, EasyPermissions.PermissionCallbacks {
    public T viewModel;
    public boolean restartState = false;
    public boolean orientationChanged = false;
    public final WorkManager workManager = WorkManager.getInstance(this);
    private LocationManager locationManager;
    private MapViewFragment mapFragment;
    private RestaurantListFragment restaurantListFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getInstance().checkCurrentLocale(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        FacebookSdk.setAdvertiserIDCollectionEnabled(false);
        FacebookSdk.setAutoLogAppEventsEnabled(false);
        configureViewModel();
        setContentView(this.getLayout());
    }

    @SuppressWarnings("unchecked")
    protected void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory();
        viewModel = (T) new ViewModelProvider(this, viewModelFactory).get(getViewModelClass());
    }

    protected abstract Class getViewModelClass();

    protected abstract View getLayout();



    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener() {
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }

    // ---------
    // ACCESS TO INTERNET && LOCATION CHECKS
    // ---------------

    public boolean networkUnavailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo == null || !activeNetworkInfo.isConnected();
    }

    public boolean requestLocationAccess() {
        mapFragment = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_MAP_VIEW);
        restaurantListFragment = (RestaurantListFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_RESTAURANT_LIST_VIEW);

        boolean locationAvailable = false;
        String PERMS = ACCESS_FINE_LOCATION;
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.app_requires_location),
                    LOCATION_PERMISSION_REQUEST_CODE, PERMS);
        } else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.open_location_settings, (paramDialogInterface, paramInt) ->
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE))
                    .setNegativeButton(R.string.Cancel, null)
                    .show();
        } else
            locationAvailable = true;
        return locationAvailable;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_CODE)
            if (resultCode == RESULT_OK && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    onLocationAccessGranted();

    }

    private void onLocationAccessGranted() {
        restartState = true;
        if (mapFragment != null && mapFragment.isVisible())
            mapFragment.onPermissionsGranted();
        else if (restaurantListFragment != null && restaurantListFragment.isVisible())
            restaurantListFragment.onPermissionsGranted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
            onLocationAccessGranted();
    }

    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (mapFragment != null && mapFragment.isVisible())
            mapFragment.onPermissionsDenied();
        else if (restaurantListFragment != null && restaurantListFragment.isVisible())
            restaurantListFragment.onPermissionsDenied();
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

    public void activateReminder() {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        // Set execution time of the reminder
        dueDate.set(Calendar.HOUR_OF_DAY, 12);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        dueDate.set(Calendar.MILLISECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.DAY_OF_MONTH, 1);
        }
        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        Data userId = new Data.Builder().build();
        if (getCurrentUser() != null)
            userId = new Data.Builder()
                    .putString(EXTRA_UID, getCurrentUser().getUid())
                    .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 1,
                TimeUnit.DAYS)
                .setInputData(userId)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueueUniquePeriodicWork(WORK_REQUEST_NAME, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
        PreferenceHelper.setReminderPreference(true);
    }

    public void cancelReminder() {
        workManager.cancelAllWork();
        PreferenceHelper.setReminderPreference(false);
        Toast.makeText(this, R.string.reminder_disabled, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWorkmateClick(String uid) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra(EXTRA_UID, uid);
        startActivity(intent);
    }

    @Override
    public void onRestaurantClick(String placeId) {
        Intent intent = new Intent(this, RestaurantDetailsActivity.class);
        intent.putExtra(PLACE_ID, placeId);
        startActivity(intent);
    }

    protected void backToLoginPage() {
        finishAffinity();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        workManager.cancelAllWork();
    }
}



