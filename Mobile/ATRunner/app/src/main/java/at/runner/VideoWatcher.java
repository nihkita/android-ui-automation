package at.runner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.SystemClock;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * A class that will be in control of watching videos.
 */
public class VideoWatcher {

    /**
     * Contains the interval processor that is used throughout this class.
     */
    public IntervalProcessor IntervalProcessorMain;

    /**
     * Contains processor that will handle getting all the configuration data.
     */
    private ConfigurationProcessor _configurationProcessor;

    /**
     * Contains the absolute path and file name of the file to store a screen shot of the current
     * devices screen when the second pass is hit.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private String _secondPassFileName = "/sdcard/ATRunner/SecondPassScreenShot.png";

    /**
     * Contains the absolute path and file name of the file to store a screen shot of the current
     * devices screen when the third pass is hit.
     */
    private String _thirdPassFileName = "/sdcard/ATRunner/ThirdPassScreenShot.png";

    /**
     * Contains the absolute path and file name of the file to store all the pixels in of the third
     * pass file name.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private String _thirdPassPixelsFileName = "/sdcard/ATRunner/ThirdPassPixels.txt";

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
     * Contains the current points that the process has last detected.
     */
    public int CurrentPoints = -1;

    /**
     * Contains the ending points in which processing should stop at.
     */
    public int EndingPoints = -1;

    public boolean FinishedCheckingPlus10 = false;

    /**
     * Contains the total number of redemptions that have been processed.
     */
    public int RedemptionCount = 0;

    /**
     * Contains the starting points that were found for processing.
     */
    public int StartingPoints = -1;

    /**
     * A constructor of a video watcher class.
     * @param logger The logging class that will be used to log messages.
     * @param uiDevice A reference to the UI device to interact with.
     * @param configurationProcessor The processor that will get configuration data.
     */
    public VideoWatcher(Logger logger, UiDevice uiDevice, ConfigurationProcessor configurationProcessor) {
        Log = logger;
        _uiDevice = uiDevice;
        _configurationProcessor = configurationProcessor;
    }

    /**
     * The main function that will process this class.
     * @return A value will be returned indicating whether or not the processing was a success or
     * not.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean Process() {
        String functionName = "VideoWatcher_Process()";
        Log.i(functionName, "The function has started.");

        try {
            ActionResult result;
            boolean firstPass = false;
            boolean justPlayedPlus10 = false;
            boolean secondPass = false;

            long lastProcessedTime = System.currentTimeMillis();

            // There are cases were the progress bar is moved to the right but take too much time to
            // do so.  In that case, record the number of attempts that were made.
            int processedProgressBarCount = 0;

            while (true) {

                // Sleep for a half of a second before starting the checking process all over again.
                SystemClock.sleep(500);

                IntervalWeight intervalWeight = IntervalProcessorMain.GetIntervalWeight();

                // Check for the "Complete action using" dialog.
                result = checkForCompleteActionUsingDialog();
                if (result == ActionResult.Exception) {
                    return false;
                }
                if (result == ActionResult.Found) {
                    result = processCompleteActionUsingDialog();
                    if (result == ActionResult.Exception) {
                        return false;
                    }
                    if (result == ActionResult.Processed) {
                        lastProcessedTime = System.currentTimeMillis();
                        Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));
                        firstPass = false;
                        secondPass = false;
                        processedProgressBarCount = 0;

                        // Do not reset the just played +10 variable if the Can't Play Video is successfully processed.
                        //justPlayedPlus10 = false;
                        continue;
                    }
                }

                //TODO: Check for install popup dialog.  android:id/button_once

                // Check for the Can't play this video popup.
                result = checkForCantPlayVideo();
                if (result == ActionResult.Exception) {
                    return false;
                }
                if (result == ActionResult.Found) {
                    result = processCantPlayVideo();
                    if (result == ActionResult.Exception) {
                        return false;
                    }
                    if (result == ActionResult.Processed) {
                        lastProcessedTime = System.currentTimeMillis();
                        Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));
                        firstPass = false;
                        secondPass = false;
                        processedProgressBarCount = 0;

                        // Do not reset the just played +10 variable if the Can't Play Video is successfully processed.
                        //justPlayedPlus10 = false;
                        continue;
                    }
                }

                // Check for the watch button which will show up if a particular app was watched too
                // many times.
                result = checkForWatchButton();
                if (result == ActionResult.Exception) {
                    return false;
                }
                if (result == ActionResult.Found) {
                    Log.w(functionName, "There points button is now reading 'Watch' which means the application needs to be restarted.");
                    return false;
                }

                // Check for the watch video button.
                result = checkForWatchVideoButton();
                if (result == ActionResult.Exception) {
                    return false;
                }
                if (result == ActionResult.Found) {
                    UpdatePointVariables();
                    if (CurrentPoints > 1010) {
                        return true;
                    }
                    if (CurrentPoints + (RedemptionCount * 1000) >= EndingPoints) {
                        Log.i(functionName, String.format("Processing finished for today.  The current starting points was '%d', current points is '%d' with the redemption count of '%d' which is greater than or equal to the ending point of '%d'.", StartingPoints, CurrentPoints, RedemptionCount, EndingPoints));
                        return true;
                    }
                    if (intervalWeight == IntervalWeight.Light) {
                        // Sleep between 30 and 35 seconds.
                        Random r = new Random();
                        int sleepTime = r.nextInt(5000) + 30000;
                        SystemClock.sleep(sleepTime);
                        Log.i(functionName, String.format("The interval weight was light, so the processing is sleeping for '%d' seconds.", sleepTime/1000));
                    } else if (intervalWeight == IntervalWeight.Medium) {
                        // Sleep between 10 and 15 seconds.
                        Random r = new Random();
                        int sleepTime = r.nextInt(5000) + 10000;
                        SystemClock.sleep(sleepTime);
                        Log.i(functionName, String.format("The interval weight was medium, so the processing is sleeping for '%d' seconds.", sleepTime/1000));
                    }

                    result = processWatchVideoButton();
                    if (result == ActionResult.Exception) {
                        return false;
                    }
                    if (result == ActionResult.Processed) {

                        // There are cases where the clicking of the Watch Video button will not
                        // work.  In this case try clicking the button 3 additional times before
                        // resetting the application.
                        for (int clickCount = 0; clickCount < 3; clickCount++) {
                            // Sleep for 5 seconds before starting the checking process all over again.
                            SystemClock.sleep(2000);

                            result = checkForWatchVideoButton();
                            if (result == ActionResult.Exception) {
                                return false;
                            }
                            if (result == ActionResult.Found) {
                                result = processWatchVideoButton();
                                if (result == ActionResult.Exception) {
                                    return false;
                                }
                                if (result == ActionResult.Processed) {
                                    continue;
                                }
                            }
                        }

                        if (result == ActionResult.Processed) {
                            Log.w(functionName, "The 'Watch Video' button was clicked 4 times and was not able to successfully play the video.  Therefore, restart the application.");
                            return false;
                        }

                        lastProcessedTime = System.currentTimeMillis();
                        Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));
                        firstPass = false;
                        secondPass = false;
                        processedProgressBarCount = 0;

                        // Do not reset the just played +10 variable if the watch video button is successfully processed.
                        //justPlayedPlus10 = false;
                        continue;
                    }
                }

                // Check for a video control with a progress bar.
                if (intervalWeight == IntervalWeight.Heavy) {
                    result = checkForVideoControl();
                    if (result == ActionResult.Exception) {
                        return false;
                    }
                    if (result == ActionResult.Found) {
                        if (processedProgressBarCount < 2)
                        {
                            for (int videoControlTries = 0; videoControlTries < 3; videoControlTries++) {
                                result = processVideoControl();
                                if (result == ActionResult.Exception) {
                                    return false;
                                }
                                if (result == ActionResult.Processed) {
                                    result = checkForProcessBar();
                                    if (result == ActionResult.Exception) {
                                        return false;
                                    }
                                    if (result == ActionResult.Found) {
                                        result = processProgressBar();
                                        if (result == ActionResult.Exception) {
                                            return false;
                                        }
                                        if (result == ActionResult.Processed) {
                                            lastProcessedTime = System.currentTimeMillis();
                                            Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));
                                            firstPass = false;
                                            secondPass = false;
                                            justPlayedPlus10 = false;
                                            processedProgressBarCount++;
                                            break;
                                        }
                                    }
                                }

                                SystemClock.sleep(500);
                            }
                        }
                        if (result == ActionResult.Processed) {
                            continue;
                        }
                    }
                }

                // If a +10 video was just playing, check for the "Replay Video" button which should
                // appear and then go back to the main screen.
                if (justPlayedPlus10) {
                    result = checkForReplayButton();
                    if (result == ActionResult.Exception) {
                        return false;
                    }
                    if (result == ActionResult.Found) {
                        UpdatePointVariables();
                        if (CurrentPoints > 1010) {
                            return true;
                        }
                        if (CurrentPoints + (RedemptionCount * 1000) >= EndingPoints) {
                            Log.i(functionName, String.format("Processing finished for today.  The current starting points was '%d', current points is '%d' with the redemption count of '%d' which is greater than or equal to the ending point of '%d'.", StartingPoints, CurrentPoints, RedemptionCount, EndingPoints));
                            return true;
                        }
                        if (intervalWeight == IntervalWeight.Light) {
                            // Sleep between 30 and 35 seconds.
                            Random r = new Random();
                            int sleepTime = r.nextInt(5000) + 30000;
                            SystemClock.sleep(sleepTime);
                            Log.i(functionName, String.format("The interval weight was light, so the processing is sleeping for '%d' seconds for +10 videos.", sleepTime/1000));
                        } else if (intervalWeight == IntervalWeight.Medium) {
                            // Sleep between 10 and 15 seconds.
                            Random r = new Random();
                            int sleepTime = r.nextInt(5000) + 10000;
                            SystemClock.sleep(sleepTime);
                            Log.i(functionName, String.format("The interval weight was medium, so the processing is sleeping for '%d' seconds for +10 videos.", sleepTime/1000));
                        }

                        // Press the back button to return to the list of apps to search through.
                        getUiDevice().pressBack();
                        Log.i(functionName, "The +10 video has finished playing so the back button was pressed.");

                        lastProcessedTime = System.currentTimeMillis();
                        Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));
                        firstPass = false;
                        secondPass = false;
                        justPlayedPlus10 = false;
                        processedProgressBarCount = 0;
                        continue;
                    }
                }

                // Check for the apps to click on.
                if (!FinishedCheckingPlus10) {
                    result = checkForAppItem(ValueType.Plus10);
                    if (result == ActionResult.Exception) {
                        return false;
                    }
                    if (result == ActionResult.Found) {
                        result = processAppItem(ValueType.Plus10);
                        if (result == ActionResult.Exception) {
                            return false;
                        }
                        if (result == ActionResult.Processed) {
                            // If a +10 was found, then attempt to play the video up to 20 times.
                            for (int plus10Pass = 0; plus10Pass < 20; plus10Pass++) {
                                result = checkForReplayButton();
                                if (result == ActionResult.Exception) {
                                    return false;
                                }
                                if (result == ActionResult.Found) {
                                    result = checkForWatchButton();
                                    if (result == ActionResult.Exception) {
                                        return false;
                                    }
                                    if (result == ActionResult.Found) {
                                        Log.w(functionName, "There 'Watch' button was found so exit this function and restart.");
                                        return false;
                                    }
                                    result = processReplayButton();
                                    if (result == ActionResult.Exception) {
                                        return false;
                                    }
                                    if (result == ActionResult.Processed) {
                                        break;
                                    }
                                }
                                result = checkForWatchVideoButton();
                                if (result == ActionResult.Exception) {
                                    return false;
                                }
                                if (result == ActionResult.Found) {
                                    result = checkForWatchButton();
                                    if (result == ActionResult.Exception) {
                                        return false;
                                    }
                                    if (result == ActionResult.Found) {
                                        Log.w(functionName, "There 'Watch' button was found so exit this function and restart.");
                                        return false;
                                    }
                                    result = processWatchVideoButton();
                                    if (result == ActionResult.Exception) {
                                        return false;
                                    }
                                    if (result == ActionResult.Processed) {
                                        break;
                                    }
                                }

                                // Sleep a half of a second between each check.
                                SystemClock.sleep(500);
                            }

                            // Update the last processed time after searching because it could take a
                            // while and we would not want the back button being pressed if it takes
                            // to long.
                            lastProcessedTime = System.currentTimeMillis();
                            Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));

                            if (result == ActionResult.Processed) {
                                firstPass = false;
                                secondPass = false;
                                justPlayedPlus10 = true;
                                processedProgressBarCount = 0;
                                continue;
                            }
                        }
                    } else if (result == ActionResult.NotFoundAfterSearching) {
                        // Update the last processed time after searching because it could take a
                        // while and we would not want the back button being pressed if it takes
                        // to long.
                        lastProcessedTime = System.currentTimeMillis();
                        Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));
                        FinishedCheckingPlus10 = true;
                    }
                } else {
                    result = checkForAppItem(ValueType.Plus5);
                    if (result == ActionResult.Exception) {
                        return false;
                    }
                    if (result == ActionResult.Found) {
                        result = processAppItem(ValueType.Plus5);
                        if (result == ActionResult.Exception) {
                            return false;
                        }
                        if (result == ActionResult.Processed) {
                            lastProcessedTime = System.currentTimeMillis();
                            Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));
                            firstPass = false;
                            secondPass = false;
                            justPlayedPlus10 = false;
                            processedProgressBarCount = 0;
                            continue;
                        }
                    }
                }

                // Check if the last time something was processed was greater than 60 seconds.
                if (!firstPass && (lastProcessedTime + 60000 < System.currentTimeMillis())) {
                    Log.i(functionName, String.format("The last processed time is '%d' and the current time is '%d' which is greater than 60 seconds.", lastProcessedTime, System.currentTimeMillis()));
                    // Record that the first pass was hit.
                    firstPass = true;
                    justPlayedPlus10 = false;

                    // In some cases, pressing the back button will solve the problem.
                    getUiDevice().pressBack();
                    Log.i(functionName, "The back button is being pressed because there has been no activity for the past 60 seconds.");
                    continue;
                }

                // Check if the last time something was processed was greater than 70 seconds.
                if (!secondPass && (lastProcessedTime + 70000 < System.currentTimeMillis())) {
                    Log.i(functionName, String.format("The last processed time is '%d' and the current time is '%d' which is greater than 70 seconds.", lastProcessedTime, System.currentTimeMillis()));
                    // Record that the second pass was hit.
                    secondPass = true;
                    justPlayedPlus10 = false;

                    // Save a second shot just in case the future pass hits.
                    getUiDevice().takeScreenshot(new File(_secondPassFileName));

                    // In some cases, pressing the back button will solve the problem.
                    getUiDevice().pressBack();
                    Log.i(functionName, "The back button is being pressed because there has been no activity for the past 70 seconds.");
                    continue;
                }

                // Check if the last time something was processed was greater than 80 seconds.
                if (lastProcessedTime + 80000 < System.currentTimeMillis()) {
                    getUiDevice().takeScreenshot(new File(_thirdPassFileName));

                    // The two screen shots should match at this point because there are no simple actions
                    // up to this point to continue processing.  There is only a complicated way to continue
                    // processing in which case the screen should not have changed.
                    if (!areTwoImagesEqual(_secondPassFileName, _thirdPassFileName)) {
                        Log.e(functionName, String.format("The two screen shots at '%s' and '%s' should match at this point but they are different.", _secondPassFileName, _thirdPassFileName));
                    }

                    // Pressing the back button will not work because it was already attempted in previous
                    // passes.
                    result = checkForSkipSurveyButton();
                    if (result == ActionResult.Exception) {
                        return false;
                    }
                    if (result == ActionResult.Found) {
                        result = processSkipSurveyButton();
                        if (result == ActionResult.Exception) {
                            return false;
                        }
                        if (result == ActionResult.Processed) {
                            lastProcessedTime = System.currentTimeMillis();
                            Log.i(functionName, String.format("The last processed time has been updated to '%d'.", lastProcessedTime));
                            firstPass = false;
                            secondPass = false;
                            justPlayedPlus10 = false;
                            processedProgressBarCount = 0;
                            continue;
                        } else {
                            Log.e(functionName, "The skip survey button was not successfully processed.");
                            return false;
                        }
                    } else {
                        Log.e(functionName, "The skip survey button was not successfully found on the screen shots.");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }
        return true;
    }

    public boolean UpdatePointVariables() {
        String functionName = "VideoWatcher_UpdatePointVariables()";
        Log.i(functionName, "The function has started.");

        try{
            ActionResult result = checkForPointsControl();
            if (result == ActionResult.Exception) {
                return false;
            }
            if (result == ActionResult.Found) {
                CurrentPoints = getCurrentPoints();
                Log.i(functionName, String.format("The current points are '%d'.", CurrentPoints));

                // If the current points is greater than zero, then that means that the points has
                // been successfully received where as
                if (CurrentPoints == -1) {
                    Log.e(functionName, "There current points is -1 which means there was an exception with getting the current points.");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }
        return true;
    }

    /**
     * Checks to see if two images are equal.
     * @param firstFileName The absolute file path and name of one image to be checked.
     * @param secondFileName The absolute file path and name of a second image to be checked.
     * @return true will be returned if they are equal and false will be returned if they are different.
     */
    private boolean areTwoImagesEqual(String firstFileName, String secondFileName) {
        String functionName = "VideoWatcher_areTwoImagesEqual()";
        Log.i(functionName, "The function has started.");

        try {
            int[][] firstFilePixels = getPixelsFromImage(firstFileName);
            int[][] secondFilePixels = getPixelsFromImage(secondFileName);

            // If the pixels for the first file is null, then return false because there was some
            // sort of exception.
            if (firstFilePixels == null) {
                Log.i(functionName, "The first files pixels were null so the images are not equal.");
                return false;
            }
            if (firstFilePixels.length == 0) {
                Log.i(functionName, "The first files pixels were empty so the images are not equal.");
                return false;
            }

            // If the pixels for the second file is null, then return false because there was some
            // sort of exception.
            if (secondFilePixels == null) {
                Log.i(functionName, "The second files pixels were null so the images are not equal.");
                return false;
            }
            if (secondFilePixels.length == 0) {
                Log.i(functionName, "The second files pixels were empty so the images are not equal.");
                return false;
            }

            // Make sure the number of pixels are the same in both images.
            if (firstFilePixels.length != secondFilePixels.length){
                Log.i(functionName, String.format("The number of pixels in the first file is '%d' and the number of pixels in the second file is '%d' which are different.", firstFilePixels.length, secondFilePixels.length));
                return false;
            }


            for (int row = 0; row < firstFilePixels.length; row++) {
                for (int col = 0; col < firstFilePixels[row].length; col++) {
                    if (firstFilePixels[row][col] != secondFilePixels[row][col]) {
                        Log.i(functionName, String.format("The pixel at the row '%d' and column '%d' was '%d' for the first file and '%d' for the second file so they are different.", row, col, firstFilePixels[row][col], secondFilePixels[row][col]));
                        return false;
                    }
                }
            }

            Log.i(functionName, "The two file were found to be the same.");
            return true;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }

        return false;
    }

    /**
     * Checks to see if there is an app with the specific value type located on the screen.
     * @param valueType The type of app to check for.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForAppItem(ValueType valueType) {
        String functionName = "VideoWatcher_checkForAppItem()";
        // Determine if a "+5" or a "+10" text should be searched.
        String searchText;
        if (valueType == ValueType.Plus5) {
            searchText = "+5";
        } else if (valueType == ValueType.Plus10) {
            searchText = "+10";
        } else {
            Log.i(functionName, "The function has failed to start because the valueType parameter is not able to be handled.");
            return ActionResult.Exception;
        }
        functionName = String.format("VideoWatcher_checkForAppItem(%s)", searchText);
        Log.i(functionName, "The function has started.");

        // Attempt to scroll to an item that has the value type.
        try {
            // This resource must be on the screen in order to select an app.
            String resourceId = "com.appredeem.apptrailers:id/pull_refresh_list";
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource id '%s'.", resourceId));
                return ActionResult.NotFound;
            }

            UiScrollable listView = new UiScrollable(new UiSelector().scrollable(true));
            if (!listView.scrollTextIntoView(searchText)) {
                Log.i(functionName, String.format("The function was not able to locate the text '%s'.", searchText));
                return ActionResult.NotFoundAfterSearching;
            }

            // Scroll down a little bit more because there are scenarios where the the list view will scroll to a +10 but it will not
            // actually be on the screen to be clicked on.  Therefore, scroll down a little bit more.
            getUiDevice().swipe(10, 150, 10, 100, 100);
        } catch (UiObjectNotFoundException e) {
            Log.i(functionName, String.format("The function encountered a handled exception was not able to locate the text '%s'.", searchText));
            return ActionResult.NotFound;
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the text '%s'.", searchText));
        return ActionResult.Found;
    }

    /**
     * Checks to see if a progress bar is present.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForProcessBar() {
        String className = "android.widget.SeekBar";
        String functionName = "VideoWatcher_checkForProcessBar()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().className(className));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function has not found the class named '%s'.", className));
                return ActionResult.NotFound;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the class named '%s'.", className));
        return ActionResult.Found;
    }

    /**
     * Checks to see if there is a dialog stating it cannot play a video.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForCantPlayVideo() {
        String functionName = "VideoWatcher_checkForCantPlayVideo()";
        Log.i(functionName, "The function has started.");

        try {
            String text = "Can't play this video.";

            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the text '%s'.", text));
                return ActionResult.NotFound;
            }

            String resourceId = "android:id/button1";
            obj = new UiObject(new UiSelector().resourceId(resourceId));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource id '%s'.", resourceId));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The function found the resource id '%s' but it was not clickable.", resourceId));
                return ActionResult.NotFound;
            }
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, "The function was able a window which states it cannot play videos.");
        return ActionResult.Found;
    }

    /**
     * Checks to see if the "Complete action using" dialog is present.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForCompleteActionUsingDialog() {
        String functionName = "VideoWatcher_checkForCompleteActionUsingDialog()";
        String resourceId = "android:id/button_once";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the id '%s'.", resourceId));
                return ActionResult.NotFound;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the the resource with the id '%s'", resourceId));
        return ActionResult.Found;
    }

    /**
     * Checks to see if there is a points control is available.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForPointsControl() {
        String resourceId = "com.appredeem.apptrailers:id/header_points";
        String functionName = "VideoWatcher_checkForPointsControl()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));

            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able find the resource with the id '%s'.", resourceId));
                return ActionResult.NotFound;
            }

            String text = obj.getText().trim();
            if (text.equals("Watch")) {
                Log.i(functionName, String.format("The function was able to locate the resource with the id '%s' but the text was 'Watch'.", resourceId));
                return ActionResult.NotFound;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the the resource with the id '%s'.", resourceId));
        return ActionResult.Found;
    }

    /**
     * Checks to see if there is a 'Replay Video' button present.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForReplayButton() {
        String functionName = "VideoWatcher_checkForReplayButton()";
        String text = "Replay Video";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }

            if (!obj.isClickable()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the the resource with the text '%s'.", text));
        return ActionResult.Found;
    }

    /**
     * Checks to see if there is a skip survey button available.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForSkipSurveyButton() {
        String functionName = "VideoWatcher_checkForSkipSurveyButton()";
        Log.i(functionName, "The function has started.");
        try {
            int[][] pixels = getPixelsFromImage(_thirdPassFileName);
            if (pixels == null) {
                Log.e(functionName, String.format("The pixels for the image '%s' was not able to be determined.", _thirdPassFileName));
                return ActionResult.Exception;
            }

            try {
                File file = new File(_thirdPassPixelsFileName);
                FileWriter writer = new FileWriter(file, true);
                //noinspection TryFinallyCanBeTryWithResources
                try {
                    for (int[] pixelsInRow : pixels) {
                        for (int col = 0; col < pixelsInRow.length; col++) {
                            int pixel = pixelsInRow[col];
                            int red = Color.red(pixel);
                            int green = Color.blue(pixel);
                            int blue = Color.blue(pixel);
                            writer.append(String.format("[%03d,%03d,%03d]", red, green, blue));
                            // Always append a comma to the file unless the current column is the last column.
                            if (col + 1 < pixelsInRow.length) {
                                writer.append(",");
                            }
                        }
                    }
                    writer.flush();
                } finally {
                    writer.close();
                }
            } catch (IOException e) {
                Log.e(functionName, String.format("The function encountered an exception when writing to the file '%s'.", _thirdPassPixelsFileName), e);
                return ActionResult.Exception;
            }

            // TODO: The application will need to check for the "Skip Survey" button within the image to click it.
            Log.e(functionName, "The application has haulted because the skip survey function was unable to process any further.");
            return ActionResult.Exception;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        // TODO: Uncomment out these lines of code when the "Skip Survey" function is flushed out.
//        Log.i(functionName, "The function was able to locate the skip survey button.");
//        return ActionResult.Found;
    }

    /**
     * Checks to see if a video control is present.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForVideoControl() {
        String className = "android.widget.VideoView";
        String functionName = "VideoWatcher_checkForVideoControl()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().className(className));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function has not found the class named '%s'.", className));
                return ActionResult.NotFound;
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the class named '%s'.", className));
        return ActionResult.Found;
    }

    /**
     * Checks to see if there is a 'Watch' button present.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForWatchButton() {
        String functionName = "VideoWatcher_checkForWatchButton()";
        String resourceId = "com.appredeem.apptrailers:id/appsDetail_appPointsIconText";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the id '%s'.", resourceId));
                return ActionResult.NotFound;
            }

            String text = obj.getText().trim();
            if (!text.equals("Watch")) {
                Log.i(functionName, String.format("The function was able to locate the resource with the id '%s' but the text was '%s' and not 'Watch'.", resourceId, text));
                return ActionResult.NotFound;
            }

        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the the resource with the id '%s' and it was equal to 'Watch'.", resourceId));
        return ActionResult.Found;
    }

    /**
     * Checks to see if there is a button on the screen to watch video.
     * @return The result of running this function will be returned.
     */
    private ActionResult checkForWatchVideoButton(){
        String functionName = "VideoWatcher_checkForWatchVideoButton()";
        String resourceId = "com.appredeem.apptrailers:id/appsDetail_btnWatchVideo";
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
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, String.format("The function was able to locate the the resource with the id '%s'.", resourceId));
        return ActionResult.Found;
    }

    /**
     * Gets the bitmap representation of a file.
     * @param fileName The name of the image file to get the bitmap for.
     * @return An array of all the pixels that were found are returned; otherwise, null will be returned.
     */
    private Bitmap getBitmapFromImage(String fileName) {
        String functionName = "VideoWatcher_getBitmapFromImage()";
        Log.i(functionName, "The function has started.");
        try{
            FileInputStream stream = new FileInputStream(fileName);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int r = stream.read(buffer);
                if (r == -1) break;
                out.write(buffer, 0, r);
            }
            byte[] bytes = out.toByteArray();
            out.close();
            stream.close();

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (FileNotFoundException e) {
            Log.e(functionName, String.format("The function has ended because the file '%s' was not found.", fileName), e);
        } catch (IOException e) {
            Log.e(functionName, String.format("The function has ended because there was an IO failure with the file '%s'.", fileName), e);
        } catch (Exception e){
            Log.e(functionName, String.format("The function has ended in a failure when trying to access the file '%s'.", fileName), e);
        }
        return null;
    }

    /**
     * Returns the total points that the user currently has available.
     * @return An integer of the current points that the user has available.  If zero is returned,
     * then the points was not able to be determined, and if negative one is returned, there was
     * an exception.
     */
    private int getCurrentPoints() {
        String functionName = "VideoWatcher_getCurrentPoints()";
        Log.i(functionName, "The function has started.");
        try {
            // The time to wait is 2 seconds because the current points may be zero and have not
            // fully updated yet.
            int timeToWait = 2000;

            // Get the limit in milliseconds so that we can figure out if the timeToWait has
            // elapsed or not based on the current time in milliseconds.
            long limitMilliseconds = System.currentTimeMillis() + timeToWait;

            // A variable to see whether or not this is the first check or not.
            boolean firstCheck = true;

            SystemClock.sleep(500);

            // Keep looping until the current time has surpassed 15 seconds.
            for (long currentMilliseconds = System.currentTimeMillis(); currentMilliseconds < limitMilliseconds; currentMilliseconds = System.currentTimeMillis()) {
                Log.i(functionName, String.format("The current milliseconds is '%d' and the limit is '%d'.", currentMilliseconds, limitMilliseconds));

                // Sleep for a short time to allow some time before attempting to get the
                // current point, just in case the first pass is zero points.
                if (!firstCheck) {
                    SystemClock.sleep(200);
                }

                String resourceId = "com.appredeem.apptrailers:id/header_points";
                UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));

                if (!obj.exists()) {
                    Log.i(functionName, String.format("The function was not able find the resource '%s' to click on.", resourceId));
                    return 0;
                }

                Log.i(functionName, String.format("The function found the header points object with the resource id of '%s' with the text '%s'.", resourceId, obj.getText()));

                // Make sure the string is trimmed because if there are spaces at the end or at the
                // beginning, then it will not be able to be parsed.
                int points = Integer.parseInt(obj.getText().trim());

                // The total number of points should not be zero and if it is zero, then continue
                // to wait.
                if (points <= 0){
                    Log.i(functionName, "The total point that were found is zero which indicates that the points has not been fully updated.");
                    firstCheck = false;
                    continue;
                }

                Log.i(functionName, String.format("The function found the current points to be %d.", points));
                return points;
            }

            Log.i(functionName, String.format("The function waited '%d' seconds and was not able find points that is greater than zero.", timeToWait/1000));
            return 0;

        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }
        return -1;
    }

    /**
     * Gets an array representation of the pixels within the file.
     * @param fileName The name of the image file to get the pixels for.
     * @return An array of all the pixels that were found are returned; otherwise, null will be returned.
     */
    private int[][] getPixelsFromImage(String fileName) {
        String functionName = "VideoWatcher_getPixelsFromImage()";
        Log.i(functionName, "The function has started.");
        try{
            Bitmap bmp = getBitmapFromImage(fileName);
            if (bmp == null) {
                Log.e(functionName, String.format("The function has ended because the bitmap from the file '%s' was not able to be determined.", fileName));
                return null;
            }

            int width = bmp.getWidth();
            int height = bmp.getHeight();
            int[] pixelsOneDimension = new int[height * width];
            bmp.getPixels(pixelsOneDimension, 0, width, 0, 0, width, height);

            int[][] pixelsTwoDimension = new int[height][width];

            for (int row = 0; row < height; row++) {
                System.arraycopy(pixelsOneDimension, row * width, pixelsTwoDimension[row], 0, width);
            }

            Log.i(functionName, String.format("The function ended successfully finding '%d' pixels.", pixelsOneDimension.length));
            return pixelsTwoDimension;
        } catch (Exception e){
            Log.e(functionName, String.format("The function has ended in a failure when trying to access the file '%s'.", fileName), e);
        }
        return null;
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
     * Processes an app with the specific value type located on the screen.
     * @param valueType The type of app to be processed.
     * @return The result of running this function will be returned.
     */
    private ActionResult processAppItem(ValueType valueType) {
        String functionName = "VideoWatcher_processAppItem()";
        // Determine if a "+5" or a "+10" text should be searched.
        String searchText;
        if (valueType == ValueType.Plus5) {
            searchText = "+5";
        } else if (valueType == ValueType.Plus10) {
            searchText = "+10";
        } else {
            Log.i(functionName, "The function has failed to start because the valueType parameter is not able to be handled.");
            return ActionResult.Exception;
        }

        functionName = String.format("VideoWatcher_processAppItem(%s)", searchText);
        Log.i(functionName, "The function has started.");

        // At this point, within the current view should be a "+5" or a "+10" item.
        try {
            UiObject obj = new UiObject(new UiSelector().text(searchText));
            if (obj.exists()) {
                Log.i(functionName, String.format("The function found the object with the text '%s'.", searchText));
                obj.click();
                Log.i(functionName, String.format("The function found has successfully clicked on the text '%s'.", searchText));
                return ActionResult.Processed;
            }
            Log.i(functionName, String.format("The function was not able to find a resource with the text '%s'.", searchText));
            return ActionResult.NotFound;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }
        return ActionResult.Exception;
    }

    /**
     * Processes the Can't Play Video popup.
     * @return The result of running this function will be returned.
     */
    private ActionResult processCantPlayVideo() {
        String functionName = "VideoWatcher_processCantPlayVideo()";
        Log.i(functionName, "The function has started.");

        try {
            String resourceId = "android:id/button1";
            UiObject obj = new UiObject(new UiSelector().resourceId(resourceId));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource id '%s'.", resourceId));
                return ActionResult.NotFound;
            }
            if (!obj.isClickable()){
                Log.i(functionName, String.format("The function found the resource id '%s' but it was not clickable.", resourceId));
                return ActionResult.NotFound;
            }
            obj.click();
            Log.i(functionName, String.format("The function found has successfully clicked on the resource id '%s'.", resourceId));
            return ActionResult.Processed;
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
        }
        return ActionResult.Exception;
    }

    /**
     * Processes the "Complete action using" dialog.
     * @return The result of running this function will be returned.
     */
    private ActionResult processCompleteActionUsingDialog() {
        String functionName = "VideoWatcher_processCompleteActionUsingDialog()";
        Log.i(functionName, "The function has started.");
        try {
            getUiDevice().pressBack();
            Log.i(functionName, "The back button was pressed to close the complete action using dialog..");
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the progress bar.
     * @return The result of running this function will be returned.
     */
    private ActionResult processProgressBar() {
        String className = "android.widget.SeekBar";
        String functionName = "VideoWatcher_processProgressBar()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().className(className));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function has not found the class named '%s'.", className));
                return ActionResult.NotFound;
            }
            if (!obj.swipeRight(1)) {
                Log.i(functionName, "The sliding of the progress bar failed.");
                return ActionResult.NotProcessed;
            }
            // After scrolling to the end, wait a little bit because the progress bar will actually
            // move back and need some time before completing.
            SystemClock.sleep(1700);
        } catch (UiObjectNotFoundException e){
            Log.i(functionName, String.format("The function has encountered a handled exception and has not able to find the class named '%s'.", className));
            return ActionResult.NotFound;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        Log.i(functionName, "The progress bar was successfully moved to the right.");
        return ActionResult.Processed;
    }

    /**
     * Processes the 'Replay Video' button.
     * @return The result of running this function will be returned.
     */
    private ActionResult processReplayButton() {
        String functionName = "VideoWatcher_processReplayButton()";
        String text = "Replay Video";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().text(text));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }

            if (!obj.isClickable()) {
                Log.i(functionName, String.format("The function was not able to locate the resource with the text '%s'.", text));
                return ActionResult.NotFound;
            }

            obj.click();
            Log.i(functionName, String.format("The function found has successfully clicked on the text '%s'.", text));
            return ActionResult.Processed;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }
    }

    /**
     * Processes the Skip Survey button.
     * @return The result of running this function will be returned.
     */
    private ActionResult processSkipSurveyButton() {
        String functionName = "VideoWatcher_processSkipSurveyButton()";

        // TODO: Fill out this function.
        Log.e(functionName, "The function has not been defined and should be getting called.");
        return ActionResult.Exception;
    }

    /**
     * Processes the video control.
     * @return The result of running this function will be returned.
     */
    private ActionResult processVideoControl() {
        String className = "android.widget.VideoView";
        String functionName = "VideoWatcher_processVideoControl()";
        Log.i(functionName, "The function has started.");
        try {
            UiObject obj = new UiObject(new UiSelector().className(className));
            if (!obj.exists()) {
                Log.i(functionName, String.format("The function has not found the class named '%s'.", className));
                return ActionResult.NotFound;
            }

            Rect bounds = obj.getBounds();
            int x = bounds.left;
            int y = bounds.top;
            Log.i(functionName, String.format("The left (%d) and top (%d) corner of the class was found.", x, y));

            x += 10;
            y += 10;
            getUiDevice().click(x, y);
            Log.i(functionName, String.format("The left (%d) and top (%d) position was clicked.", x, y));
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return ActionResult.Exception;
        }

        return ActionResult.Processed;
    }

    /**
     * Processes the watch video button.
     * @return The result of running this function will be returned.
     */
    private ActionResult processWatchVideoButton() {
        String functionName = "VideoWatcher_processWatchVideoButton()";
        Log.i(functionName, "The function has started.");
        try {
            String resourceId = "com.appredeem.apptrailers:id/appsDetail_btnWatchVideo";
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
