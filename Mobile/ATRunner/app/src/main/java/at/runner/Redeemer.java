package at.runner;

import android.hardware.input.InputManager;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that will be in control of redeeming rewards.
 */
public class Redeemer {

    /**
     * Contains a reference to the logging class to write out log messages.  Please note that this
     * variable was named "Log" for a reason and not "_logger" which would normally be the name for
     * a private field.  By simply deleting this private variable and importing "import android.util.Log;"
     * all the references to the "Log" should still work without much rewrite.
     */
    private Logger Log;

    /**
     * Contains processor that will handle getting all the configuration data.
     */
    private ConfigurationProcessor _configurationProcessor;

    /**
     * Contains a reference to the UI Device that is being processed.
     */
    private UiDevice _uiDevice;

    /**
     * A constructor of a redeemer class.
     * @param logger The logging class that will be used to log messages.
     * @param uiDevice A reference to the UI device to interact with.
     * @param configurationProcessor The processor that will get configuration data.
     */
    public Redeemer(Logger logger, UiDevice uiDevice, ConfigurationProcessor configurationProcessor) {
        Log = logger;
        _uiDevice = uiDevice;
        _configurationProcessor = configurationProcessor;
    }

    /**
     * Checks to see if the "Redeem" button exists.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForRedeemButton() {
        String functionName = "Redeemer_checkForRedeemButton()";
        String text = "Redeem";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            Log.i(functionName, String.format("The button with the text '%s' was found.", text));
            return ActionResult.Found;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Checks to see if there is a Redeem tab on the screen.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForRedeemTab() {
        String functionName = "Redeemer_checkForRedeemTab()";
        String text = "Redeem";
        int triesMaximum = 4;
        Log.i(functionName, "The function has started.");
        boolean countIsZero = false;
        boolean exists = false;
        boolean clickable = false;
        try {
            for (int tries = 0; tries < triesMaximum; tries++) {
                // Create the selector to get the tab control.
                String className = "android.widget.TabWidget";
                UiSelector tabSelector = new UiSelector().className(className);

                // Create a collection of the children of the tab control.
                UiCollection tabChildrenCollection = new UiCollection(tabSelector);

                // Get the number of children that were found.
                className = "android.widget.RelativeLayout";
                int childCount = tabChildrenCollection.getChildCount(new UiSelector().className(className));
                countIsZero = childCount == 0;
                if (countIsZero) {
                    SystemClock.sleep(500);
                    Log.i(functionName, "The function was not able to locate any tabs within the tab control.");
                    getUiDevice().pressBack();
                    Log.i(functionName, "The function has pressed the back button.");
                    continue;
                }

                UiObject childByInstance = tabChildrenCollection.getChildByInstance(new UiSelector().className(className), 3);
                exists = childByInstance.exists();
                if (!exists) {
                    SystemClock.sleep(500);
                    Log.i(functionName, "The tab instance within the tab control was not able to be found.");
                    getUiDevice().pressBack();
                    Log.i(functionName, "The function has pressed the back button.");
                    continue;
                }

                clickable = childByInstance.isClickable();
                if (!clickable) {
                    SystemClock.sleep(500);
                    Log.i(functionName, "The tab instance within the tab control was not clickable.");
                    getUiDevice().pressBack();
                    Log.i(functionName, "The function has pressed the back button.");
                    continue;
                }
            }

            if (countIsZero) {
                Log.i(functionName, String.format("The tab instances were never found after '%d' tries.", triesMaximum));
                return ActionResult.NotFound;
            }

            if (!exists) {
                Log.i(functionName, String.format("The specific tab instance was never found after '%d' tries.", triesMaximum));
                return ActionResult.NotFound;
            }

            if (!clickable) {
                Log.i(functionName, String.format("The specific tab instance was found but it was not clickable after '%d' tries.", triesMaximum));
                return ActionResult.NotClickable;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the the resource with the text '%s'.", text));
        return ActionResult.Found;
    }

    /**
     * Checks to see if there is an Amazon.com item on the screen.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForAmazonRedemption() {
        String functionName = "Redeemer_checkForAmazonRedemption()";
        String text = "Amazon.com";
        String className = "android.widget.ListView";
        Log.i(functionName, "The function has started.");

        // Attempt to scroll to an item that has the value type.
        try {
            UiScrollable listView = new UiScrollable(new UiSelector().className(className).scrollable(true));
            if (!listView.exists()) {
                Log.i(functionName, String.format("The scrollable list view that should have a class of '%s' was not able to be located.", className));
                return ActionResult.NotFound;
            }
            if (!listView.scrollTextIntoView(text)) {
                Log.i(functionName, String.format("The function was not able to locate the text '%s'.", text));
                return ActionResult.NotFoundAfterSearching;
            }

            // Scroll down a little bit more just in case the item is not fully on the screen.
            getUiDevice().swipe(10, 150, 10, 100, 100);
        } catch (UiObjectNotFoundException e) {
            Log.i(functionName, String.format("The function encountered a handled exception was not able to locate the text '%s'.", text));
            return ActionResult.NotFound;
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the text '%s'.", text));
        return ActionResult.Found;
    }

    /**
     * Checks to see if the "Log In" button exists.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForLoginButton() {
        String functionName = "Redeemer_checkForLoginButton()";
        String text = "Log In";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            Log.i(functionName, String.format("The button with the text '%s' was found.", text));
            return ActionResult.Found;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Checks to see if the "Login with Facebook" button exists.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForLoginWithFacebook() {
        String functionName = "Redeemer_checkForLoginWithFacebook()";
        String text = "Login with Facebook";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            // Wait 2 minutes for this text to be available because the "Fetching Settings..." dialog may be present.
            if (!obj.waitForExists(1000 * 60 * 2)) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            // Do not check if the this item is clickable because it is not.
            //if (!obj.isClickable()){
            //    Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
            //    return ActionResult.NotClickable;
            //}
            Log.i(functionName, String.format("The button with the text '%s' was found.", text));
            return ActionResult.Found;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Checks to see if the "Logout of Facebook" button exists.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForLogoutOfFacebook() {
        String functionName = "Redeemer_checkForLogoutOfFacebook()";
        String text = "Logout of Facebook";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            // Wait 2 minutes for this text to be available because the "Fetching Settings..." dialog may be present.
            if (!obj.waitForExists(1000 * 60 * 2)) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            // Do not check if the this item is clickable because it is not.
            //if (!obj.isClickable()){
            //    Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
            //    return ActionResult.NotClickable;
            //}
            Log.i(functionName, String.format("The button with the text '%s' was found.", text));
            return ActionResult.Found;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Checks to see if the Facebook Login screen exists.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForFacebookLogin() {
        String functionName = "Redeemer_checkForFacebookLogin()";
        String className = "android.widget.ImageView";
        Log.i(functionName, "The function has started.");

        // The maximum wait time is set to 5 minutes just in case the loading screen
        // shows for an extended period of time.  Lets have this function only wait 5 minutes for
        // it to go away.
        long waitTime = 1000 * 60 * 5;
        long maximumWaitTime = SystemClock.currentThreadTimeMillis() + waitTime;

        try {
            while (true) {
                if (SystemClock.currentThreadTimeMillis() > maximumWaitTime) {
                    Log.e(functionName, String.format("The Facebook login screen was still not loaded after '%d' seconds.", waitTime/1000));
                    return ActionResult.NotFound;
                }

                UiObject obj = new UiObject(new UiSelector().className(className));
                if (!obj.exists()){
                    SystemClock.sleep(1000);
                    continue;
                }

                break;
            }

            Log.i(functionName, "The Facebook login screen is now showing.");
            return ActionResult.Found;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Checks and waits for the loading screen to go away.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForLoadingScreenShowing() {
        String functionName = "Redeemer_checkForLoadingScreenShowing()";
        String resourceId = "com.appredeem.apptrailers:id/first_loading_text";
        Log.i(functionName, "The function has started.");

        // The maximum wait time is set to 5 minutes just in case the loading screen
        // shows for an extended period of time.  Lets have this function only wait 5 minutes for
        // it to go away.
        long waitTime = 1000 * 60 * 5;
        long maximumWaitTime = SystemClock.currentThreadTimeMillis() + waitTime;

        try {
            while (true) {
                if (SystemClock.currentThreadTimeMillis() > maximumWaitTime) {
                    Log.e(functionName, String.format("The loading screen was shown for more than '%d' seconds so the process has ended in an exception.", waitTime/1000));
                    return ActionResult.Found;
                }

                //
                UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
                if (obj.exists()){
                    SystemClock.sleep(1000);
                    continue;
                }

                break;
            }

            Log.i(functionName, "The loading screen has stopped showing.");
            return ActionResult.NotFound;
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Checks to see if the "OK" button exists.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForOkButton() {
        String functionName = "Redeemer_checkForOkButton()";
        String className = "android.widget.ImageView";
        Log.i(functionName, "The function has started.");
        try {
            // Wait 2 seconds before even trying to search for the close button.
            SystemClock.sleep(2000);

            // Search for the close button that appears.
            UiObject obj = new UiObject(new UiSelector().className(className));
            if (!obj.waitForExists(20000)) {
                Log.i(functionName, String.format("The image was not able to be detected with the class name '%s'.", className));
                return ActionResult.NotFound;
            }

            // Even after the close button has been detected, wait 5 seconds.
            SystemClock.sleep(5000);

            Log.i(functionName, String.format("The image with the class name of '%s' was found.", className));
            return ActionResult.Found;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Checks to see if the "Yes, Please" button exists.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForYesPleaseButton() {
        String functionName = "Redeemer_checkForYesPleaseButton()";
        String text = "Yes, Please!";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.waitForExists(20000)) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            Log.i(functionName, String.format("The button with the text '%s' was found.", text));
            return ActionResult.Found;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Checks to see if the "No" button exists.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForNoButton() {
        String functionName = "Redeemer_checkForNoButton()";
        String text = "No";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.waitForExists(20000)) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            Log.i(functionName, String.format("The button with the text '%s' was found.", text));
            return ActionResult.Found;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Returns the collection of key codes for the specified string.
     * @return The list of key codes is returned, otherwise, null will be returned if the key codes
     * could not be determined.
     */
    private List<Integer[]> getKeyCodesFromString(String text) {
        String functionName = "Redeemer_getKeyCodesFromString()";
        Log.i(functionName, "The function has started.");
        try {
            List<Integer[]> keyCodes = new ArrayList();
            for (int i = 0; i < text.length(); i++) {
                Integer[] key = new Integer[2];
                switch (text.charAt(i)) {
                    case '0':
                        key[0] = KeyEvent.KEYCODE_0;
                        key[1] = 0;
                        break;
                    case '1':
                        key[0] = KeyEvent.KEYCODE_1;
                        key[1] = 0;
                        break;
                    case '2':
                        key[0] = KeyEvent.KEYCODE_2;
                        key[1] = 0;
                        break;
                    case '3':
                        key[0] = KeyEvent.KEYCODE_3;
                        key[1] = 0;
                        break;
                    case '4':
                        key[0] = KeyEvent.KEYCODE_4;
                        key[1] = 0;
                        break;
                    case '5':
                        key[0] = KeyEvent.KEYCODE_5;
                        key[1] = 0;
                        break;
                    case '6':
                        key[0] = KeyEvent.KEYCODE_6;
                        key[1] = 0;
                        break;
                    case '7':
                        key[0] = KeyEvent.KEYCODE_7;
                        key[1] = 0;
                        break;
                    case '8':
                        key[0] = KeyEvent.KEYCODE_8;
                        key[1] = 0;
                        break;
                    case '9':
                        key[0] = KeyEvent.KEYCODE_9;
                        key[1] = 0;
                        break;
                    case 'a':
                        key[0] = KeyEvent.KEYCODE_A;
                        key[1] = 0;
                        break;
                    case 'b':
                        key[0] = KeyEvent.KEYCODE_B;
                        key[1] = 0;
                        break;
                    case 'c':
                        key[0] = KeyEvent.KEYCODE_C;
                        key[1] = 0;
                        break;
                    case 'd':
                        key[0] = KeyEvent.KEYCODE_D;
                        key[1] = 0;
                        break;
                    case 'e':
                        key[0] = KeyEvent.KEYCODE_E;
                        key[1] = 0;
                        break;
                    case 'f':
                        key[0] = KeyEvent.KEYCODE_F;
                        key[1] = 0;
                        break;
                    case 'g':
                        key[0] = KeyEvent.KEYCODE_G;
                        key[1] = 0;
                        break;
                    case 'h':
                        key[0] = KeyEvent.KEYCODE_H;
                        key[1] = 0;
                        break;
                    case 'i':
                        key[0] = KeyEvent.KEYCODE_I;
                        key[1] = 0;
                        break;
                    case 'j':
                        key[0] = KeyEvent.KEYCODE_J;
                        key[1] = 0;
                        break;
                    case 'k':
                        key[0] = KeyEvent.KEYCODE_K;
                        key[1] = 0;
                        break;
                    case 'l':
                        key[0] = KeyEvent.KEYCODE_L;
                        key[1] = 0;
                        break;
                    case 'm':
                        key[0] = KeyEvent.KEYCODE_M;
                        key[1] = 0;
                        break;
                    case 'n':
                        key[0] = KeyEvent.KEYCODE_N;
                        key[1] = 0;
                        break;
                    case 'o':
                        key[0] = KeyEvent.KEYCODE_O;
                        key[1] = 0;
                        break;
                    case 'p':
                        key[0] = KeyEvent.KEYCODE_P;
                        key[1] = 0;
                        break;
                    case 'q':
                        key[0] = KeyEvent.KEYCODE_Q;
                        key[1] = 0;
                        break;
                    case 'r':
                        key[0] = KeyEvent.KEYCODE_R;
                        key[1] = 0;
                        break;
                    case 's':
                        key[0] = KeyEvent.KEYCODE_S;
                        key[1] = 0;
                        break;
                    case 't':
                        key[0] = KeyEvent.KEYCODE_T;
                        key[1] = 0;
                        break;
                    case 'u':
                        key[0] = KeyEvent.KEYCODE_U;
                        key[1] = 0;
                        break;
                    case 'v':
                        key[0] = KeyEvent.KEYCODE_V;
                        key[1] = 0;
                        break;
                    case 'w':
                        key[0] = KeyEvent.KEYCODE_W;
                        key[1] = 0;
                        break;
                    case 'x':
                        key[0] = KeyEvent.KEYCODE_X;
                        key[1] = 0;
                        break;
                    case 'y':
                        key[0] = KeyEvent.KEYCODE_Y;
                        key[1] = 0;
                        break;
                    case 'z':
                        key[0] = KeyEvent.KEYCODE_Z;
                        key[1] = 0;
                        break;
                    case 'A':
                        key[0] = KeyEvent.KEYCODE_A;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'B':
                        key[0] = KeyEvent.KEYCODE_B ;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'C':
                        key[0] = KeyEvent.KEYCODE_C;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'D':
                        key[0] = KeyEvent.KEYCODE_D;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'E':
                        key[0] = KeyEvent.KEYCODE_E;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'F':
                        key[0] = KeyEvent.KEYCODE_F;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'G':
                        key[0] = KeyEvent.KEYCODE_G;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'H':
                        key[0] = KeyEvent.KEYCODE_H;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'I':
                        key[0] = KeyEvent.KEYCODE_I;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'J':
                        key[0] = KeyEvent.KEYCODE_J;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'K':
                        key[0] = KeyEvent.KEYCODE_K;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'L':
                        key[0] = KeyEvent.KEYCODE_L;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'M':
                        key[0] = KeyEvent.KEYCODE_M;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'N':
                        key[0] = KeyEvent.KEYCODE_N;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'O':
                        key[0] = KeyEvent.KEYCODE_O;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'P':
                        key[0] = KeyEvent.KEYCODE_P;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'Q':
                        key[0] = KeyEvent.KEYCODE_Q;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'R':
                        key[0] = KeyEvent.KEYCODE_R;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'S':
                        key[0] = KeyEvent.KEYCODE_S;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'T':
                        key[0] = KeyEvent.KEYCODE_T;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'U':
                        key[0] = KeyEvent.KEYCODE_U;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'V':
                        key[0] = KeyEvent.KEYCODE_V;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'W':
                        key[0] = KeyEvent.KEYCODE_W;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'X':
                        key[0] = KeyEvent.KEYCODE_X;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'Y':
                        key[0] = KeyEvent.KEYCODE_Y;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case 'Z':
                        key[0] = KeyEvent.KEYCODE_Z;
                        key[1] = KeyEvent.META_SHIFT_LEFT_ON;
                        break;
                    case '@':
                        key[0] = KeyEvent.KEYCODE_AT;
                        key[1] = 0;
                        break;
                    case '.':
                        key[0] = KeyEvent.KEYCODE_PERIOD;
                        key[1] = 0;
                        break;
                    default:
                        Log.e(functionName, String.format("The text '%s' was not able to be converted to a valid key code.", text.substring(i, i+1)));
                        return null;
                }
                keyCodes.add(key);
            }

            return keyCodes;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return null;
        }
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
     * The main function that will process this class.
     * @return A value will be returned indicating whether or not the processing was a success or
     * not.
     */
    public boolean Process() {
        String functionName = "Redeemer_Process()";
        Log.i(functionName, "The function has started.");

        try {
            // Check for the redeem tab.
            ActionResult result = checkForRedeemTab();
            if (result != ActionResult.Found) {
                // If the redeem tab is not available then return false and stop processing because
                // it must exist.
                return false;
            }
            result = processRedeemTab();
            if (result != ActionResult.Processed) {
                // If the redeem tab is not available then return false and stop processing because
                // it must exist.
                return false;
            }

            result = checkForLoadingScreenShowing();
            if (result != ActionResult.NotFound) {
                return false;
            }

            // Check for the Amazon.com item.
            result = checkForAmazonRedemption();
            if (result != ActionResult.Found) {
                // If the Amazon.com item is not available then return false and stop processing because
                // it must exist.
                return false;
            }
            result = processAmazonRedemption();
            if (result != ActionResult.Processed) {
                // If the Amazon.com item  is not available then return false and stop processing because
                // it must exist.
                return false;
            }

            // Check for the "Redeem" button.
            result = checkForRedeemButton();
            if (result != ActionResult.Found) {
                // If the Amazon.com item is not available then return false and stop processing because
                // it must exist.
                return false;
            }
            result = processRedeemButton();
            if (result != ActionResult.Processed) {
                // If the Amazon.com item  is not available then return false and stop processing because
                // it must exist.
                return false;
            }

            result = checkForLoginButton();
            if (result == ActionResult.Exception){
                return false;
            } else if (result == ActionResult.Found) {
                result = processLoginButton();
                if (result == ActionResult.Exception) {
                    return false;
                } else if (result == ActionResult.Processed) {
                    result = checkForLoginWithFacebook();
                    if (result != ActionResult.Found){
                        return false;
                    }
                    result = processLoginWithFacebook();
                    if (result != ActionResult.Processed) {
                        return false;
                    }
                    result = checkForFacebookLogin();
                    if (result != ActionResult.Found){
                        return false;
                    }
                    result = processFacebookLogin();
                    if (result != ActionResult.Processed) {
                        return false;
                    }
                    result = checkForOkButton();
                    if (result != ActionResult.Found){
                        return false;
                    }
                    result = processOkButton();
                    if (result != ActionResult.Processed) {
                        return false;
                    }
                    result = checkForLogoutOfFacebook();
                    if (result != ActionResult.Found){
                        return false;
                    }

                    getUiDevice().pressBack();
                    Log.i(functionName, "The back button was press after the user was successfully logged in.");

                    result = checkForRedeemButton();
                    if (result != ActionResult.Found) {
                        return false;
                    }
                    result = processRedeemButton();
                    if (result != ActionResult.Processed) {
                        return false;
                    }
                } else {
                    Log.e(functionName, "An unknown state was found where the login button was not successfully processed.");
                    return false;
                }
            }

            result = checkForYesPleaseButton();
            if (result != ActionResult.Found) {
                return false;
            }
            result = processYesPleaseButton();
            if (result != ActionResult.Processed) {
                return false;
            }

            result = checkForNoButton();
            if (result != ActionResult.Found) {
                return false;
            }
            result = processNoButton();
            if (result != ActionResult.Processed) {
                return false;
            }

            // Sleep for three seconds before finishing the redemption process.
            SystemClock.sleep(3000);
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
            return false;
        }
        return true;
    }

    /**
     * Processes the "Redeem" button that will appear.
     * @return The result of running this function will be returned.
     */
    private ActionResult processRedeemButton() {
        String functionName = "Redeemer_processRedeemButton()";
        String text = "Redeem";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            obj.click();
            Log.i(functionName, String.format("The button with the text '%s' was found and successfully clicked on.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the redemption tab.
     * @return The result of running this function will be returned.
     */
    private ActionResult processRedeemTab() {
        String functionName = "Redeemer_processRedeemTab()";
        String text = "Redeem";
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

            UiObject childByInstance = tabChildrenCollection.getChildByInstance(new UiSelector().className(className), 3);
            if (!childByInstance.exists()) {
                Log.i(functionName, "The tab instance within the tab control was not able to be found.");
                return ActionResult.NotFound;
            }

            if (!childByInstance.isClickable()) {
                Log.i(functionName, "The tab instance within the tab control was not clickable.");
                return ActionResult.NotClickable;
            }

            childByInstance.click();

            Log.i(functionName, String.format("The function was able to click the the resource with the text '%s'.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the Amazon.com redemption item.
     * @return The result of running this function will be returned.
     */
    private ActionResult processAmazonRedemption() {
        String functionName = "Redeemer_processAmazonRedemption()";
        Log.i(functionName, "The function has started.");
        try {
            String text = "Amazon.com";
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }
            // There is no need to check if the item is clickable or not.  Just click on the UI object.
            //if (!obj.isClickable()){
            //    Log.i(functionName, String.format("The function was able to locate the resource with the text '%s' but it was not clickable.", text));
            //    return ActionResult.NotClickable;
            //}
            obj.click();
            Log.i(functionName, String.format("The function found has successfully clicked on the resource with the text '%s'.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the "Log In" button that may appear.
     * @return The result of running this function will be returned.
     */
    private ActionResult processLoginButton() {
        String functionName = "Redeemer_processLoginButton()";
        String text = "Log In";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            obj.click();
            Log.i(functionName, String.format("The button with the text '%s' was found and successfully clicked on.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the "Login with Facebook" button that may appear.
     * @return The result of running this function will be returned.
     */
    private ActionResult processLoginWithFacebook() {
        String functionName = "Redeemer_processLoginWithFacebook()";
        String text = "Login with Facebook";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            // Do not check if the this item is clickable because it is not.
            //if (!obj.isClickable()){
            //    Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
            //    return ActionResult.NotClickable;
            //}
            obj.click();
            Log.i(functionName, String.format("The button with the text '%s' was found and successfully clicked on.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the Facebook login screen.
     * @return The result of running this function will be returned.
     */
    private ActionResult processFacebookLogin() {
        String functionName = "Redeemer_processFacebookLogin()";
        Log.i(functionName, "The function has started.");
        try {
            // Since the following login screen is on a web browser within the app, there are no
            // controls to gain access to.  Therefore, the raw inputs must be used.

            // Scroll down a little bit more because the password entry box will be hidden when the
            // keyboard comes up.  Scrolling down will prevent this from happening.
            getUiDevice().swipe(100, 250, 100, 100, 100);

            // Click in the space that is for the users Email Address or Phone Number.
            getUiDevice().click(50, 100);
            Log.i(functionName, "The user name text box was successfully clicked on.");

            // Sleep for a half of a second to allow for the keyboard to appear on the phone.
            SystemClock.sleep(500);

//            sendText(_facebookUserName);
            List<Integer[]> keyCodes = getKeyCodesFromString(_configurationProcessor.GetFacebookUserName());
            for (Integer[] keyCode : keyCodes) {
                getUiDevice().pressKeyCode(keyCode[0], keyCode[1]);
                SystemClock.sleep(75);
            }
            Log.i(functionName, String.format("The user name text box had the text of '%s' successfully entered into the box.", _configurationProcessor.GetFacebookUserName()));

            // Click somewhere random to remove the suggestion drop down if one exists.
            getUiDevice().click(50, 2);

            // Sleep for a half of a second to allow for the keyboard to appear on the phone.
            SystemClock.sleep(500);

            // Click in the space that is for the users Password.
            getUiDevice().click(50, 150);
            Log.i(functionName, "The password text box was successfully clicked on.");

            // Sleep for a half of a second to allow for the keyboard to appear on the phone.
            SystemClock.sleep(500);

//            sendText(_facebookPassword);
            keyCodes = getKeyCodesFromString(_configurationProcessor.GetFacebookPassword());
            for (Integer[] keyCode : keyCodes) {
                getUiDevice().pressKeyCode(keyCode[0], keyCode[1]);
                SystemClock.sleep(75);
            }
            Log.i(functionName, String.format("The password text box had the text of '%s' successfully entered into the box.", _configurationProcessor.GetFacebookPassword()));

            // Click on the Log In button.
            getUiDevice().click(160, 215);

            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the "OK" button that may appear.
     * @return The result of running this function will be returned.
     */
    private ActionResult processOkButton() {
        String functionName = "Redeemer_processOkButton()";
        Log.i(functionName, "The function has started.");
        try {
            // Since the "OK" button is within a web page, we cannot gain access to the UI element
            // to click, but instead click on the screen where the button is.
            getUiDevice().click(225, 430);
            Log.i(functionName, "The user clicked on the 'OK' button.");
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the "Yes, Please" button that may appear.
     * @return The result of running this function will be returned.
     */
    private ActionResult processYesPleaseButton() {
        String functionName = "Redeemer_processYesPleaseButton()";
        String text = "Yes, Please!";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            obj.click();
            Log.i(functionName, String.format("The button with the text '%s' was found and successfully clicked on.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the "No" button that may appear.
     * @return The result of running this function will be returned.
     */
    private ActionResult processNoButton() {
        String functionName = "Redeemer_processNoButton()";
        String text = "No";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The button with the text '%s' was not able to be located..", text));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The button with the text '%s' was found but it was not clickable.", text));
                return ActionResult.NotClickable;
            }
            obj.click();
            Log.i(functionName, String.format("The button with the text '%s' was found and successfully clicked on.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }
}
