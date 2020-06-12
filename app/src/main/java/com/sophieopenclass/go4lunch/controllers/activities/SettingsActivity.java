package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sophieopenclass.go4lunch.AppController;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySettingsBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.utils.PreferenceHelper;

import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.sophieopenclass.go4lunch.utils.Constants.ENGLISH_LOCALE;
import static com.sophieopenclass.go4lunch.utils.Constants.FRENCH_LOCALE;
import static com.sophieopenclass.go4lunch.utils.Constants.STORAGE_PERMS;

public class SettingsActivity extends BaseActivity<MyViewModel> {
    public static boolean localeHasChanged = false;
    public static boolean profileHasChanged = false;
    private User currentUser;
    private Uri uriImageSelected;
    private ImageView imageViewDialog;
    private ActivitySettingsBinding binding;
    private String currentAppLocale = PreferenceHelper.getCurrentLocale();

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    public View getLayout() {
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!PreferenceHelper.getReminderPreference())
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
        PreferenceHelper.setReminderPreference(false);
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

        displayCurrentLocale();

        binding.updateProfilePic.setOnClickListener(v -> updateImageDialog());

        binding.deleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        binding.settingsToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_account)
                .setPositiveButton(R.string.delete, (paramDialogInterface, paramInt) -> {
                    viewModel.deleteUserMessages(currentUser.getUid());
                    viewModel.deleteUser(currentUser.getUid());
                    Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
                    deleteAccount();
                })
                .setNegativeButton(R.string.Cancel, null)
                .show();
    }

    public void deleteAccount() {
        AuthUI.getInstance().delete(this).addOnSuccessListener(v -> backToLoginPage());
    }

    private void displayCurrentLocale() {
        if (currentAppLocale.equals(FRENCH_LOCALE))
            binding.currentLocale.setText(R.string.french_locale);
        else
            binding.currentLocale.setText(R.string.english_locale);
    }

    private void updateImageDialog() {
        AlertDialog alertDialogImage = new AlertDialog.Builder(this)
                .setView(R.layout.alert_dialog_profile_pic)
                .setPositiveButton(R.string.save, (dialog, which) -> savePictureToFirestore())
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
        if (!locale.equals(PreferenceHelper.getCurrentLocale())) {
            PreferenceHelper.setCurrentLocale(locale);
            localeHasChanged = true;
            AppController.getInstance().updateLocale();
            refreshActivity();
            Toast.makeText(this, R.string.locale_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    private void chooseImageFromPhone() {
        if (!EasyPermissions.hasPermissions(this, STORAGE_PERMS)) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_RC);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleResponse(requestCode, resultCode, data);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        super.onPermissionsGranted(requestCode, perms);
        if (requestCode == READ_STORAGE_RC)
            chooseImageFromPhone();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        super.onPermissionsDenied(requestCode, perms);
        Snackbar.make(binding.getRoot(), R.string.photo_access_declined, BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setDuration(5000).show();
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

    private void savePictureToFirestore() {
        if (uriImageSelected != null) {
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
                                                uriImageSelected = null;
                                            }
                                        });
                            }));
        }
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