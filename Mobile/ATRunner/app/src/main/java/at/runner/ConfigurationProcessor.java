package at.runner;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A configuration class that will help get configurations that are needed by this application.
 */
public class ConfigurationProcessor {

    private String _successfullyProcessedDatesFileName = "ATSuccessDates.txt";

    /**
     * Contains a reference to the logging class to write out log messages.  Please note that this
     * variable was named "Log" for a reason and not "_logger" which would normally be the name for
     * a private field.  By simply deleting this private variable and importing "import android.util.Log;"
     * all the references to the "Log" should still work without much rewrite.
     */
    private Logger Log;

    /**
     * A constructor of a video watcher class.
     * @param logger The logging class that will be used to log messages.
     */
    public ConfigurationProcessor(Logger logger) {
        Log = logger;
    }

    /**
     * Added the success date to the set of successfully processed configuration dates.
     * @param date The string representation of the current date which should be in the format 'yyyy-mm-hh'.
     */
    public void AddSuccessfullyProcessedDate(String date) {
        String functionName = "ConfigurationProcessor_AddSuccessfullyProcessedDate()";
        Log.i(functionName, "The function has started.");

        try {
            List<String> currentSuccesses = GetSuccessfullyProcessedDates();

            File file = new File(Common.GetStorageDirectory(), _successfullyProcessedDatesFileName);;
            Log.i(functionName, String.format("The file name for gathering successful processing dates is '%s'.", file.getAbsolutePath()));

            FileWriter writer = new FileWriter(file, true);

            try {
                // Append a comma if the file already exists so the dates are not mixed up.
                if (currentSuccesses.size() > 0) {
                    writer.append(",");
                }

                // Append the date to the file.
                writer.append(date);

                // Write out the actual log message.
                writer.flush();
            } finally {
                writer.close();
            }

            Log.i(functionName, String.format("The file for gathering successful processing dates was successfully updated with the date '%s'.", date));
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }
    }

    /**
     * Returns the ending hour that this application should start running.
     * @return The ending hour that this application should start running will be returned.
     */
    public int GetEndingHour() {
        String functionName = "ConfigurationProcessor_GetEndingHour()";
        Log.i(functionName, "The function has started.");
        int value = -1;

        try {
            String stringValue = getPropertyValue("pATEndingHour");
            if (!stringValue.equals("")) {
                value = Integer.parseInt(stringValue);
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return -2;
        }

        Log.i(functionName, String.format("The ending hour was found to be '%d'.", value));
        return value;
    }

    /**
     * Returns the facebook password that was to be used by this application.
     * @return The facebook password that was to be used by this application will be returned.
     */
    public String GetFacebookPassword() {
        String functionName = "ConfigurationProcessor_GetFacebookUserName()";
        Log.i(functionName, "The function has started.");
        String value;

        try {
            value = getPropertyValue("pATFaceBookPassword");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return "";
        }

        Log.i(functionName, String.format("The facebook password was found to be '%s'.", value));
        return value;
    }

    /**
     * Returns the facebook user name that was to be used by this application.
     * @return The facebook user name that was to be used by this application will be returned.
     */
    public String GetFacebookUserName() {
        String functionName = "ConfigurationProcessor_GetFacebookUserName()";
        Log.i(functionName, "The function has started.");
        String value;

        try {
            value = getPropertyValue("pATFaceBookUsername");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return "";
        }

        Log.i(functionName, String.format("The facebook user name was found to be '%s'.", value));
        return value;
    }

    /**
     * Returns the maximum number of points to make for this application in a day.
     * @return The maximum number of points to make for this application in a day will be returned.
     */
    public int GetMaximumPoints() {
        String functionName = "ConfigurationProcessor_GetMaximumPoints()";
        Log.i(functionName, "The function has started.");
        int value = -1;

        try {
            String stringValue = getPropertyValue("pATMaximumPoints");
            if (!stringValue.equals("")) {
                value = Integer.parseInt(stringValue);
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return -2;
        }

        Log.i(functionName, String.format("The maximum points was found to be '%d'.", value));
        return value;
    }

    /**
     * Returns the minimum number of points to make for this application in a day.
     * @return The minimum number of points to make for this application in a day will be returned.
     */
    public int GetMinimumPoints() {
        String functionName = "ConfigurationProcessor_GetMinimumPoints()";
        Log.i(functionName, "The function has started.");
        int value = -1;

        try {
            String stringValue = getPropertyValue("pATMinimumPoints");
            if (!stringValue.equals("")) {
                value = Integer.parseInt(stringValue);
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return -2;
        }

        Log.i(functionName, String.format("The minimum points was found to be '%d'.", value));
        return value;
    }

    /**
     * Returns the starting hour that this application should start running.
     * @return The starting hour that this application should start running will be returned.
     */
    public int GetStartingHour() {
        String functionName = "ConfigurationProcessor_GetStartingHour()";
        Log.i(functionName, "The function has started.");
        int value = -1;

        try {
            String stringValue = getPropertyValue("pATStartingHour");
            if (!stringValue.equals("")) {
                value = Integer.parseInt(stringValue);
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return -2;
        }

        Log.i(functionName, String.format("The starting hour was found to be '%d'.", value));
        return value;
    }

    /**
     * Returns a list of strings that represent the dates of successfully processed days.
     * @return A list of strings that represent the dates of successfully processed days will be returned.
     */
    public List<String> GetSuccessfullyProcessedDates() {
        String functionName = "ConfigurationProcessor_GetSuccessfullyProcessedDates()";
        Log.i(functionName, "The function has started.");
        List<String> dates = new ArrayList();

        try {
            File file = new File(Common.GetStorageDirectory(), _successfullyProcessedDatesFileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            Log.i(functionName, String.format("The file name for gathering successful processing dates is '%s'.", file.getAbsolutePath()));

            if (!file.exists()){
                Log.i(functionName, "The file for gathering successful processing dates does not exist so an empty set of dates is returned.");
                return dates;
            }

            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));

            try {
                String line;
                int lineCount = 0;
                while ((line = br.readLine()) != null) {
                    if (lineCount > 0) {
                        text.append('\n');
                    }
                    text.append(line);
                    lineCount++;
                }
                Log.i(functionName, String.format("The file was successfully read and found '%d' lines.", lineCount));
            } finally {
                br.close();
            }

            String completeString = text.toString();
            String[] parts = completeString.split(",");

            for (int i = 0; i < parts.length; i++) {
                dates.add(parts[i]);
            }

            Log.i(functionName, String.format("There were '%d' successfully processed dates within the file.", parts.length));
            return dates;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return dates;
        }
    }

    /**
     * Returns the value of the property that was set on the runtime object.
     * @param propertyName The name of the property to get.
     * @return The value of the property that was found will be returned; otherwise, an empty string will be returned.
     */
    private String getPropertyValue(String propertyName) {
        String functionName = "ConfigurationProcessor_getPropertyValue()";
        Log.i(functionName, "The function has started.");
        String value;

        try {
//            if (propertyName.equals("pATEndingHour")) {
//                return "20";
//            }
//            if (propertyName.equals("pATFaceBookPassword")) {
//                return "BlueBoy3324";
//            }
//            if (propertyName.equals("pATFaceBookUsername")) {
//                return "bruceevans8819@gmail.com";
//            }
//            if (propertyName.equals("pATMaximumPoints")) {
//                return "400";
//            }
//            if (propertyName.equals("pATStartingHour")) {
//                return "6";
//            }

            Process process = Runtime.getRuntime().exec(String.format("getprop %s", propertyName));
            try {
                BufferedReader bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
                try {
                    value = bis.readLine();
                } finally {
                    bis.close();
                }
            } finally {
                process.destroy();
            }

            // Make sure the value does not equal null which will prevent null reference exceptions.
            if (value == null)
            {
                value = "";
            }
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            return "";
        }

        Log.i(functionName, String.format("The property with he key '%s' was found to be '%s'.", propertyName, value));
        return value;
    }
}
