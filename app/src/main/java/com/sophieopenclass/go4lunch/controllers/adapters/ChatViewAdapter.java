package com.sophieopenclass.go4lunch.controllers.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.controllers.activities.ChatActivity;
import com.sophieopenclass.go4lunch.databinding.ChatActivityMessageItemBinding;
import com.sophieopenclass.go4lunch.models.Message;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.utils.MyFirestoreRecyclerAdapter;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatViewAdapter extends FirestoreRecyclerAdapter<Message, ChatViewAdapter.MessageViewHolder> {
    private String currentUserId;
    private RequestManager glide;
    private Listener callback;
    private FirestoreRecyclerOptions<Message> options;

    public ChatViewAdapter(@NonNull FirestoreRecyclerOptions<Message> options, String currentUserId, RequestManager glide, Listener callback) {
        super(options);
        this.options = options;
        this.currentUserId = currentUserId;
        this.glide = glide;
        this.callback = callback;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message model) {
        // Because we cannot make a query using whereEqualTo() AND orderBy() simultaneously* I order the list below.
        // *It is possible when the fields required to make indexes are known beforehand
        // but in our case we do not know them, the fields are created dynamically (ex : participants.$userId)
        Message[] array = options.getSnapshots().toArray(new Message[0]);
        Arrays.sort(array, new ChatActivity.MessageRecentComparator());
        holder.bind(model, currentUserId);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_activity_message_item, parent, false);
        return new MessageViewHolder(view);
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private ChatActivityMessageItemBinding binding;
        private final Drawable bkgCurrentUser;
        private final Drawable bkgRemoteUser;

        MessageViewHolder(View itemView) {
            super(itemView);
            binding = ChatActivityMessageItemBinding.bind(itemView);
            bkgCurrentUser = ContextCompat.getDrawable(itemView.getContext(), R.drawable.bkg_message_end);
            bkgRemoteUser = ContextCompat.getDrawable(itemView.getContext(), R.drawable.bkg_message_start);
        }

        void bind(Message message, String currentUserId) {
            // Check if current user is the sender

            Boolean isCurrentUser = message.getUserSenderId().equals(currentUserId);

            // Update message TextView
            binding.messageContainerTextView.setText(message.getMessage());
            binding.messageContainerTextView.setTextAlignment(isCurrentUser ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);

            // Update date TextView
            if (message.getDateCreated() != null)
                binding.messageDateTextView.setText(this.convertDateToHour(message.getDateCreated()));

            // Update image sent ImageView
            if (message.getUrlImage() != null) {
                glide.load(message.getUrlImage())
                        .into(binding.imageSent);
                binding.imageSent.setVisibility(View.VISIBLE);
            } else {
                binding.imageSent.setVisibility(View.GONE);
            }

            //Update Message Bubble Color Background
            binding.messageContainer.setBackground(isCurrentUser ? bkgCurrentUser : bkgRemoteUser);

            // Update all views alignment depending is current user or not
            this.updateDesignDependingUser(isCurrentUser);
        }

        private void updateDesignDependingUser(Boolean isSender) {
            // MESSAGE CONTAINER
            RelativeLayout.LayoutParams paramsLayoutContent = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsLayoutContent.addRule(isSender ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
            binding.messageContainer.setLayoutParams(paramsLayoutContent);

            // CARDVIEW IMAGE SENT
            RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsImageView.addRule(isSender ? RelativeLayout.ALIGN_LEFT : RelativeLayout.ALIGN_RIGHT, R.id.message_container);
            binding.imageSentCardview.setLayoutParams(paramsImageView);

            binding.chatContainerRoot.requestLayout();
        }

        // ---

        private String convertDateToHour(Date date) {
            DateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return format.format(date);
        }
    }

    public interface Listener {
        void onDataChanged();
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        callback.onDataChanged();
    }
}
