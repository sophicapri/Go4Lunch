package com.sophieopenclass.go4lunch.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.gson.Gson;
import com.sophieopenclass.go4lunch.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sophieopenclass.go4lunch.utils.Constants.HITS_ALGOLIA;
import static com.sophieopenclass.go4lunch.utils.Constants.UID_FIELD;

public class AlgoliaDataRepository {
    private Index index;
    private List<User> workmates = new ArrayList<>();

    public AlgoliaDataRepository(Index index) {
        this.index = index;
    }

    public void populateDatabase(List<User> workmates) {
        this.workmates = workmates;
        Gson gson = new Gson();
        for (User workmate : workmates) {
            String jsonUser = gson.toJson(workmate);
            index.getObjectAsync(workmate.getUid(), (content, error) -> {
                if (content != null) { // update user data
                    try {
                        index.saveObjectAsync(new JSONObject(jsonUser), workmate.getUid(), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try { // add new user
                        index.addObjectAsync(new JSONObject(jsonUser), workmate.getUid(), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public MutableLiveData<ArrayList<User>> searchWorkmate(String input) {
        Query query = new Query(input).setAttributesToRetrieve(UID_FIELD)
                .setHitsPerPage(20);
        MutableLiveData<ArrayList<User>> usersMutable = new MutableLiveData<>();
        index.searchAsync(query, (jsonObject, e) -> {
            if (jsonObject != null)
                try {
                    JSONArray hits = jsonObject.getJSONArray(HITS_ALGOLIA);
                    ArrayList<User> users = new ArrayList<>();
                    for (int i = 0; i < hits.length(); i++) {
                        for (User user : workmates) {
                            if (hits.getJSONObject(i).getString(UID_FIELD).equals(user.getUid()))
                                users.add(user);
                        }
                    }
                    usersMutable.setValue(users);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
        });
        return usersMutable;
    }
}
