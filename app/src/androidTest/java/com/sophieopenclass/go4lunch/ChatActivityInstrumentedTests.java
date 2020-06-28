package com.sophieopenclass.go4lunch;

import androidx.test.espresso.intent.rule.IntentsTestRule;

import com.sophieopenclass.go4lunch.view.activities.ChatActivity;
import com.sophieopenclass.go4lunch.view.activities.MainActivity;
import com.sophieopenclass.go4lunch.view.activities.UserDetailActivity;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.sophieopenclass.go4lunch.utils.TestUtils.withRecyclerView;
import static org.hamcrest.core.IsNot.not;

public class ChatActivityInstrumentedTests {

    @Rule
    public IntentsTestRule<MainActivity> rule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void test_display_chat_activity() throws InterruptedException {
        onView(withId(R.id.workmates_view)).perform(click());
        Thread.sleep(1000);
        onView(withRecyclerView(R.id.recycler_view_workmates).atPosition(0)).perform(click());
        intended(hasComponent(UserDetailActivity.class.getName()));

        onView(withId(R.id.fab_message_user)).perform(click());
        Thread.sleep(1000);
        intended(hasComponent(ChatActivity.class.getName()));
        intended(not(hasComponent(UserDetailActivity.class.getName())));
    }
}
