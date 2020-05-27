package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;

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
import static com.sophieopenclass.go4lunch.utils.Constants.PREF_LANGUAGE;
import static com.sophieopenclass.go4lunch.utils.Constants.PREF_REMINDER;

public class SettingsActivity extends BaseActivity<MyViewModel> {
    private static final String TAG = "SettingsActivity";
    private ActivitySettingsBinding binding;
    public static boolean localeHasChanged = false;
    public static boolean profileHasChanged = false;
    private User currentUser;
    private EditText usernameInput;

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
            if (binding.notificationToggle.isChecked())
                activateReminder();
            else {
                cancelReminder();
            }
        });

        binding.appLocale.setOnClickListener(v -> changeAppLanguage("fr"));
        binding.appLocaleEn.setOnClickListener(v -> changeAppLanguage("en"));
    }

    private void cancelReminder() {
        workManager.cancelAllWork();
        sharedPrefs.edit().putBoolean(PREF_REMINDER, false).apply();
        Log.i(TAG, "cancelReminder: ");
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
        binding.settingsUsername.setText(user.getUsername());
        binding.settingsUsername.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            AlertDialog alertDialog = alertDialogBuilder.setView(R.layout.alert_dialog_username)
                    .setPositiveButton("Enregistrer", null)
                    .create();

            alertDialog.setOnDismissListener(dialog -> usernameInput.clearComposingText());
            alertDialog.setOnShowListener(dialog -> {
                usernameInput = alertDialog.findViewById(R.id.new_username);
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(v1 -> saveUsername(alertDialog));
            });
            alertDialog.show();
        });

        Glide.with(binding.profilePic.getContext())
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profilePic);

        binding.profilePic.setOnClickListener(v -> chooseImageFromPhone());

        binding.deleteAccount.setOnClickListener(v -> {
            viewModel.deleteUserMessages(currentUser.getUid());
            viewModel.deleteUser(currentUser.getUid());
            Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show();
            deleteAccount();
        });
    }

    private void saveUsername(AlertDialog alertDialog) {
        String username = "";
        if (usernameInput != null && !usernameInput.getText().toString().isEmpty()) {
            username = usernameInput.getText().toString();
            binding.settingsUsername.setText(username);
            viewModel.updateUsername(username, currentUser.getUid());
            profileHasChanged = true;
        }
        alertDialog.hide();
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
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
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
                Log.i(TAG, "handleResponse: ");
                binding.progressBar.setVisibility(View.VISIBLE);
                Uri uriImageSelected = data.getData();
                Glide.with(this) //SHOWING PREVIEW OF IMAGE
                        .load(uriImageSelected)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.profilePic);
                addPictureToFirestore(uriImageSelected);
            } else {
                Toast.makeText(this, getString(R.string.toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addPictureToFirestore(Uri uriImageSelected) {
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
}