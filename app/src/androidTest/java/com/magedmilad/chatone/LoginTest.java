package com.magedmilad.chatone;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.magedmilad.chatone.TestUtils.logOutIfLoggedIn;
import static com.magedmilad.chatone.TestUtils.withError;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void successfulLogin() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("a@a.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(withId(R.id.view_pager)).perform(swipeLeft());
        onView(withId(R.id.email)).check(matches(withText("a@a.com")));
    }

    @Test
    public void failedLoginInvalidMail() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("misdkjf"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(withText("entered email/password is invalid"))
                .inRoot(withDecorView(not(mActivityTestRule.getActivity().getWindow().getDecorView())))
                .check(matches(isDisplayed()));
    }

    @Test
    public void failedLoginWrongPassword() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("a@a.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("sglsndks"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(withText("entered email/password is invalid"))
                .inRoot(withDecorView(not(mActivityTestRule.getActivity().getWindow().getDecorView())))
                .check(matches(isDisplayed()));
    }

    @Test
    public void failedLoginEmptyEmail() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_password))
                .perform(typeText("sglsndks"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(withId(R.id.input_email)).check(matches(withError("enter your email")));
    }

    @Test
    public void failedLoginEmptyPassword() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("sglsndks"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(withId(R.id.input_password)).check(matches(withError("enter your password")));
    }
}
