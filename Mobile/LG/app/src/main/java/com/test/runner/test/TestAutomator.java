package com.test.runner.test;

import android.os.SystemClock;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import java.util.Calendar;
import java.util.Random;

public class TestAutomator extends UiAutomatorTestCase {

    public void testAutomate() throws UiObjectNotFoundException {
        Random r = new Random();
        long time = System.currentTimeMillis();
        int watchCount = 0;
        int resetCount = r.nextInt(15 - 10) + 10; // reset Perk every 10-15 app trailers
        boolean replay = false;
        boolean needReset = false;
        UiObject obj;

        EnvironmentProcessor environmentProcessor = new EnvironmentProcessor();
        EnvironmentProperty ep = new EnvironmentProperty();
        ep.key = "perkBotPid";
        ep.value = String.valueOf(android.os.Process.myPid());
        environmentProcessor.set(ep);

        int startingHour = Integer.parseInt(environmentProcessor.getValue("perkStartingHour"));
        int endingHour = Integer.parseInt(environmentProcessor.getValue("perkEndingHour"));
        while (true) {
            try {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (hour < startingHour) {
                    // Sleep for a minute.
                    SystemClock.sleep(1000 * 60);
                    continue;
                }
                if (endingHour <= hour) {
                    // Sleep for a minute.
                    SystemClock.sleep(1000 * 60);
                    continue;
                }

                // Clear application data and reset perk every 2 hours.
                if (hour % 2 == 0) {
                    if (needReset) {
                        needReset = false;
                        getUiDevice().pressHome();
                        obj = new UiObject(new UiSelector().text("Settings"));
                        if (obj.waitForExists(20000)) obj.click();
                        UiScrollable listView = new UiScrollable(new UiSelector().scrollable(true));
                        listView.scrollTextIntoView("Apps");
                        obj = new UiObject(new UiSelector().text("Apps"));
                        if (obj.waitForExists(20000)) obj.click();
                        listView = new UiScrollable(new UiSelector().resourceId("android:id/list").scrollable(true));
                        listView.scrollTextIntoView("Perk TV");
                        obj = new UiObject(new UiSelector().text("Perk TV"));
                        if (obj.waitForExists(20000)) obj.click();
                        obj = new UiObject(new UiSelector().text("Clear data"));
                        if (obj.waitForExists(20000)) obj.click();
                        obj = new UiObject(new UiSelector().text("OK"));
                        if (obj.waitForExists(20000)) obj.click();
                        getUiDevice().pressHome();
                        obj = new UiObject(new UiSelector().text("Perk TV"));
                        if (obj.waitForExists(20000)) obj.clickAndWaitForNewWindow();
                    }
                } else needReset = true;

                // Login if necessary.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/tv_ls_login"));
                if (obj.exists()) {
                    obj.click();
                    String email = environmentProcessor.getValue("perkEmail");
                    String pass = environmentProcessor.getValue("perkPass");
                    if (email.isEmpty() || pass.isEmpty()) continue;
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/et_ls_login_email"));
                    if (obj.waitForExists(20000)) {
                        boolean pressBack = false;
                        if (!obj.getText().isEmpty()) pressBack = true;
                        obj.setText(email);
                        if (pressBack) getUiDevice().pressBack();
                    }
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/et_ls_login_pass"));
                    if (obj.waitForExists(20000)) obj.setText(pass);
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/ib_ls_login"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_watchVideo_overlay_close"));
                    if (obj.waitForExists(40000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/rlv_watchRewards_coachmark"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/rlv_sweeps_coachmark"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_app_watch_earn"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/overlay"));
                    if (obj.waitForExists(10000)) obj.click();
                    // Check "Low Bandwidth Video".
                    obj = new UiObject(new UiSelector().resourceId("android:id/home"));
                    if (obj.waitForExists(20000)) obj.click();
                    UiScrollable listView = new UiScrollable(new UiSelector().resourceId("com.juteralabs.perktv:id/listview_drawer").scrollable(true));
                    listView.scrollTextIntoView("Low Bandwidth Video");
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/low_bandwidth_check"));
                    if (obj.waitForExists(20000)){
                        if (!obj.isChecked()) obj.click();
                    }
                    obj = new UiObject(new UiSelector().resourceId("android:id/home"));
                    if (obj.waitForExists(20000)) obj.click();
                }

                // Even though after logging in, the following items are checked for and clicked if they exist, we need to check
                // multiple times after the user has logged in because they may re-appear.  This is especially seen when the apps
                // cached is cleared.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_watchVideo_overlay_close"));
                if (obj.exists()) obj.click();
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/rlv_watchRewards_coachmark"));
                if (obj.exists()) obj.click();
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/rlv_sweeps_coachmark"));
                if (obj.exists()) obj.click();
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_app_watch_earn"));
                if (obj.exists()) obj.click();
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/overlay"));
                if (obj.exists()) obj.click();

                // If the "Update" button is present in the update dialog box, then simply pause all
                // processing and do not perform any actions.  This should keep the update dialog
                // showing on the screen to alert why processing has stopped.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_versionUpdate"));
                if (obj.exists()) {
                    SystemClock.sleep(1000 * 60 * 60);
                    continue;
                }

                // If the "X" button is present in the update dialog, then simply pause all
                // processing and do not perform any actions.  This should keep the update dialog
                // showing on the screen to alert why processing has stopped.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/ib_close_versionUpdate"));
                if (obj.exists()) {
                    SystemClock.sleep(1000 * 60 * 60);
                    continue;
                }

                // Press "No, thanks" when asked to rate Perk TV.
                obj = new UiObject(new UiSelector().text("Love using Perk TV?"));
                if (obj.exists()) {
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_rate_the_app_noThanks"));
                    obj.click();
                }

                // Close "Like Us and get 50 Points!"
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_likeEarn_overlay_close"));
                if (obj.exists()) obj.click();

                // Tap screen to dismiss popup.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/imageView1"));
                if (obj.exists()) obj.click();

                // Sometimes no content is displayed for app trailers selection. The screen is just white.  To prevent
                // this, do some checks to see if the screen is truly in that state and if so, press the watch button
                // again to reload the trailers.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/watch_button_text"));
                if (obj.exists()){
                    // Check to see if the circular progress indicator is showing.  If it is, then it is still
                    // trying to attempt to load options and there is no further action required.
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/progressBar"));
                    if (obj.exists()){
                        // Sleep for a second and then continue processing.
                        SystemClock.sleep(1000);
                    } else {
                        // At this point the progress bar is not showing so lets check to see if any app trailers are
                        // available to be selected.  If there are app trailers, then there is no need to click the watch
                        // button anymore.
                        obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/sv_movieTrailers"));
                        if (!obj.exists()) {
                            // See if the watch button still exists, and if so, click on it to load up the trailers again.
                            obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/watch_button_text"));
                            if (obj.exists()){
                                obj.click();
                                // Click on the App trailers button on the Watch & Earn screen.
                                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_app_watch_earn"));
                                if (obj.waitForExists(5000)) obj.click();
                            }
                        }
                    }
                }

                // Select "App Trailers".
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_app_watch_earn"));
                if (obj.exists()) obj.clickAndWaitForNewWindow(20000);

                // Select "Watch and Earn".
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/video_thumbnail").index(0));
                if (obj.exists()) obj.clickAndWaitForNewWindow(20000);

                // Select "Yes" for "Are you still watching?".
                obj = new UiObject(new UiSelector().resourceId("android:id/button1").text("Yes"));
                if (obj.exists()) obj.click();

                // If video fails to load, restart Perk TV application.
                if (time + 240000 < System.currentTimeMillis() || watchCount == resetCount) {
                    getUiDevice().pressHome();
                    watchCount = 0;
                    if (watchCount == resetCount) resetCount = r.nextInt(15 - 10) + 10;
                    // Close "PerkTV" application.
                    getUiDevice().pressRecentApps();
                    obj = new UiObject(new UiSelector().text("Clear all"));
                    obj.clickAndWaitForNewWindow(20000);
                    getUiDevice().pressHome();
                    // Open "PerkTV" application.
                    obj = new UiObject(new UiSelector().text("Perk TV"));
                    if (obj.exists()) {
                        obj.clickAndWaitForNewWindow(20000);
                        time = System.currentTimeMillis();
                    }
                }

                // Check for ad without "Perk TV Ads" banner.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/videoplayer_relativelayout"));
                if (obj.exists()) {
                    // Check for ad with "Perk TV Ads" banner.
                    obj = new UiObject(new UiSelector().textContains("Perk TV Ads"));
                    if (!obj.exists()) {
                        // Check for "Watch Next" button.
                        obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_watch_next"));
                        if (obj.exists()) {
                            if (Math.random() < 0.75) { // Select "Replay".
                                obj = new UiObject(new UiSelector().text("Replay"));
                                replay = true;
                            } else { // Select "Watch Next".
                                obj = new UiObject(new UiSelector().text("Watch Next"));
                                replay = false;
                                watchCount++;
                            }
                            if (obj.exists()) obj.clickAndWaitForNewWindow(20000);
                        } else {
                            SystemClock.sleep(10000);
                            // Check for video player.
                            obj = new UiObject(new UiSelector().className("android.widget.VideoView"));
                            if (obj.exists()) {
                                obj.click(); // Click video to display controls.
                                obj = new UiObject(new UiSelector().className("android.widget.SeekBar"));
                                // Check for video controls.
                                if (obj.exists()) {
                                    obj.swipeRight(1); // Skip to end.
                                    time = System.currentTimeMillis();
                                }
                            }
                        }
                    }
                }

                // If the browser is trying to be opened, then simply press the back button to continue
                // watching.  Also sleep for 10 seconds to make sure the action doesn't appear again.
                obj = new UiObject(new UiSelector().textContains("Complete action using"));
                if (obj.exists()) {
                    getUiDevice().pressBack();
                    SystemClock.sleep(10000);
                }

                obj = new UiObject(new UiSelector().resourceId("com.android.chrome:id/control_container"));
                if (obj.exists()) {
                    getUiDevice().pressBack();
                    SystemClock.sleep(9000);
                }
                SystemClock.sleep(1000);
            } catch (Exception e) {}
        }
    }
}