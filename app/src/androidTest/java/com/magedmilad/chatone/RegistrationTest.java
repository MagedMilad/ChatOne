package com.magedmilad.chatone;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.magedmilad.chatone.TestUtils.logOutIfLoggedIn;
import static com.magedmilad.chatone.TestUtils.withError;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RegistrationTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void successfulRegistration() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email)).perform(closeSoftKeyboard());
        onView(withId(R.id.signup_text)).perform(click());
        onView(withId(R.id.input_name))
                .perform(typeText("mina"), closeSoftKeyboard());
        onView(withId(R.id.input_email))
                .perform(typeText("mina@m.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.view_pager)).perform(swipeLeft());
        onView(withId(R.id.view_pager)).perform(swipeLeft());
        onView(withId(R.id.email)).check(matches(withText("mina@m.com")));
    }

    @Test
    public void failedRegInvalidMail() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email)).perform(closeSoftKeyboard());
        onView(withId(R.id.signup_text)).perform(click());
        onView(withId(R.id.input_name))
                .perform(typeText("mina"), closeSoftKeyboard());
        onView(withId(R.id.input_email))
                .perform(typeText("a@a.a"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.input_email)).check(matches(withError("this email is invalid")));

        onView(withId(R.id.input_email))
                .perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.input_email))
                .perform(typeText("lakdjn"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.input_email)).check(matches(withError("this email is invalid")));
    }

    @Test
    public void failedRegAlreadyRegMail() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email)).perform(closeSoftKeyboard());
        onView(withId(R.id.signup_text)).perform(click());
        onView(withId(R.id.input_name))
                .perform(typeText("mina"), closeSoftKeyboard());
        onView(withId(R.id.input_email))
                .perform(typeText("mina@m.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.input_email)).check(matches(withError("this email is already registered")));
    }

    @Test
    public void failedRegWrongPassword() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email)).perform(closeSoftKeyboard());
        onView(withId(R.id.signup_text)).perform(click());
        onView(withId(R.id.input_name))
                .perform(typeText("mina"), closeSoftKeyboard());
        onView(withId(R.id.input_email))
                .perform(typeText("h@h.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("1254"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(closeSoftKeyboard(), click());
        onView(withId(R.id.input_password)).check(matches(withError("weak password, password should be at least 6 characters")));
    }

    @Test
    public void failedRegEmptyName() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email)).perform(closeSoftKeyboard());
        onView(withId(R.id.signup_text)).perform(click());
        onView(withId(R.id.input_email))
                .perform(typeText("a@a.a"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.input_name)).check(matches(withError("user name can't be empty")));
    }

    @Test
    public void failedRegEmptyEmail() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email)).perform(closeSoftKeyboard());
        onView(withId(R.id.signup_text)).perform(click());
        onView(withId(R.id.input_name))
                .perform(typeText("mina"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.input_email)).check(matches(withError("user email can't be empty")));
    }

    @Test
    public void failedRegEmptyPassword() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email)).perform(closeSoftKeyboard());
        onView(withId(R.id.signup_text)).perform(click());
        onView(withId(R.id.input_name))
                .perform(typeText("mina"), closeSoftKeyboard());
        onView(withId(R.id.input_email))
                .perform(typeText("h@h.com"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.input_password)).check(matches(withError("password can't be Empty")));
    }
}
