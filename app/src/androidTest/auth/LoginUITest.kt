package com.example.askchinna.auth

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.askchinna.R
import com.askchinna.ui.auth.LoginActivity
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testMobileNumberValidation() {
        // Check initial state - login button should be disabled
        onView(withId(R.id.buttonLogin)).check(matches(not(isEnabled())))

        // Enter invalid mobile number (too short)
        onView(withId(R.id.editTextMobile)).perform(typeText("98765"), closeSoftKeyboard())

        // Error should be displayed and login button still disabled
        onView(withId(R.id.editTextMobile)).check(matches(hasErrorText(
            containsString(R.string.error_invalid_mobile))))
        onView(withId(R.id.buttonLogin)).check(matches(not(isEnabled())))

        // Clear and enter valid mobile number
        onView(withId(R.id.editTextMobile)).perform(clearText(),
            typeText("9876543210"), closeSoftKeyboard())

        // Login button should now be enabled
        onView(withId(R.id.buttonLogin)).check(matches(isEnabled()))
    }

    @Test
    fun testNavigationToRegister() {
        // Click on register text
        onView(withId(R.id.textViewRegister)).perform(click())

        // Check if we're on register screen by looking for register button
        onView(withId(R.id.buttonRegister)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginButtonTriggersOtpScreen() {
        // Enter valid mobile number
        onView(withId(R.id.editTextMobile)).perform(typeText("9876543210"),
            closeSoftKeyboard())

        // Click login button
        onView(withId(R.id.buttonLogin)).perform(click())

        // Verify loading indicator is shown (this is shown while OTP is sent)
        onView(withId(R.id.progressBar)).check(matches(isDisplayed()))

        // Note: We can't fully test OTP verification transition in UI tests
        // as it requires actual Firebase interaction
    }
}