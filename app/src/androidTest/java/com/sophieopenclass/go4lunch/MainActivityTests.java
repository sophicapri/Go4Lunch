package com.sophieopenclass.go4lunch;

import android.view.Gravity;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
import com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_MAP_VIEW;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_RESTAURANT_LIST_VIEW;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_WORKMATES_LIST;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class MainActivityTests {
    private MainActivity activity;
    private Fragment mapFragment;
    private Fragment restaurantListFragment;
    private Fragment workmateListFragment;


    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setup() {
        activity = rule.getActivity();
        FirebaseAuth.getInstance().signInAnonymously();
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
        onView(withId(R.id.list_view)).perform(click());
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
    public void test_open_drawer_menu(){
        onView(withContentDescription(R.string.open_navigation_drawer)).perform(click());
        assertThat(activity.binding.drawerLayout.isDrawerOpen(R.id.drawer_layout), equalTo(true));
    }
}