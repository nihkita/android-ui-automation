package at.runner;

import android.os.SystemClock;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

/**
 * A class that will be in control of starting up the external application.
 */
public class Starter {

    /**
     * Contains a reference to the UI Device that is being processed.
     */
    private UiDevice _uiDevice;

    /**
     * Contains a reference to the logging class to write out log messages.  Please note that this
     * variable was named "Log" for a reason and not "_logger" which would normally be the name for
     * a private field.  By simply deleting this private variable and importing "import android.util.Log;"
     * all the references to the "Log" should still work without much rewrite.
     */
    private Logger Log;

    /**
     * A constructor that will create a Starter class.
     * @param logger The logging class that will be used to log messages.
     * @param uiDevice A reference to the UI device to interact with.
     */
    public Starter(Logger logger, UiDevice uiDevice) {
        Log = logger;
        _uiDevice = uiDevice;
    }

    /**
     * The main function that will process this class.
     * @return A value will be returned indicating whether or not the processing was a success or
     * not.
     */
    public boolean Process() {
        String functionName = "Starter_Process()";
        Log.i(functionName, "The function has started.");

        try {
            getUiDevice().pressHome();
            Log.i(functionName, "The home button was successfully pressed.");

            // Check for the Apps item.
            ActionResult result = checkForAppTrailersApp();
            if (result != ActionResult.Found) {
                // If the Apps Trailers app is not available then return false and stop processing because
                // it must exist.
                return false;
            }
            result = processAppTrailersApp();
            if (result != ActionResult.Processed) {
                // If the App Trailers app is not available then return false and stop processing because
                // it must exist.
                return false;
            }

            // Check for the welcome app trailers OK button.
            result = checkForWelcomeToAppTrailersOkButton();
            if (result == ActionResult.Exception) {
                return false;
            }
            if (result == ActionResult.Found) {
                result = processWelcomeToAppTrailersOkButton();
                if (result == ActionResult.Exception){
                    return false;
                }
            }

            // Check for the tip 3 times OK button.
            result = checkForTip3TimesOkButton();
            if (result == ActionResult.Exception) {
                return false;
            }
            if (result == ActionResult.Found) {
                result = processTip3TimesOkButton();
                if (result == ActionResult.Exception){
                    return false;
                }
            }

            // Try 4 times to get back to the home page which has the home icon.
            for(int attempts = 0; attempts < 4; attempts++) {
                // Check for the Home icon.
                result = checkForHomeIcon();
                if (result == ActionResult.Found) {
                    break;
                }

                // The device may be on a view that does not have the home button on it and if that
                // is the case, then press the back button to get back to the main view.
                getUiDevice().pressBack();

            }
            if (result != ActionResult.Found) {
                // If the home icon is not available then return false and stop processing because
                // it must exist.
                return false;
            }
            result = processHomeIcon();
            if (result != ActionResult.Processed) {
                // If the home icon is not available then return false and stop processing because
                // it must exist.
                return false;
            }

            // Check for the Apps item.
            result = checkForAppsItem();
            if (result != ActionResult.Found) {
                // If the Apps item is not available then return false and stop processing because
                // it must exist.
                return false;
            }
            result = processAppsItem();
            if (result != ActionResult.Processed) {
                // If the Apps item is not available then return false and stop processing because
                // it must exist.
                return false;
            }
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
            return false;
        }

        Log.i(functionName, "The starting of the application has successfully completed.");
        return true;
    }

    /**
     * Resets the external application by closing out the application, clearing all cache, and
     * getting the application back to the state of ready to watch videos.
     * @return A value will be returned indicating whether or not the reset has succeeded or not.
     */
    public boolean Reset() {
        String functionName = "Starter_Reset()";
        Log.i(functionName, "The function has started.");

        try {
            getUiDevice().pressHome();
            UiObject obj = new UiObject(new UiSelector().text("Apps"));
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
            listView.scrollTextIntoView("AppTrailers");
            obj = new UiObject(new UiSelector().text("AppTrailers"));
            if (obj.waitForExists(20000)) obj.click();
            obj = new UiObject(new UiSelector().text("Clear data"));
            if (obj.waitForExists(20000)) {
                // Sleep for a few seconds because it does take some time for the button to become
                // enabled.
                SystemClock.sleep(3000);
                if (obj.isEnabled()) {
                    obj.click();
                } else {
                    return true;
                }
            }
            obj = new UiObject(new UiSelector().text("OK"));
            if (obj.waitForExists(20000)) obj.click();
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
            return false;
        }

        Log.i(functionName, "The resetting of the application has successfully completed.");
        return true;
    }

    /**
     * This function will check apps item is available for selection.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForAppsItem() {
        String text = "Check out these app trailers!";
        String functionName = "Starter_checkForAppsItem()";
        Log.i(functionName, "The function has started.");
        try {
            // There may be other elements on the page that have the text "Apps", so attempt to select
            // the item with the description "Check out these app trailers!" instead.
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function found the object with the text '%s'.", text));
        return ActionResult.Found;
    }

    /**
     * This function will check if the App Trailers app is available.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForAppTrailersApp() {
        String text = "AppTrailers";
        String functionName = "Starter_checkForAppTrailersApp()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            Integer timeToWait = 4000;
            if (!obj.waitForExists(timeToWait)) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The function was able to locate the resource with the text '%s' but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function found the object with the text '%s'.", text));
        return ActionResult.Found;
    }

    /**
     * This function will check the Home icon is available for selection.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForHomeIcon() {
        String functionName = "Starter_checkForHomeIcon()";
        Log.i(functionName, "The function has started.");
        try {
            // Create the selector to get the tab control.
            String className = "android.widget.TabWidget";
            UiSelector tabSelector = new UiSelector().className(className);

            // Create a collection of the children of the tab control.
            UiCollection tabChildrenCollection = new UiCollection(tabSelector);

            // Get the number of children that were found.
            className = "android.widget.RelativeLayout";
            int childCount = tabChildrenCollection.getChildCount(new UiSelector().className(className));
            if (childCount == 0) {
                Log.i(functionName, "The function was not able to locate any tabs within the tab control.");
                return ActionResult.NotFound;
            }

            UiObject firstChild = tabChildrenCollection.getChildByInstance(new UiSelector().className(className), 0);
            if (!firstChild.exists()) {
                Log.i(functionName, "The first child within the tab control doesn't exist.");
                return ActionResult.NotFound;
            }

            if (!firstChild.isClickable()) {
                Log.i(functionName, "The first child within the tab control is not clickable.");
                return ActionResult.NotClickable;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, "The function found the Home icon.");
        return ActionResult.Found;
    }

    /**
     * This function will check an "OK" button on the Tip 3 Times dialog that may be present when
     * App Trailers first loads up.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForTip3TimesOkButton() {
        String resourceId = "android:id/button1";
        String functionName = "Starter_checkForTip3TimesOkButton()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
            Integer timeToWait = 4000;
            if (!obj.waitForExists(timeToWait)) {
                Log.i(functionName, String.format("The function waited '%d' seconds and was not able find the resource '%s' to click on.", timeToWait/1000, resourceId));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()) {
                Log.i(functionName, String.format("The function was able find the resource '%s' but it was not clickable.", resourceId));
                return ActionResult.NotClickable;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function found the object named '%s'.", resourceId));
        return ActionResult.Found;
    }

    /**
     * This function will check for an "OK" button that may be present when App Trailers
     * first loads up.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForWelcomeToAppTrailersOkButton() {
        String resourceId = "com.appredeem.apptrailers:id/btnOk";
        String functionName = "Starter_checkForWelcomeToAppTrailersOkButton()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
            Integer timeToWait = 4000;
            if (!obj.waitForExists(timeToWait)) {
                Log.i(functionName, String.format("The function waited '%d' seconds and was not able find the resource '%s' to click on.", timeToWait/1000, resourceId));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()) {
                Log.i(functionName, String.format("The function was able find the resource '%s' but it was not clickable.", resourceId));
                return ActionResult.NotClickable;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function found the object named '%s'.", resourceId));
        return ActionResult.Found;
    }

    /**
     * Returns the UI Device that should be processed.  This function was created to mimic the
     * "getUiDevice()" function that is found when a class inherits from "UiAutomatorTestCase".
     * @return The active UI Device is returned.
     */
    private UiDevice getUiDevice() {
        return _uiDevice;
    }

    /**
     * Processes the "Apps" list item that is on the Home tab.
     * @return The result of running this function will be returned.
     */
    private ActionResult processAppsItem() {
        String text = "Check out these app trailers!";
        String functionName = "Starter_processAppsItem()";
        Log.i(functionName, "The function has started.");
        try {
            // There may be other elements on the page that have the text "Apps", so attempt to select
            // the item with the description "Check out these app trailers!" instead.
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }
            obj.click();
            Log.i(functionName, String.format("The function found has successfully clicked on the resource with the text '%s'.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the AppTrailers app.
     * @return The result of running this function will be returned.
     */
    private ActionResult processAppTrailersApp() {
        String text = "AppTrailers";
        String functionName = "Starter_processAppTrailersApp()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The function was able to locate the resource with the text '%s' but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            obj.clickAndWaitForNewWindow();
            Log.i(functionName, String.format("The function found has successfully clicked on the object with the text '%s'.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the Home icon when  App Trailers first loads up.
     * @return The result of running this function will be returned.
     */
    private ActionResult processHomeIcon() {
        String functionName = "Starter_processHomeIcon()";
        Log.i(functionName, "The function has started.");
        try {
            // Create the selector to get the tab control.
            String className = "android.widget.TabWidget";
            UiSelector tabSelector = new UiSelector().className(className);

            // Create a collection of the children of the tab control.
            UiCollection tabChildrenCollection = new UiCollection(tabSelector);

            // Get the number of children that were found.
            className = "android.widget.RelativeLayout";
            int childCount = tabChildrenCollection.getChildCount(new UiSelector().className(className));
            if (childCount == 0) {
                Log.i(functionName, "The function was not able to locate any tabs within the tab control.");
                return ActionResult.NotFound;
            }

            UiObject firstChild = tabChildrenCollection.getChildByInstance(new UiSelector().className(className), 0);
            if (!firstChild.exists()) {
                Log.i(functionName, "The first child within the tab control doesn't exist.");
                return ActionResult.NotFound;
            }

            if (!firstChild.isClickable()) {
                Log.i(functionName, "The first child within the tab control is not clickable.");
                return ActionResult.NotClickable;
            }

            firstChild.click();
            Log.i(functionName, "The function found has successfully clicked on the Home tab.");
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the "OK" button on the Tip 3 Times dialog that may be present when  App Trailers
     * first loads up.
     * @return The result of running this function will be returned.
     */
    private ActionResult processTip3TimesOkButton() {
        String resourceId = "android:id/button1";
        String functionName = "Starter_processTip3TimesOkButton()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the id '%s'.", resourceId));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The function was able to locate the resource with the id '%s' but it was not clickable.", resourceId));
                return ActionResult.NotClickable;
            }
            obj.click();
            Log.i(functionName, String.format("The function found has successfully clicked on the resource with the id '%s'.", resourceId));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the "OK" button that may be present when App Trailers first loads up.
     * @return The result of running this function will be returned.
     */
    private ActionResult processWelcomeToAppTrailersOkButton() {
        String resourceId = "com.appredeem.apptrailers:id/btnOk";
        String functionName = "Starter_processWelcomeToAppTrailersOkButton()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the id '%s'.", resourceId));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The function was able to locate the resource with the id '%s' but it was not clickable.", resourceId));
                return ActionResult.NotClickable;
            }
            obj.click();
            Log.i(functionName, String.format("The function found has successfully clicked on the resource with the id '%s'.", resourceId));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }
}