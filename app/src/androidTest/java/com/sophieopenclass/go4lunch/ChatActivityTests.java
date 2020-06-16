package com.sophieopenclass.go4lunch;

import androidx.fragment.app.Fragment;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import com.sophieopenclass.go4lunch.controllers.activities.ChatActivity;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
import com.sophieopenclass.go4lunch.controllers.activities.UserDetailActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_WORKMATES_LIST;
import static com.sophieopenclass.go4lunch.utils.TestUtils.withRecyclerView;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class ChatActivityTests {
    private MainActivity activity;

    @Rule
    public IntentsTestRule<MainActivity> rule = new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setup(){
        activity = rule.getActivity();
    }

    @Test
    public void test_display_chat_activity() throws InterruptedException {
        onView(withId(R.id.workmates_view)).perform(click());
        Thread.sleep(1000);
        onView(withRecyclerView(R.id.recycler_view_workmates).atPosition(0)).perform(click());
        intended(hasComponent(UserDetailActivity.class.getName()));

        onView(withId(R.id.fab_message_user)).perform(click());
        intended(hasComponent(ChatActivity.class.getName()));
        intended(not(hasComponent(UserDetailActivity.class.getName())));
    }
}
