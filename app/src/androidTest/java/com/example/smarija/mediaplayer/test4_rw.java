package com.example.smarija.mediaplayer;


import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class test4_rw {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void test4_rw() {
        ViewInteraction button = onView(
                allOf(withId(R.id.button1),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.mainlayout),
                        withParent(withId(android.R.id.content)),
                        isDisplayed()));
        linearLayout.perform(click());

        SystemClock.sleep(7000);

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button6),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button2.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction button3 = onView(
                allOf(withId(R.id.button2),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button3.perform(click());

    }

}
