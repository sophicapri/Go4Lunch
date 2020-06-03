package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySettingsBinding;
import com.sophieopenclass.go4lunch.models.User;

import java.util.Locale;
import java.util.UUID;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.sophieopenclass.go4lunch.controllers.activities.ChatActivity.RC_CHOOSE_PHOTO;
import static com.sophieopenclass.go4lunch.controllers.activities.ChatActivity.READ_STORAGE_RC;
import static com.sophieopenclass.go4lunch.utils.Constants.ENGLISH_LOCALE;
import static com.sophieopenclass.go4lunch.utils.Constants.FRENCH_LOCALE;
import static com.sophieopenclass.go4lunch.utils.Constants.PREF_LANGUAGE;
import static com.sophieopenclass.go4lunch.utils.Constants.PREF_REMINDER;

public class SettingsActivity extends BaseActivity<MyViewModel> {
    private static final String TAG = "SettingsActivity";
    private ActivitySettingsBinding binding;
    public static boolean localeHasChanged = false;
    public static boolean profileHasChanged = false;
    private User currentUser;
    private  Uri uriImageSelected;
    private ImageView imageViewDialog;
    private String currentAppLocale = sharedPrefs.getString(PREF_LANGUAGE, Locale.getDefault().getLanguage());

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    public View getFragmentLayout() {
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!sharedPrefs.getBoolean(PREF_REMINDER, false))
            binding.notificationToggle.setChecked(false);

        binding.notificationToggle.setOnClickListener(v -> {
            if (binding.notificationToggle.isChecked()) {
                activateReminder();
                Toast.makeText(this, R.string.reminder_activated, Toast.LENGTH_LONG).show();
            } else {
                cancelReminder();
            }
        });

        binding.containerLanguageSettings.setOnClickListener(v -> openPopupMenuLocales());
    }

    private void cancelReminder() {
        workManager.cancelAllWork();
        sharedPrefs.edit().putBoolean(PREF_REMINDER, false).apply();
        Toast.makeText(this, R.string.reminder_disabled, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (networkUnavailable()) {
            Snackbar.make(binding.getRoot(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setDuration(5000).setTextColor(getResources().getColor(R.color.quantum_white_100)).show();
        } else if (getCurrentUser() != null)
            viewModel.getUser(getCurrentUser().getUid()).observe(this, this::initUI);
    }

    private void initUI(User user) {
        currentUser = user;
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        binding.usernameDisplayed.setText(user.getUsername());
        binding.editTextUsername.setText(user.getUsername());
        binding.editUsername.setOnClickListener(v -> {
            binding.editUsernameContainer.setVisibility(View.VISIBLE);
            binding.editTextUsername.requestFocus();
            binding.editTextUsername.setSelection(user.getUsername().length());
            // show keyboard
            if (inputManager != null)
                inputManager.showSoftInput(binding.editTextUsername, InputMethodManager.SHOW_IMPLICIT);
        });

        binding.cancelUsernameUpdate.setOnClickListener(v -> {
            binding.editUsernameContainer.setVisibility(View.GONE);
            // Hide keyboard
            if (inputManager != null) {
                binding.editTextUsername.setText(user.getUsername());
                inputManager.hideSoftInputFromWindow(binding.editTextUsername.getWindowToken(), 0);
            }
        });

        Glide.with(binding.updateProfilePic.getContext())
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.updateProfilePic);

        binding.saveUsername.setOnClickListener(v -> {
            if (!binding.editTextUsername.getText().toString().isEmpty())
                saveUsername(binding.editTextUsername.getText().toString());
            else
                binding.editTextUsername.setError(getString(R.string.empty_field));
        });

        if (currentAppLocale.equals(FRENCH_LOCALE))
        binding.currentLocale.setText(R.string.french_locale);
        else
            binding.currentLocale.setText(R.string.english_locale);

        binding.updateProfilePic.setOnClickListener(v -> updateImageDialog());

        binding.deleteAccount.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                .setMessage(R.string.delete_account)
                .setPositiveButton(R.string.delete, (paramDialogInterface, paramInt) -> {
                    viewModel.deleteUserMessages(currentUser.getUid());
                    viewModel.deleteUser(currentUser.getUid());
                    Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
                    deleteAccount();
                })
                .setNegativeButton(R.string.Cancel, null)
                .show());

        binding.settingsToolbar.setNavigationOnClickListener( v -> onBackPressed());
    }

    private void updateImageDialog() {
        AlertDialog alertDialogImage = new AlertDialog.Builder(this)
                .setView(R.layout.alert_dialog_profile_pic)
                .setPositiveButton(R.string.save, (dialog, which) -> savePictureToFirestore(uriImageSelected))
                .setNegativeButton(R.string.Cancel, null)
                .show();

        Button chooseImageBtn = alertDialogImage.findViewById(R.id.choose_image_btn);
        if (chooseImageBtn != null) {
            chooseImageBtn.setOnClickListener(v -> chooseImageFromPhone());
        }

        imageViewDialog = alertDialogImage.findViewById(R.id.dialog_profile_pic);
        Glide.with(this)
                .load(currentUser.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(imageViewDialog);
    }

    private void saveUsername(String username) {
        binding.usernameDisplayed.setText(username);
        viewModel.updateUsername(username, currentUser.getUid());
        profileHasChanged = true;
        binding.editUsernameContainer.setVisibility(View.GONE);
    }

    private void changeAppLanguage(String locale) {
        if (!locale.equals(sharedPrefs.getString(PREF_LANGUAGE, Locale.getDefault().getLanguage()))) {
            sharedPrefs.edit().putString(PREF_LANGUAGE, locale).apply();
            localeHasChanged = true;
            updateLocale();
            refreshActivity();
            Toast.makeText(this, R.string.locale_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshActivity() {
        finish();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void chooseImageFromPhone() {
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_RC);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        if (requestCode == READ_STORAGE_RC) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImageFromPhone();
            } else {
                Snackbar.make(binding.getRoot(), R.string.photo_access_declined, BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setDuration(5000).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleResponse(requestCode, resultCode, data);
    }

    private void handleResponse(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                uriImageSelected = data.getData();
                if (imageViewDialog != null) {
                    Glide.with(this) //SHOWING PREVIEW OF IMAGE
                            .load(uriImageSelected)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageViewDialog);
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savePictureToFirestore(Uri uriImageSelected) {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uuid = UUID.randomUUID().toString(); // GENERATE UNIQUE STRING
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(uuid);
        mImageRef.putFile(uriImageSelected)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String pathImageSavedInFirebase = uri.toString();
                            viewModel.updateUrlPicture(pathImageSavedInFirebase, currentUser.getUid())
                                    .observe(this, urlPicture -> {
                                        if (urlPicture != null) {
                                            refreshActivity();
                                            profileHasChanged = true;
                                            binding.progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }));
    }

    public void openPopupMenuLocales() {
        PopupMenu popupMenu = new PopupMenu(this, binding.currentLocale);
        popupMenu.getMenuInflater().inflate(R.menu.pop_up_menu_languages, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.french_locale:
                    if (!currentAppLocale.equals(FRENCH_LOCALE))
                        changeAppLanguage(FRENCH_LOCALE);
                    return true;

                case R.id.english_locale:
                    if (!currentAppLocale.equals(ENGLISH_LOCALE))
                        changeAppLanguage(ENGLISH_LOCALE);
                    return true;
            }
            return true;
        });
        popupMenu.show();

    }
}