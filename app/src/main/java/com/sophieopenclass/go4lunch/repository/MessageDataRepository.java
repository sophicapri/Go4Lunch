package com.sophieopenclass.go4lunch.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sophieopenclass.go4lunch.models.Message;
import com.sophieopenclass.go4lunch.models.User;

import java.util.HashMap;
import java.util.Map;

public class MessageDataRepository {
    private CollectionReference messageCollectionRef;

    public MessageDataRepository(CollectionReference messageCollectionRef) {
        this.messageCollectionRef = messageCollectionRef;
    }

    // --- GET ---

    public Query getAllMessagesForChat(String idSender, String idReceiver) {
        return messageCollectionRef.limit(50).whereEqualTo("participants." + idSender, true)
                .whereEqualTo("participants." + idReceiver, true);
    }

    // not correct.
    public Query getAllMessagesForChat() {
        return messageCollectionRef.orderBy("dateCreated").limit(50);
    }

    // --- CREATE ---

    public MutableLiveData<Message> createMessageForChat(String textMessage, String userSenderId, String userReceiverId) {
        MutableLiveData<Message> newMessage = new MutableLiveData<>();
        Map<String, Boolean> participants = new HashMap<>();
        participants.put(userSenderId, true);
        participants.put(userReceiverId, true);
        Message message = new Message(textMessage, userSenderId, participants);

        messageCollectionRef.add(message).addOnCompleteListener(addMessageTask -> {
            if (addMessageTask.isSuccessful()) {
                if (addMessageTask.getResult() != null) {
                    newMessage.postValue(message);
                }
            }
        });
        return newMessage;
    }

    public MutableLiveData<Message> createMessageWithImageForChat(String urlImage, String textMessage, String userSenderId, String userReceiverId) {
        MutableLiveData<Message> newMessage = new MutableLiveData<>();
        Map<String, Boolean> participants = new HashMap<>();
        participants.put(userSenderId, true);
        participants.put(userReceiverId, true);
        Message message = new Message(textMessage, urlImage, participants);

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
