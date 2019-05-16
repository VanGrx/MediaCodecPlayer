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
public class test7_mix {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void test7_mix() {
        ViewInteraction button = onView(
                allOf(withId(R.id.button1),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button.perform(click());

        SystemClock.sleep(1500);

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.mainlayout),
                        withParent(withId(android.R.id.content)),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button4),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button2.perform(click());

        SystemClock.sleep(1500);

        ViewInteraction button3 = onView(
                allOf(withId(R.id.button6),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button3.perform(click());

        SystemClock.sleep(2600);

        ViewInteraction button4 = onView(
                allOf(withId(R.id.button1),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button4.perform(click());

        SystemClock.sleep(1500);

        ViewInteraction linearLayout2 = onView(
                allOf(withId(R.id.mainlayout),
                        withParent(withId(android.R.id.content)),
                        isDisplayed()));
        linearLayout2.perform(click());

        ViewInteraction button5 = onView(
                allOf(withId(R.id.button3),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button5.perform(click());

        SystemClock.sleep(1500);

        ViewInteraction button6 = onView(
                allOf(withId(R.id.button1),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button6.perform(click());

        SystemClock.sleep(1500);

        ViewInteraction linearLayout3 = onView(
                allOf(withId(R.id.mainlayout),
                        withParent(withId(android.R.id.content)),
                        isDisplayed()));
        linearLayout3.perform(click());

        ViewInteraction button7 = onView(
                allOf(withId(R.id.button3),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button7.perform(click());

    }

}
