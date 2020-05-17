package com.sophieopenclass.go4lunch.controllers.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.ChatViewAdapter;
import com.sophieopenclass.go4lunch.databinding.ActivityChatBinding;
import com.sophieopenclass.go4lunch.models.Message;
import com.sophieopenclass.go4lunch.models.User;

import static com.sophieopenclass.go4lunch.utils.Constants.UID;

public class ChatActivity extends BaseActivity<MyViewModel> implements ChatViewAdapter.Listener {
    private ActivityChatBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private User currentUser;

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    protected View getFragmentLayout() {
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null && getIntent().hasExtra(UID)) {
            String uid = (String) getIntent().getExtras().get(UID);
            viewModel.getUser(uid).observe(this, this::initUI);
        }

        if (getCurrentUser() != null) {
            viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
                updateRecyclerView(user);
                currentUser = user;
            });
        }
    }

    private void initUI(User user) {
        binding.workmateName.setText(user.getUsername());
        Glide.with(binding.workmateProfilePic.getContext())
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.workmateProfilePic);

        binding.sendMessageButton.setOnClickListener(v -> onSendMessageClick());
    }

    private void onSendMessageClick() {
        if (!TextUtils.isEmpty(binding.messageEditText.getText()) && currentUser.getUid() != null) {
            // Check if the ImageView is set
            if (binding.chatImageChosenPreview.getDrawable() == null) {
                // SEND A TEXT MESSAGE
                viewModel.createMessageForChat(binding.messageEditText.getText().toString(), currentUser)
                        .observe(this, message -> {
                            if (message == null)
                                Toast.makeText(this, "une erreur est survenue", Toast.LENGTH_LONG).show();
                        });
            } /*else {
                /*
                // SEND A IMAGE + TEXT IMAGE
                this.uploadPhotoInFirebaseAndSendMessage(editTextMessage.getText().toString());
                this.editTextMessage.setText("");
                this.imageViewPreview.setImageDrawable(null);
            }
            */
        }
    }

    private void updateRecyclerView(User currentUser) {
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(viewModel.getAllMessages(), Message.class)
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
}
