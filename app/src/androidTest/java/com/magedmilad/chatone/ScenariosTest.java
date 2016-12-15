package com.magedmilad.chatone;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.magedmilad.chatone.TestUtils.first;
import static com.magedmilad.chatone.TestUtils.logOutIfLoggedIn;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScenariosTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void Scenario1() {
        //user mina register
        logOutIfLoggedIn();

//        onView(withId(R.id.signup_text)).perform(closeSoftKeyboard(), click());
//        onView(withId(R.id.input_name))
//                .perform(typeText("mina"), closeSoftKeyboard());
//        onView(withId(R.id.input_email))
//                .perform(typeText("mina@onechat.com"), closeSoftKeyboard());
//        onView(withId(R.id.input_password))
//                .perform(typeText("123456"), closeSoftKeyboard());
//        onView(withId(R.id.register_button)).perform(click());
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        //user maged register
//        logOutIfLoggedIn();

//        onView(withId(R.id.signup_text)).perform(closeSoftKeyboard(), click());
//        onView(withId(R.id.input_name))
//                .perform(typeText("maged"), closeSoftKeyboard());
//        onView(withId(R.id.input_email))
//                .perform(typeText("maged@onechat.com"), closeSoftKeyboard());
//        onView(withId(R.id.input_password))
//                .perform(typeText("123456"), closeSoftKeyboard());
//        onView(withId(R.id.register_button)).perform(click());


        onView(withId(R.id.input_email))
                .perform(typeText("maged@onechat.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //maged add mina as friend
        onView(allOf(withClassName(is("com.github.clans.fab.FloatingActionButton")),
                withParent(withId(R.id.setting_stack)),
                isDisplayed())).perform(click());
        onView(allOf(withId(R.id.add_friend_button),
                withParent(withId(R.id.setting_stack)),
                isDisplayed())).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.add_friend_edit_text))
                .perform(typeText("mina@onechat.com"), closeSoftKeyboard());

        onView(allOf(withId(android.R.id.button1), withText("Ok"),
                withParent(allOf(withClassName(is("android.widget.LinearLayout")),
                        withParent(withClassName(is("android.widget.LinearLayout"))))),
                isDisplayed())).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.friend_name_text_view), withText("mina"))).check(matches(isDisplayed()));

        //maged send a message to mina
        onView(allOf(withId(R.id.friend_name_text_view), withText("mina"))).perform(click());
        onView(withId(R.id.message_text))
                .perform(typeText("message to send"), closeSoftKeyboard());
        onView(withId(R.id.send_circular_image_view)).perform(click(), pressBack());


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //mina check if message is received
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("mina@onechat.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.friend_name_text_view), withText("maged"))).perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView2 = onView(
                first(allOf(withId(R.id.message_text_view), withText("message to send"),
                        isDisplayed())));
        //TODO :check error
        textView2.check(matches(withText("message to send")));
    }

}
