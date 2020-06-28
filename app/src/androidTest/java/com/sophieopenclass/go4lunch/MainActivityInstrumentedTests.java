package com.sophieopenclass.go4lunch;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sophieopenclass.go4lunch.view.activities.LoginActivity;
import com.sophieopenclass.go4lunch.view.activities.MainActivity;
import com.sophieopenclass.go4lunch.view.activities.SettingsActivity;
import com.sophieopenclass.go4lunch.view.activities.UserDetailActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_MAP_VIEW;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_RESTAURANT_LIST_VIEW;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_WORKMATES_LIST;
import static com.sophieopenclass.go4lunch.utils.TestUtils.forceClick;
import static com.sophieopenclass.go4lunch.utils.TestUtils.withRecyclerView;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTests {
    private MainActivity activity;
    private Fragment mapFragment;
    private Fragment restaurantListFragment;
    private Fragment workmateListFragment;
    // uid of a Dummy User created beforehand in Firebase
    private static final String DUMMY_USER_ID = "123";

    @Rule
    public IntentsTestRule<MainActivity> rule = new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setup() {
        activity = rule.getActivity();
        activity.setDummyUserId(DUMMY_USER_ID);
        mapFragment = activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_MAP_VIEW);
        restaurantListFragment = activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_RESTAURANT_LIST_VIEW);
        workmateListFragment = activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_WORKMATES_LIST);
    }

    @Test
    public void test_map_is_displayed(){
        assertThat(mapFragment, notNullValue());
        assertThat(restaurantListFragment, nullValue());
        assertThat(workmateListFragment, nullValue());
    }

    @Test
    public void test_display_restaurant_list_on_click(){
        onView(withId(R.id.restaurant_list_view)).perform(click());
        restaurantListFragment = activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_RESTAURANT_LIST_VIEW);
        assertThat(restaurantListFragment, notNullValue());
        assertThat(mapFragment.isVisible(), equalTo(false));
        assertThat(workmateListFragment, nullValue());
    }

    @Test
    public void test_display_workmate_list_on_click(){
        onView(withId(R.id.workmates_view)).perform(click());
        workmateListFragment = activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_WORKMATES_LIST);
        assertThat(workmateListFragment, notNullValue());
        assertThat(mapFragment.isVisible(), equalTo(false));
        assertThat(restaurantListFragment, nullValue());
    }

    @Test
    public void test_display_workmate_lunch_on_click() throws InterruptedException {
        onView(withId(R.id.workmates_view)).perform(click());
        Thread.sleep(1000);
        onView(withRecyclerView(R.id.recycler_view_workmates).atPosition(0)).perform(click());
        Thread.sleep(1000);
        intended(hasComponent(UserDetailActivity.class.getName()));
    }

    @Test
    public void test_open_and_close_drawer_menu() throws InterruptedException {
        onView(withContentDescription(R.string.open_navigation_drawer)).perform(click());
        assertThat(activity.binding.drawerLayout.isDrawerOpen(GravityCompat.START), equalTo(true));
        activity.onBackPressed();
        Thread.sleep(1000);
        assertThat(activity.binding.drawerLayout.isDrawerOpen(GravityCompat.START), equalTo(false));
    }

    @Test
    public void test_open_my_lunch_activity() throws InterruptedException {
        onView(withContentDescription(R.string.open_navigation_drawer)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.my_lunch)).perform(forceClick());
        intended(hasComponent(UserDetailActivity.class.getName()));
    }

    @Test
    public void test_open_settings_activity() throws InterruptedException {
        onView(withContentDescription(R.string.open_navigation_drawer)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.settings)).perform(forceClick());
        intended(hasComponent(SettingsActivity.class.getName()));
    }

    @Test
    public void test_sign_out() throws InterruptedException {
        onView(withContentDescription(R.string.open_navigation_drawer)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.sign_out)).perform(forceClick());
        intended(hasComponent(LoginActivity.class.getName()));
    }
}