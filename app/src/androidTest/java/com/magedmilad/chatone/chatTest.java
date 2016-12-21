package com.magedmilad.chatone;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.magedmilad.chatone.TestUtils.childAtPosition;
import static com.magedmilad.chatone.TestUtils.first;
import static com.magedmilad.chatone.TestUtils.logOutIfLoggedIn;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class chatTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void sendMessageToFriend() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("mina@m.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(allOf(withId(R.id.friend_name_text_view), withText("maged"))).perform(click());
        onView(withId(R.id.message_text))
                .perform(typeText("message to send"), closeSoftKeyboard());
        onView(withId(R.id.send_circular_image_view)).perform(click());
        onView(first(allOf(withId(R.id.message_text_view), withText("message to send"),
                childAtPosition(
                        allOf(withId(R.id.view),
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.RelativeLayout.class),
                                        0)),
                        0),
                isDisplayed()))).check(matches(isDisplayed()));
    }

    @Test
    public void emptyMessage() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("mina@m.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(allOf(withId(R.id.friend_name_text_view), withText("maged"))).perform(click());
        onView(withId(R.id.send_circular_image_view)).perform(click());
        onView(allOf(withId(R.id.message_text_view), withText(""))).check(doesNotExist());
    }
}