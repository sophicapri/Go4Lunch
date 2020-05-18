package com.sophieopenclass.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.sophieopenclass.go4lunch.models.Chat;
import com.sophieopenclass.go4lunch.models.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.sophieopenclass.go4lunch.controllers.fragments.ListViewFragment.TAG;

public class MessageDataRepository {
    private CollectionReference messageCollectionRef;

    public MessageDataRepository(CollectionReference messageCollectionRef) {
        this.messageCollectionRef = messageCollectionRef;
    }

    // --- GET ---

    public MutableLiveData<String> getChatId(String currentUserId, String workmateId) {
        MutableLiveData<String> chatId = new MutableLiveData<>();
        messageCollectionRef.whereEqualTo("participants." + currentUserId, true)
                .whereEqualTo("participants." + workmateId, true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null) {
                    Log.i(TAG, "getChatId: 1 -" + task.getResult().getDocuments().size());
                    if (task.getResult().getDocuments().size() == 1)
                        chatId.setValue(task.getResult().getDocuments().get(0).getId());
                } else if (task.getException() != null)
                    Log.e(TAG, "getChatId: " + (task.getException().getMessage()));
        });
        return chatId;
    }

    public Query getMessages(String chatId) {
        return messageCollectionRef.document(chatId).collection("conversation").orderBy("dateCreated").limit(50);
    }

    // --- CREATE ---

    public MutableLiveData<Boolean> createChat(String currentUserId, String workmateId) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        Map<String, Boolean> participants = new HashMap<>();
        participants.put(currentUserId, true);
        participants.put(workmateId, true);
        Chat chatSession = new Chat(participants);
        messageCollectionRef.add(chatSession).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                success.setValue(true);
            if (task.getException() != null)
                Log.e(TAG, "getChatId: " + (task.getException().getMessage()));
        });
        return success;
    }

    public MutableLiveData<Message> createMessageForChat(String textMessage, String userSenderId, String chatId) {
        MutableLiveData<Message> newMessage = new MutableLiveData<>();
        Message message = new Message(textMessage, userSenderId);

        messageCollectionRef.document(chatId).collection("conversation").add(message).addOnCompleteListener(addMessageTask -> {
            if (addMessageTask.isSuccessful()) {
                if (addMessageTask.getResult() != null) {
                    newMessage.postValue(message);
                }
            }
        });
        return newMessage;
    }

    public MutableLiveData<Message> createMessageWithImageForChat(String urlImage, String textMessage, String userSenderId) {
        MutableLiveData<Message> newMessage = new MutableLiveData<>();
        Message message = new Message(textMessage, urlImage, userSenderId);

        messageCollectionRef.add(message).addOnCompleteListener(addMessageTask -> {
            if (addMessageTask.isSuccessful()) {
                if (addMessageTask.getResult() != null) {
                    newMessage.postValue(message);
                }
            }
        });

        return newMessage;
    }
}
