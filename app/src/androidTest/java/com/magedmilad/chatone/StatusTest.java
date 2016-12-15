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
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.magedmilad.chatone.TestUtils.logOutIfLoggedIn;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StatusTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void changeStatus() {
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("a@a.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(withId(R.id.view_pager)).perform(swipeLeft());

        onView(allOf(withClassName(is("com.github.clans.fab.FloatingActionButton")),
                withParent(withId(R.id.setting_stack)),
                isDisplayed())).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.change_status_button),
                        withParent(withId(R.id.setting_stack)),
                        isDisplayed()));
        floatingActionButton2.perform(click());

        onView(withId(R.id.add_friend_edit_text))
                .perform(typeText("new status"), closeSoftKeyboard());
        onView(allOf(withId(android.R.id.button1), withText("Ok"),
                withParent(allOf(withClassName(is("android.widget.LinearLayout")),
                        withParent(withClassName(is("android.widget.LinearLayout"))))),
                isDisplayed())).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.status)).check(matches(withText("new status")));
    }

}
