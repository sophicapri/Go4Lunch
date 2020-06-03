package com.sophieopenclass.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sophieopenclass.go4lunch.models.Chat;
import com.sophieopenclass.go4lunch.models.Message;

import java.util.HashMap;
import java.util.Map;

import static com.sophieopenclass.go4lunch.controllers.fragments.RestaurantListFragment.TAG;
import static com.sophieopenclass.go4lunch.utils.Constants.MESSAGES_SUBCOLLECTION;
import static com.sophieopenclass.go4lunch.utils.Constants.DATE_CREATED;
import static com.sophieopenclass.go4lunch.utils.Constants.PARTICIPANTS_FIELD;
import static com.sophieopenclass.go4lunch.utils.Constants.USER_SENDER_ID;

public class MessageDataRepository {
    private CollectionReference messageCollectionRef;

    public MessageDataRepository(CollectionReference messageCollectionRef) {
        this.messageCollectionRef = messageCollectionRef;
    }

    // --- GET ---

    public MutableLiveData<String> getChatId(String currentUserId, String workmateId) {
        MutableLiveData<String> chatId = new MutableLiveData<>();
        messageCollectionRef.whereEqualTo(PARTICIPANTS_FIELD + currentUserId, true)
                .whereEqualTo(PARTICIPANTS_FIELD + workmateId, true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null) {
                    Log.i(TAG, "getChatId:-" + task.getResult().getDocuments().size());
                    if (task.getResult().getDocuments().size() == 1)
                        chatId.setValue(task.getResult().getDocuments().get(0).getId());
                } else if (task.getException() != null)
                    Log.e(TAG, "getChatId: " + (task.getException().getMessage()));
        });
        return chatId;
    }

    public Query getMessagesQuery(String chatId) {
        return messageCollectionRef.document(chatId).collection(MESSAGES_SUBCOLLECTION).orderBy(DATE_CREATED).limit(50);
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

        messageCollectionRef.document(chatId).collection(MESSAGES_SUBCOLLECTION).add(message).addOnCompleteListener(addMessageTask -> {
            if (addMessageTask.isSuccessful()) {
                if (addMessageTask.getResult() != null) {
                    newMessage.postValue(message);
                }
            }
        });
        return newMessage;
    }

    public MutableLiveData<Message> createMessageWithImageForChat(String urlImage, String textMessage, String userSenderId, String chatId) {
        MutableLiveData<Message> newMessage = new MutableLiveData<>();
        Message message = new Message(textMessage, urlImage, userSenderId);

        messageCollectionRef.document(chatId).collection(MESSAGES_SUBCOLLECTION).add(message).addOnCompleteListener(addMessageTask -> {
            if (addMessageTask.isSuccessful()) {
                if (addMessageTask.getResult() != null) {
                    newMessage.postValue(message);
                }
            }
        });

        return newMessage;
    }

    public void deleteUserMessages(String uid) {
        messageCollectionRef.whereEqualTo(PARTICIPANTS_FIELD + uid, true).get().addOnSuccessListener(documentSnapshots -> {
            for (DocumentSnapshot document :documentSnapshots.getDocuments()){
                document.getReference().collection(MESSAGES_SUBCOLLECTION).whereEqualTo(USER_SENDER_ID, uid)
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                int convDeleted = 0;
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        for (DocumentSnapshot document :documentSnapshots.getDocuments()) {
                            document.getReference().delete();
                            convDeleted++;
                        }
                        Log.i(TAG, "onSuccess: messages deleted = " + convDeleted);
                    }
                });
            }

        });
    }
}
