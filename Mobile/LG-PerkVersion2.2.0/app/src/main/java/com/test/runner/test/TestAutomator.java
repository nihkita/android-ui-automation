package com.test.runner.test;

/**
 * Created by stratmann on 1/10/2015.
 */
//Import the uiautomator libraries

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Random;

public class TestAutomator extends UiAutomatorTestCase {

    public void testAutomate() throws UiObjectNotFoundException {
        Random r = new Random();
        long time = System.currentTimeMillis();
        int watchCount = 0;
        int resetCount = r.nextInt(15 - 10) + 10; // reset Perk every 10-15 app trailers
        boolean like = false;
        boolean replay = false;
        boolean needReset = false;
        UiObject obj;

        int startingHour = 0;
        int endingHour = 0;
        Boolean rateVideos = false;
        Process process;
        try {
            process = Runtime.getRuntime().exec("getprop pRateVideos");
            BufferedReader bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
            rateVideos = Boolean.parseBoolean(bis.readLine());
            bis.close();
            process.destroy();
            process = Runtime.getRuntime().exec("getprop pStartingHour");
            bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
            startingHour = Integer.parseInt(bis.readLine());
            bis.close();
            process.destroy();
            process = Runtime.getRuntime().exec("getprop pEndingHour");
            bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
            endingHour = Integer.parseInt(bis.readLine());
            bis.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Calendar c = Calendar.getInstance();

                // The hour of the day is in 24 hour format.
                int currentHour = c.get(c.HOUR_OF_DAY);

                if (currentHour < startingHour) {
                    SystemClock.sleep(1000 * 60 * 10);
                    continue;
                }

                // If the current time is 6:05 AM and the ending hour is 6, then stop working because
                // the ending hour is equal to the current hour.  If the current time is 6:05 AM and
                // then ending hour is 5, then stop working because the ending hour is less than
                // the current hour.
                if (endingHour <= currentHour) {
                    SystemClock.sleep(1000 * 60 * 10);
                    continue;
                }

                // Clear application data and reset perk every 2 hours.
                if (currentHour % 2 == 0) {
                    if (needReset) {
                        needReset = false;
                        getUiDevice().pressHome();
                        obj = new UiObject(new UiSelector().text("Apps"));
                        if (obj.waitForExists(20000)) obj.click();
                        // Wait for Settings icon to display to indicate when it is safe to swipe.
                        obj = new UiObject(new UiSelector().resourceId("com.lge.launcher2:id/lg_page_menu_edit_btn"));
                        obj.waitForExists(20000);
                        while (true) { // Find Settings application.
                            obj = new UiObject(new UiSelector().text("Settings"));
                            if (obj.waitForExists(3000)) break;
                            getUiDevice().swipe(250, 250, 10, 250, 10);
                        }
                        obj.clickAndWaitForNewWindow(20000);
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
                        if (obj.exists()) obj.clickAndWaitForNewWindow();
                    }
                } else needReset = true;

                // Close update dialog
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/ib_close_versionUpdate"));
                if (obj.exists()) obj.click();

                // Login if necessary.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/tv_ls_login"));
                if (obj.exists()) {
                    obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/et_ls_login_email"));
                    process = Runtime.getRuntime().exec("getprop pUser");
                    BufferedReader bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String user = bis.readLine();
                    bis.close();
                    process.destroy();
                    if (obj.waitForExists(20000)) obj.setText(user);
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/et_ls_login_pass"));
                    process = Runtime.getRuntime().exec("getprop pPass");
                    bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String pass = bis.readLine();
                    bis.close();
                    process.destroy();
                    if (obj.waitForExists(20000)) obj.setText(pass);
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/ib_ls_login"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_watchVideo_overlay_close"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/rlv_watchRewards_coachmark"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/rlv_sweeps_coachmark"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_app_watch_earn"));
                    if (obj.waitForExists(20000)) obj.clickAndWaitForNewWindow(20000);
                    // Check "Low Bandwidth Video".
                    obj = new UiObject(new UiSelector().resourceId("android:id/home"));
                    if (obj.waitForExists(20000)) obj.click();
                    UiScrollable listView = new UiScrollable(new UiSelector().resourceId("com.juteralabs.perktv:id/listview_drawer").scrollable(true));
                    listView.scrollTextIntoView("Low Bandwidth Video");
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/low_bandwidth_check"));
                    if (obj.waitForExists(20000)) obj.click();
                    obj = new UiObject(new UiSelector().resourceId("android:id/home"));
                    if (obj.waitForExists(20000)) obj.click();
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

                // Select "App Trailers".
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_app_watch_earn"));
                if (obj.exists()) obj.clickAndWaitForNewWindow(20000);

                // Select "Watch and Earn".
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/watchandearn"));
                if (obj.exists()) {
                    obj.clickAndWaitForNewWindow(20000);
                    // Select "Watch Trailer To Earn Points".
                    obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/watch_trailer_button"));
                    if (obj.exists()) obj.click();
                }

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

                // Check for "Get the App" button.
                obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/btn_getTheApp"));
                if (obj.exists()) {
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
                } else {
                    if (!replay) like = Math.random() < 0.5;
                    if (like) obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/ib_likeOFF")); // Select "Like".
                    else obj = new UiObject(new UiSelector().resourceId("com.juteralabs.perktv:id/ib_dislikeOFF")); // Select "Dislike".
                    if (obj.exists()) {
                        if (rateVideos) {
                            obj.click();
                        }
                        if (Math.random() < 0.75) { // Select "Replay".
                            obj = new UiObject(new UiSelector().text("Replay"));
                            replay = true;
                        } else { // Select "Watch Next".
                            obj = new UiObject(new UiSelector().text("Watch Next"));
                            replay = false;
                            watchCount++;
                        }
                        if (obj.exists()) obj.clickAndWaitForNewWindow(20000);
                    }
                }
            } catch (Exception e) {}
            SystemClock.sleep(1000);
        }
    }
}