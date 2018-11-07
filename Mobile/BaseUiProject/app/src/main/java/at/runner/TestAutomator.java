package at.runner;

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
        getUiDevice().pressHome();
        String functionName = "main()";
        Log.i(functionName, "The test has begun.");
   }
}