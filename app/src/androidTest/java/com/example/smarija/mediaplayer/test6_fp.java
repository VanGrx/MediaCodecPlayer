package com.example.smarija.mediaplayer;


import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
public class test6_fp {

    @Rule
    public ActivityTestRule<Fragment2> mActivityTestRule = new ActivityTestRule<>(Fragment2.class);

    @Test
    public void test6_fp() {
        ViewInteraction button = onView(
                allOf(withId(R.id.browse),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withId(android.R.id.content))),
                        5),
                        isDisplayed()));
        linearLayout.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction linearLayout2 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withId(android.R.id.content))),
                        2),
                        isDisplayed()));
        linearLayout2.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button1),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button2.perform(click());

        SystemClock.sleep(5000);

        ViewInteraction linearLayout3 = onView(
                allOf(withId(R.id.mainlayout),
                        withParent(withId(android.R.id.content)),
                        isDisplayed()));
        linearLayout3.perform(click());


        ViewInteraction button3 = onView(
                allOf(withId(R.id.browse),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button3.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction linearLayout4 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withId(android.R.id.content))),
                        5),
                        isDisplayed()));
        linearLayout4.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction linearLayout5 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withId(android.R.id.content))),
                        3),
                        isDisplayed()));
        linearLayout5.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction button4 = onView(
                allOf(withId(R.id.button1),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button4.perform(click());

        SystemClock.sleep(5000);

        ViewInteraction linearLayout6 = onView(
                allOf(withId(R.id.mainlayout),
                        withParent(withId(android.R.id.content)),
                        isDisplayed()));
        linearLayout6.perform(click());

        ViewInteraction button5 = onView(
                allOf(withId(R.id.browse),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button5.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction linearLayout7 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withId(android.R.id.content))),
                        5),
                        isDisplayed()));
        linearLayout7.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction linearLayout8 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withId(android.R.id.content))),
                        4),
                        isDisplayed()));
        linearLayout8.perform(click());

        SystemClock.sleep(1000);

        ViewInteraction button6 = onView(
                allOf(withId(R.id.button1),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button6.perform(click());

        SystemClock.sleep(5000);

        ViewInteraction linearLayout9 = onView(
                allOf(withId(R.id.mainlayout),
                        withParent(withId(android.R.id.content)),
                        isDisplayed()));
        linearLayout9.perform(click());

        ViewInteraction button7 = onView(
                allOf(withId(R.id.button3),
                        withParent(allOf(withId(R.id.one),
                                withParent(withId(R.id.mainlayout)))),
                        isDisplayed()));
        button7.perform(click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
