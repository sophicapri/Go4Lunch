package com.sophieopenclass.go4lunch.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sophieopenclass.go4lunch.models.Message;
import com.sophieopenclass.go4lunch.models.User;

public class MessageDataRepository {
    private CollectionReference messageCollectionRef;

    public MessageDataRepository(CollectionReference messageCollectionRef) {
        this.messageCollectionRef = messageCollectionRef;
    }

    // --- GET ---

    public Query getAllMessageForChat(String idSender, String idReceiver) {
        return messageCollectionRef.whereEqualTo("users." + idSender, true)
                .whereEqualTo("users." + idReceiver, true).orderBy("dateCreated").limit(50);
    }

    // not correct.
    public Query getAllMessageForChat() {
        return messageCollectionRef.orderBy("dateCreated").limit(50);
    }

    // --- CREATE ---

    public MutableLiveData<Message> createMessageForChat(String textMessage, User userSender) {
        MutableLiveData<Message> newMessage = new MutableLiveData<>();
        Message message = new Message(textMessage, userSender);

        messageCollectionRef.add(message).addOnCompleteListener(addMessageTask -> {
            if (addMessageTask.isSuccessful()) {
                if (addMessageTask.getResult() != null) {
                    newMessage.postValue(message);
                }
            }
        });
        return newMessage;
    }

    public MutableLiveData<Message> createMessageWithImageForChat(String urlImage, String textMessage, User userSender) {
        MutableLiveData<Message> newMessage = new MutableLiveData<>();
        Message message = new Message(textMessage, urlImage, userSender);

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
