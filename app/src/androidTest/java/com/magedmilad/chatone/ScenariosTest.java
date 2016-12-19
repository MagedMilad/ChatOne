package com.magedmilad.chatone;


import android.support.test.espresso.NoMatchingViewException;
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
import static com.magedmilad.chatone.TestUtils.delay;
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
    public void scenario1() {
        //user mina register
        logOutIfLoggedIn();

        onView(withId(R.id.signup_text)).perform(closeSoftKeyboard(), click());
        onView(withId(R.id.input_name))
                .perform(typeText("mina"), closeSoftKeyboard());
        onView(withId(R.id.input_email))
                .perform(typeText("mina@onechat.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());

        delay(2000);

        //user maged register
        logOutIfLoggedIn();

        onView(withId(R.id.signup_text)).perform(closeSoftKeyboard(), click());
        onView(withId(R.id.input_name))
                .perform(typeText("maged"), closeSoftKeyboard());
        onView(withId(R.id.input_email))
                .perform(typeText("maged@onechat.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());



        delay(2000);

        //maged add mina as friend
        onView(allOf(withClassName(is("com.github.clans.fab.FloatingActionButton")),
                withParent(withId(R.id.setting_stack)),
                isDisplayed())).perform(click());
        onView(allOf(withId(R.id.add_friend_button),
                withParent(withId(R.id.setting_stack)),
                isDisplayed())).perform(click());
        delay(1000);

        onView(withId(R.id.add_friend_edit_text))
                .perform(typeText("mina@onechat.com"), closeSoftKeyboard());

        delay(100);

        onView(allOf(withText("Ok"),
                isDisplayed())).perform(click());

        delay(1000);

        onView(allOf(withId(R.id.friend_name_text_view), withText("mina"))).check(matches(isDisplayed()));

        //maged send a message to mina
        onView(allOf(withId(R.id.friend_name_text_view), withText("mina"))).perform(click());
        onView(withId(R.id.message_text))
                .perform(typeText("message to send"), closeSoftKeyboard());
        onView(withId(R.id.send_circular_image_view)).perform(click(), pressBack());


        delay(1000);

        //mina check if message is received
        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText("mina@onechat.com"), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        delay(1000);

        onView(allOf(withId(R.id.friend_name_text_view), withText("maged"))).perform(click());

        delay(3000);

        ViewInteraction textView2 = onView(
                first(allOf(withId(R.id.message_text_view), withText("message to send"),
                        isDisplayed())));
        //TODO :check error
        textView2.check(matches(withText("message to send")));
    }


    @Test
    public void scenario2() {
        String[] usersEmails = new String[]{"mina@onechat.com", "maged@onechat.com", "omar@onechat.com", "nourane@onechat.com"};
        //users registration
        for (int i = 0; i < 4; i++) {
            logOutIfLoggedIn();
            onView(withId(R.id.signup_text)).perform(closeSoftKeyboard(), click());
            onView(withId(R.id.input_name))
                    .perform(typeText(usersEmails[i].substring(0, usersEmails[i].indexOf("@"))), closeSoftKeyboard());
            onView(withId(R.id.input_email))
                    .perform(typeText(usersEmails[i]), closeSoftKeyboard());
            onView(withId(R.id.input_password))
                    .perform(typeText("123456"), closeSoftKeyboard());
            onView(withId(R.id.register_button)).perform(click());
            delay(1000);
        }

        //users add each others as friends
        for (int i = 0; i < 4; i++) {
            if (i == 3) break;
            logOutIfLoggedIn();

            onView(withId(R.id.input_email))
                    .perform(typeText(usersEmails[i]), closeSoftKeyboard());
            onView(withId(R.id.input_password))
                    .perform(typeText("123456"), closeSoftKeyboard());
            onView(withId(R.id.login_button)).perform(click());

            delay(2000);

            onView(allOf(withClassName(is("com.github.clans.fab.FloatingActionButton")),
                    withParent(withId(R.id.setting_stack)),
                    isDisplayed())).perform(click());
            for (int j = i + 1; j < 4; j++) {
                onView(allOf(withId(R.id.add_friend_button),
                        withParent(withId(R.id.setting_stack)),
                        isDisplayed())).perform(click());
                onView(withId(R.id.add_friend_edit_text))
                        .perform(typeText(usersEmails[j]), closeSoftKeyboard());
                onView(allOf(withText("Ok"),
                        isDisplayed())).perform(click());
                delay(100);
//                onView(allOf(withId(R.id.friend_name_text_view), withText(usersEmails[j].substring(0, usersEmails[j].indexOf("@"))))).check(matches(isDisplayed()));
            }
            onView(allOf(withId(R.id.log_out_button),
                    withParent(withId(R.id.setting_stack)),
                    isDisplayed())).perform(click());
        }
    }

    @Test
    public void scenario3(){
        String[] usersEmails = new String[]{"mina@onechat.com", "maged@onechat.com", "omar@onechat.com", "nourane@onechat.com"};

        logOutIfLoggedIn();

        onView(withId(R.id.input_email))
                .perform(typeText(usersEmails[0]), closeSoftKeyboard());
        onView(withId(R.id.input_password))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(withId(R.id.view_pager)).perform(swipeLeft());

        delay(1000);

        onView(allOf(withClassName(is("com.github.clans.fab.FloatingActionButton")),
                withParent(withId(R.id.setting_stack)),
                isDisplayed())).perform(click());
        onView(allOf(withId(R.id.start_group_chat),
                withParent(withId(R.id.setting_stack)),
                isDisplayed())).perform(click());
        onView(withId(R.id.chat_name))
                .perform(typeText("group chat test"), closeSoftKeyboard());
        delay(2000);
        onView(allOf(withText("Create Group chat"),
                isDisplayed())).perform(click());
        while(true) {
            try {
                onView(withText("group chat test")).perform(click());
                break;
            } catch (NoMatchingViewException ignored) {}
        }
        onView(withId(R.id.message_text))
                .perform(typeText("hi to all"), closeSoftKeyboard());
        delay(1000);
        onView(withId(R.id.send_circular_image_view)).perform(click(), pressBack());

        //users check if message is received
        for(int i=1;i<4;i++){
            logOutIfLoggedIn();

            onView(withId(R.id.input_email))
                    .perform(typeText(usersEmails[i]), closeSoftKeyboard());
            onView(withId(R.id.input_password))
                    .perform(typeText("123456"), closeSoftKeyboard());
            onView(withId(R.id.login_button)).perform(click());
            while(true) {
                try {
                    onView(withId(R.id.view_pager)).perform(swipeLeft());
                    break;
                } catch (NoMatchingViewException e) {}
            }
            delay(1000);
            while(true) {
                try {
                    onView(withText("group chat test")).perform(click());
                    break;
                } catch (NoMatchingViewException e) {}
            }
            delay(1000);
            while(true) {
                try {
                    onView(withId(R.id.send_circular_image_view)).perform(pressBack());
                    break;
                } catch (NoMatchingViewException e) {}
            }
        }
    }



}
