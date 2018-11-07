package at.runner;

import android.os.SystemClock;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import java.util.Calendar;
import java.util.Random;

public class TestAutomator extends UiAutomatorTestCase {

    private Logger Log;

    public void testAutomate() throws UiObjectNotFoundException {

        Random random = new Random();

        Log = new Logger(Common.GetStorageDirectory(), "atrunner");

        String functionName = "TestAutomator_testAutomate()";
        Log.i(functionName, "The test is starting.");

        // A local variable that will be used to store the current redemption count.  This way if
        // there is an exception, the redemption count is retained.
        int currentRedemptionsCount = 0;

        // The starting points that was detected before processing.
        int currentStartingPoints = -1;

        // A local variable that will contain the total amount of points that should be made while
        // processing.   This is different than the ending points.  This is the amount of points
        // from the starting points which will equal the ending points.
        int pointsToMake = -1;

        boolean finishedCheckingPlus10 = false;

        IntervalProcessor intervalProcessor = null;

        while (true)
        {
            try
            {
                ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(Log);

                Calendar c = Calendar.getInstance();

                // The hour of the day is in 24 hour format.
                int currentHour = c.get(c.HOUR_OF_DAY);

                String currentDateString = StringUtils.formatCalendarInstanceShortDate(c);

                if (currentHour < configurationProcessor.GetStartingHour()) {
                    Log.i(functionName, String.format("The process is sleeping for 10 minutes because the current hour is '%d' and must be greater than '%d' to continue processing.", currentHour, configurationProcessor.GetStartingHour()));
                    SystemClock.sleep(1000 * 60 * 10);
                    continue;
                }
                if (configurationProcessor.GetEndingHour() < currentHour) {
                    Log.i(functionName, String.format("The process is sleeping for 10 minutes because the current hour is '%d' and must be less than '%d' to continue processing.", currentHour, configurationProcessor.GetEndingHour()));
                    SystemClock.sleep(1000 * 60 * 10);
                    continue;
                }
                if (configurationProcessor.GetSuccessfullyProcessedDates().contains(currentDateString)) {
                    Log.i(functionName, String.format("The process is sleeping for 10 minutes because the date of '%s' has already successfully been processed.", currentDateString));
                    SystemClock.sleep(1000 * 60 * 10);
                    finishedCheckingPlus10 = false;
                    intervalProcessor = null;
                    continue;
                }

                if (intervalProcessor == null) {
                    intervalProcessor = new IntervalProcessor(Log, configurationProcessor);
                }

                Starter starter = new Starter(Log, getUiDevice());
                if (!starter.Reset()) {
                    // If there are any problems resetting the application, then exit.
                    return;
                }
                if (!starter.Process()) {
                    // If there are any problems starting up the application, then exit.
                    return;
                }

                Redeemer redeemer = new Redeemer(Log, getUiDevice(), configurationProcessor);
                VideoWatcher videoWatcher = new VideoWatcher(Log, getUiDevice(), configurationProcessor);

                // Update the starting points that found.
                if (currentStartingPoints == -1) {
                    if (!videoWatcher.UpdatePointVariables()) {
                        Log.e(functionName, "The processing failed to update the points variables.");
                        return;
                    }
                    currentStartingPoints = videoWatcher.CurrentPoints;
                    Log.i(functionName, String.format("The starting points has been updated to be '%d'.", currentStartingPoints));
                }
                videoWatcher.StartingPoints = currentStartingPoints;

                // Update the current redemption count in the video watcher.
                videoWatcher.RedemptionCount = currentRedemptionsCount;

                // Update the watcher to have the current ending points.
                if (pointsToMake == -1) {
                    int maximumPoints = configurationProcessor.GetMaximumPoints();
                    if (maximumPoints <= 0) {
                        Log.e(functionName, String.format("The maximum points is set to '%d' which is not valid.  It must be greater than zero.", maximumPoints));
                        return;
                    }
                    int minimumPoints = configurationProcessor.GetMinimumPoints();
                    if (maximumPoints < 0) {
                        Log.e(functionName, String.format("The minimum points is set to '%d' which is not valid.  It must be greater than or equal to zero.", minimumPoints));
                        return;
                    }
                    if (minimumPoints > maximumPoints) {
                        int tempMinimum = minimumPoints;
                        minimumPoints = maximumPoints;
                        maximumPoints = tempMinimum;
                        Log.w(functionName, String.format("The minimum points was set to '%d' and the maximum points set to '%d'.  These were switched because the maximum points must be greater than the minimum points.", minimumPoints, maximumPoints));
                    }
                    // Update the amount of points that need to be made to a different amount.
                    pointsToMake = minimumPoints + random.nextInt(1 + maximumPoints - minimumPoints);
                    Log.i(functionName, String.format("The points to make has been updated to be '%d'.", pointsToMake));
                }
                videoWatcher.EndingPoints = currentStartingPoints + pointsToMake;

                videoWatcher.FinishedCheckingPlus10 = finishedCheckingPlus10;

                videoWatcher.IntervalProcessorMain = intervalProcessor;

                Log.i(functionName, String.format("The starting points is '%d', ending points '%d', and redemption count is '%d'.", videoWatcher.StartingPoints, videoWatcher.EndingPoints, videoWatcher.RedemptionCount));

                // If the video has successfully processed then simply return.
                boolean successfullyProcessed = videoWatcher.Process();
                while (successfullyProcessed) {
                    Log.w(functionName, "The video watcher has successfully processed.");
                    if (!starter.Reset()) {
                        // If there are any problems resetting the application, then exit.
                        return;
                    }
                    if (!starter.Process()) {
                        // If there are any problems starting up the application, then exit.
                        return;
                    }

                    if (videoWatcher.CurrentPoints > 1000) {
                        if (redeemer.Process()) {
                            videoWatcher.RedemptionCount++;
                            currentRedemptionsCount = videoWatcher.RedemptionCount;
                            videoWatcher.CurrentPoints -= 1000;
                        }
                    }

                    if (videoWatcher.CurrentPoints + (videoWatcher.RedemptionCount * 1000) >= videoWatcher.EndingPoints) {
                        break;
                    }

                    successfullyProcessed = videoWatcher.Process();
                }

                finishedCheckingPlus10 = videoWatcher.FinishedCheckingPlus10;

                if (!successfullyProcessed) {
                    Log.w(functionName, "The processing has encountered a failure.");
                } else {
                    // Save a reference to the current date to mark it as being processed.
                    configurationProcessor.AddSuccessfullyProcessedDate(currentDateString);

                    // Reset the redemption count after successfully processing a day.
                    currentRedemptionsCount = 0;

                    // Reset the starting points to be -1 so that the next processing cycle will
                    // do a refresh on the starting points.  Just in case there were some additional
                    // points that were gained from the user.
                    currentStartingPoints = -1;
                }
            } catch (Exception e) {
                Log.e(functionName, "The test has ended in a failure.", e);
            }
        }
    }
}