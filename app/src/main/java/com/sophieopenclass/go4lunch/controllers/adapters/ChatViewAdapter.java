package com.sophieopenclass.go4lunch.controllers.adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
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
import com.sophieopenclass.go4lunch.databinding.ChatMessageItemBinding;
import com.sophieopenclass.go4lunch.models.Message;
import com.sophieopenclass.go4lunch.utils.DateFormatting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.sophieopenclass.go4lunch.utils.DateFormatting.formatDateToString;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getDateWithoutTime;

public class ChatViewAdapter extends FirestoreRecyclerAdapter<Message, ChatViewAdapter.MessageViewHolder> {
    private String currentUserId;
    private RequestManager glide;
    private Listener callback;

    public ChatViewAdapter(@NonNull FirestoreRecyclerOptions<Message> options, String currentUserId, RequestManager glide, Listener callback) {
        super(options);
        this.currentUserId = currentUserId;
        this.glide = glide;
        this.callback = callback;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message model) {
        holder.bind(model, currentUserId);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_item, parent, false);
        return new MessageViewHolder(view);
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private ChatMessageItemBinding binding;
        private final Drawable bkgCurrentUser;
        private final Drawable bkgRemoteUser;
        private boolean isHeaderVisible = false;

        MessageViewHolder(View itemView) {
            super(itemView);
            binding = ChatMessageItemBinding.bind(itemView);
            binding.headerDate.setVisibility(View.GONE);
            bkgCurrentUser = ContextCompat.getDrawable(itemView.getContext(), R.drawable.bkg_message_end);
            bkgRemoteUser = ContextCompat.getDrawable(itemView.getContext(), R.drawable.bkg_message_start);
        }

        void bind(Message message, String currentUserId) {
            // Check if current user is the sender
            Boolean isCurrentUser = message.getUserSenderId().equals(currentUserId);

            // Display header above the first message of the chat
            if (getBindingAdapterPosition() == 0) {
                binding.headerDate.setVisibility(View.VISIBLE);
                binding.headerDate.setText(formatDateToString(message.getDateCreated()));
                isHeaderVisible = true;
            } else {
                // Check if the date is the same as the previously sent message, if not, display header
                int positionPreviousMessage = getBindingAdapterPosition() - 1;
                Date datePreviousMessage = getDateWithoutTime(getSnapshots().get(positionPreviousMessage).getDateCreated());
                Date dateCurrentMessage = getDateWithoutTime(message.getDateCreated());
                if (!datePreviousMessage.toString().equals(dateCurrentMessage.toString())) {
                    binding.headerDate.setVisibility(View.VISIBLE);
                    binding.headerDate.setText(formatDateToString(message.getDateCreated()));
                    isHeaderVisible = true;
                } else {
                    binding.headerDate.setVisibility(View.GONE);
                    isHeaderVisible = false;
                }
            }

            // Update message TextView
            binding.messageContainerTextView.setText(message.getMessage());
            binding.messageContainerTextView.setTextAlignment(isCurrentUser ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);

            // Update date TextView
            if (message.getDateCreated() != null)
                binding.messageDateTextView.setText(convertDateToHour(message.getDateCreated()));

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
            paramsLayoutContent.addRule(isSender ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START);
            if (isHeaderVisible)
                paramsLayoutContent.addRule(RelativeLayout.BELOW, R.id.header_date);
            binding.messageContainer.setLayoutParams(paramsLayoutContent);

            // TIMESTAMP
            RelativeLayout.LayoutParams paramsDateView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsDateView.addRule(isSender ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START);
            paramsDateView.addRule(RelativeLayout.BELOW, R.id.image_sent_cardview);
            binding.messageDateTextView.setLayoutParams(paramsDateView);

            // CARDVIEW IMAGE SENT
            RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsImageView.addRule(isSender ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START);
            paramsImageView.addRule(RelativeLayout.BELOW, R.id.message_container);
            paramsImageView.setMargins(0, 10, 0, 10);
            binding.imageSentCardview.setLayoutParams(paramsImageView);

            binding.chatContainerRoot.requestLayout();
        }
    }

    private String convertDateToHour(Date date) {
        DateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format.format(date);
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
