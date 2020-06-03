package com.sophieopenclass.go4lunch.models;

import java.util.Map;

public class Chat {
    private Map<String, Boolean> participants;

    public Chat(){

    }

    public Chat(Map<String, Boolean> participants) {
        this.participants = participants;
    }

    public Map<String, Boolean> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, Boolean> participants) {
        this.participants = participants;
    }
}
