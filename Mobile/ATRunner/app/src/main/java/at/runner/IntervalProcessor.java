package at.runner;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A class that will control which time intervals are currently scheduled to be processed.
 */
public class IntervalProcessor {

    /**
     * Contains processor that will handle getting all the configuration data.
     */
    private ConfigurationProcessor _configurationProcessor;

    /**
     * Contains a list of sleep times that the application should follow.
     */
    private List<Interval> _intervals = new ArrayList();

    /**
     * Contains a value indicating whether or not this class has been initialized.
     */
    private boolean _initialized;

    /**
     * Contains a reference to the logging class to write out log messages.  Please note that this
     * variable was named "Log" for a reason and not "_logger" which would normally be the name for
     * a private field.  By simply deleting this private variable and importing "import android.util.Log;"
     * all the references to the "Log" should still work without much rewrite.
     */
    private Logger Log;

    /**
     * A constructor of a sleep processor class.
     * @param logger The logging class that will be used to log messages.
     * @param configurationProcessor The processor that will get configuration data.
     */
    public IntervalProcessor(Logger logger, ConfigurationProcessor configurationProcessor) {
        Log = logger;
        _configurationProcessor = configurationProcessor;
    }

    /**
     * Gets the sleep weight that the video processor should use when playing videos.
     * @return The sleep weight that the video processor should use when playing videos is returned.
     */
    public IntervalWeight GetIntervalWeight() {
        String functionName = "IntervalProcessor_GetIntervalWeight()";
        Log.i(functionName, "The function has started.");

        try {
            if (!_initialized) {
                Initialize();
                _initialized = true;
            }

            while (true) {
                Calendar c = Calendar.getInstance();

                // The hour of the day is in 24 hour format.
                int currentHour = c.get(c.HOUR_OF_DAY);
                int currentMinute = c.get(c.MINUTE);

                // Do a mod of 5 on the minute and then subtract it from the current minute.  This will
                // make the minute, 0, 5, 10, 15, 20, 25, 30, etc.
                int currentFiveMinute = currentMinute - (currentMinute % 5);

                Interval futureInterval = null;
                for (int i = 0; i < _intervals.size(); i++) {
                    Interval interval = _intervals.get(i);
                    if (futureInterval == null) {
                        if (currentHour < interval.StartHour) {
                            futureInterval = interval;
                        } else if (currentHour == interval.StartHour && currentFiveMinute < interval.StartMinute) {
                            futureInterval = interval;
                        }
                    }

                    if (interval.StartHour == currentHour && interval.StartMinute == currentFiveMinute) {
                        Log.i(functionName, String.format("The interval '%s' was found and returned.", interval.toString()));
                        return interval.Weight;
                    }
                }

                if (futureInterval != null) {
                    // Lets not log a message here as it would flood the logs.
                    SystemClock.sleep(1 * 60 * 1000);
                } else {
                    break;
                }
            }

            Log.i(functionName, "There were no more future intervals detected.");
            return IntervalWeight.Heavy;
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
            return IntervalWeight.Heavy;
        }
    }

    /**
     * Initializes this class and gets all the sleep times based on the configurations.
     */
    public void Initialize() {
        String functionName = "IntervalProcessor_initialize()";
        Log.i(functionName, "The function has started.");

        try {
            int maximumPoints = _configurationProcessor.GetMaximumPoints();
            int startingHour = _configurationProcessor.GetStartingHour();
            int startingMinute = startingHour * 60;
            int endingHour = _configurationProcessor.GetEndingHour() - 1;
            int endingMinute = endingHour * 60;
            int totalMinutes = endingMinute - startingMinute;
            int totalFortyFiveIntervals = (int)Math.floor(((double)totalMinutes)/((double)45));

            // Create a list of all the 45 minute intervals between the starting and ending hour but not including the
            // ending hour.
            List<Integer> minutes = new ArrayList();
            for (int i = 0; i < totalFortyFiveIntervals; i++) {
                minutes.add(startingMinute + (i * 45));
            }

            // This is a rough estimation of how many ten point videos will run.
            int estimatedTenPointVideos = 3;

            // This is a rough estimation of how many five point videos will run.
            int estimatedFivePointVideos = 100;

            // This is a rough estimation of how many three point videos will run.
            int estimatedThreePointVideos = (maximumPoints - (estimatedTenPointVideos * 10) - (estimatedFivePointVideos * 5)) / 3;

            // Add one to the estimated three points just to make sure the total three points is accurate with the total points that are needed.
            estimatedThreePointVideos++;

            // Get the total videos that are needing to be watched.
            int totalVideos = estimatedFivePointVideos + estimatedTenPointVideos + estimatedThreePointVideos;

            // Some ads are 15 seconds, some are 30 seconds, and some are 45 seconds.  Therefore, lets
            // say the average is 40 seconds.  30 seconds seems to be slightly too little.  40 seconds per Ad
            // Time may be better.
            int averageAdTime = 40 * 1000;

            // Lets say the average time to watch a trailer is 45 seconds.
            int averageAppTrailerTime = 45 * 1000;

            Random r = new Random();
            int estimatedVideos = 0;
            while (estimatedVideos < totalVideos && minutes.size() > 0) {
                // Pick a random 45 minute to process that is within the minutes array.
                int minuteIndex = r.nextInt(minutes.size());
                int minuteFromArray = minutes.get(minuteIndex);

                double hourDouble = ((double)minuteFromArray) / ((double)60);
                int hour = (int)Math.floor(hourDouble);
                double difference = hourDouble - hour;

                int minuteOfHour;
                if (difference <= 0){
                    minuteOfHour = 0;
                } else if (difference <= 0.25) {
                    minuteOfHour = 15;
                } else if (difference <= 0.50) {
                    minuteOfHour = 30;
                } else {
                    minuteOfHour = 45;
                }

                for (int minuteAddIndex = 0; minuteAddIndex < 6; minuteAddIndex++) {
                    int tempMinute = minuteOfHour + (5 * minuteAddIndex);
                    IntervalWeight tempWeight;
                    if (minuteAddIndex < 2) {
                        tempWeight = IntervalWeight.Heavy;
                    } else if (minuteAddIndex < 4) {
                        tempWeight = IntervalWeight.Medium;
                    } else {
                        tempWeight = IntervalWeight.Light;
                    }

                    if (tempMinute >= 60) {
                        _intervals.add(new Interval(hour + 1, tempMinute - 60, tempWeight));
                    } else {
                        _intervals.add(new Interval(hour, tempMinute, tempWeight));
                    }
                }

                // Based on the current intervals that are created up to this point, estimate the
                // number of videos that will be played.  This will tell us if more intervals need
                // to be considered.
                estimatedVideos = 0;
                for (int i = 0; i < _intervals.size(); i++) {
                    Interval st = _intervals.get(i);
                    if (st.Weight == IntervalWeight.Heavy) {
                        estimatedVideos += (5 * 60 * 1000) / averageAdTime;
                    } else if (st.Weight == IntervalWeight.Medium) {
                        estimatedVideos += (5 * 60 * 1000) / (averageAdTime + averageAppTrailerTime);
                    } else {
                        estimatedVideos += (5 * 60 * 1000) / (averageAdTime + averageAppTrailerTime + (7 * 1000));
                    }
                }

                // Since the minute was done processing, remove it from the minutes array so it is not
                // chosen again.
                minutes.remove(minuteIndex);
            }

            Collections.sort(_intervals);
            print();
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
        }
    }

    /**
     * Prints or logs all the sleep times that were found.
     */
    private void print() {
        String functionName = "IntervalProcessor_print()";
        Log.i(functionName, "The function has started.");

        try {
            for (int i = 0; i < _intervals.size(); i++) {
                Log.i(functionName, _intervals.get(i).toString());
            }
        } catch (Exception e){
            Log.e(functionName, "The function has ended in a failure.", e);
        }
    }
}
