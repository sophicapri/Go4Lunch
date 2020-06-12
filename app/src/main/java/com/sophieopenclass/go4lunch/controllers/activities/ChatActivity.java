package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sophieopenclass.go4lunch.view_models.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.ChatViewAdapter;
import com.sophieopenclass.go4lunch.databinding.ActivityChatBinding;
import com.sophieopenclass.go4lunch.models.Message;
import com.sophieopenclass.go4lunch.models.User;

import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.EXTRA_UID;
import static com.sophieopenclass.go4lunch.utils.Constants.STORAGE_PERMS;

public class ChatActivity extends BaseActivity<MyViewModel> implements ChatViewAdapter.Listener  {
    private FirestoreRecyclerAdapter adapter;
    private String currentUserId;
    private String workmateId;
    private String chatId;
    private User currentUser;
    private Uri uriImageSelected;
    private ActivityChatBinding binding;


    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    protected View getLayout() {
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null && getIntent().hasExtra(EXTRA_UID)) {
            workmateId = (String) getIntent().getExtras().get(EXTRA_UID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (networkUnavailable()) {
            Snackbar.make(binding.getRoot(), R.string.internet_unavailable, BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
        } else if (getCurrentUser() != null) {
            viewModel.getUser(workmateId).observe(this, this::initUI);
            viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
                currentUser = user;
                currentUserId = user.getUid();
                viewModel.getChatId(user.getUid(), workmateId).observe(this, idChat -> {
                    if (idChat != null) {
                        updateRecyclerView(user, idChat);
                        chatId = idChat;
                    }
                });
            });
        }
    }

    private void initUI(User user) {
        binding.workmateName.setText(user.getUsername());
        binding.emailAddress.setText(user.getEmail());
        Glide.with(binding.workmateProfilePic.getContext())
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.workmateProfilePic);

        binding.chatAddFileButton.setOnClickListener(v -> chooseImageFromPhone());
        binding.sendMessageButton.setOnClickListener(v -> onSendMessageClick());
        binding.backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void onSendMessageClick() {
        if (!TextUtils.isEmpty(binding.messageEditText.getText()) && currentUserId != null) {
            if (chatId == null)
                createChatAndSendMessage();
             else
                sendMessage(chatId);
        }
    }

    private void createChatAndSendMessage() {
        viewModel.createChat(currentUserId, workmateId).observe(this, chatCreationSuccessful -> {
            if (chatCreationSuccessful)
                viewModel.getChatId(currentUserId, workmateId).observe(this, this::sendMessage);
            else
                Toast.makeText(this, R.string.error_sending_message, Toast.LENGTH_LONG).show();
        });
    }

    public void sendMessage(@NonNull String chatId) {
        // Check if the ImageView is set
        if (binding.chatImageChosenPreview.getDrawable() == null) {
            // SEND A TEXT MESSAGE
            viewModel.createMessageForChat(binding.messageEditText.getText().toString(), currentUserId, chatId)
                    .observe(this, message -> {
                        if (this.chatId == null) {
                            this.chatId = chatId;
                            updateRecyclerView(currentUser, chatId);
                        }
                        if (message == null)
                            Toast.makeText(this, R.string.error_sending_message, Toast.LENGTH_LONG).show();
                    });
        } else {
            // SEND AN IMAGE + TEXT IMAGE
            uploadPhotoInFirebaseAndSendMessage(binding.messageEditText.getText().toString(), chatId);
            binding.chatImageChosenPreview.setImageDrawable(null);
        }

        binding.messageEditText.getText().clear();
    }

    private void uploadPhotoInFirebaseAndSendMessage(String message, String chatId) {
        String uuid = UUID.randomUUID().toString(); // GENERATE UNIQUE STRING
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(uuid);
        mImageRef.putFile(uriImageSelected)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String pathImageSavedInFirebase = uri.toString();
                            viewModel.createMessageWithImageForChat(pathImageSavedInFirebase, message, currentUserId, chatId)
                                    .observe(ChatActivity.this, message1 -> {
                                        if (ChatActivity.this.chatId == null) {
                                            ChatActivity.this.chatId = chatId;
                                            // to init a new chat session
                                            ChatActivity.this.updateRecyclerView(currentUser, chatId);
                                        }
                                        if (message1 == null)
                                            Toast.makeText(ChatActivity.this, R.string.error_sending_message, Toast.LENGTH_LONG).show();
                                    });
                        }).addOnFailureListener(onFailureListener())).addOnFailureListener(onFailureListener());
    }

    private void updateRecyclerView(User currentUser, String chatId) {
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(viewModel.getMessagesQuery(chatId), Message.class)
                .build();

        adapter = new ChatViewAdapter(options, currentUser.getUid(), Glide.with(this), this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                binding.chatRecyclerView.smoothScrollToPosition(adapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        binding.chatRecyclerView.setHasFixedSize(true);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onDataChanged() {
        binding.chatTextViewRecyclerViewEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    // --------------------
    // FILE MANAGEMENT
    // --------------------

    private void chooseImageFromPhone() {
        if (!EasyPermissions.hasPermissions(this, STORAGE_PERMS)) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_RC);
            return;
        }
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
                Glide.with(this) //SHOWING PREVIEW OF IMAGE
                        .load(uriImageSelected)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.chatImageChosenPreview);
            } else {
                Toast.makeText(this, getString(R.string.toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
            }
        }
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
}
