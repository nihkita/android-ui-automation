package com.test.runner.test;

import android.os.SystemClock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A processor class that will assist with logging out messages.
 */
public class LogProcessor {
    /**
     * Contains the name of the application that is writing out the logs.
     */
    private String _applicationName;

    /**
     * A value indicating whether or not the log file has been written to since this instance of
     * the logging class has been declared.
     */
    private boolean _firstWrite = true;

    /**
     * Contains the last time within milliseconds the cleanup was done.
     */
    private long _lastCleanupCheckTime = 0;

    /**
     * The full path and file name of the log file that the logging information should be written
     * out to.
     */
    private String _logFileDirectory;

    /**
     * The constructor which initializes a LogProcessor class.
     * @param logFileDirectory The full path and file name of the log file that the logging information should be written out to.
     */
    public LogProcessor(String logFileDirectory, String applicationName) {
        _logFileDirectory = logFileDirectory;
        _applicationName = applicationName;
    }

    /**
     * Logs an information message.
     * @param category The category that the message should be associated with.
     * @param message The actual message to be logged.
     */
    public void i(String category, String message) {

        // Do not log an empty message.
        if (message == null || message.equals("")) {
            return;
        }

        write("Info", category, message);
    }

    /**
     * Logs a warning.
     * @param category The category that the message should be associated with.
     * @param message The actual message to be logged.
     */
    public void w(String category, String message) {

        // Do not log an empty message.
        if (message == null || message.equals("")) {
            return;
        }

        write("Warning", category, message);
    }

    /**
     * Logs an error.
     * @param category The category that the message should be associated with.
     * @param message The actual message to be logged.
     */
    public void e(String category, String message) {

        // Do not log an empty message.
        if (message == null || message.equals("")) {
            return;
        }

        write("Error", category, message);
    }

    /**
     * Logs an exception.
     * @param category The category that the message should be associated with.
     * @param message The actual message to be logged.
     * @param e The exception that should be logged.
     */
    public void e(String category, String message, Exception e) {

        // Do not log an empty message.
        if ((message == null || message.equals("")) && e == null){
            return;
        }

        // Make sure the message is initialized before appending the exception to the string.
        if (message == null){
            message = "";
        }

        // Append the exception information on the message that is being logged.
        message += e.getClass() + ": " + e.getMessage();

        write("Exception", category, message);
    }

    /**
     * Cleans up the directory where the log messages are stored.
     */
    private void cleanUpLogs() {
        String functionName = "LogProcessor_cleanUpLogs()";
        i(functionName, "The function has started.");
        try {
            // Get the current date and time that should be logged.
            Calendar c = Calendar.getInstance();

            // Get a list of the last 10 days as a string.
            List<String> datesAsStrings = new ArrayList<String>();
            for (int i = 0; i < 10; i++) {
                String dateString = StringProcessor.formatCalendarInstanceShortDate(c) + ".log";
                datesAsStrings.add(dateString);
                i(functionName, String.format("The acceptable log file name is '%s'.", dateString));
                c.add(Calendar.DATE, -1);
            }

            File directory = new File(_logFileDirectory);
            if (!directory.exists()) {
                w(functionName, "The directory does not exist so nothing was cleaned up.");
                return;
            }

            File[] files = directory.listFiles(new LogExtensionFilter());
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                i(functionName, String.format("The log file name '%s' is being checked.", file.getAbsolutePath()));

                // Check to see if this log file was in the list of acceptable dates.
                boolean found = false;
                for (int k = 0; k < datesAsStrings.size(); k++) {
                    String dateAsStrings = datesAsStrings.get(k);
                    if (file.getAbsolutePath().contains(dateAsStrings)) {
                        found = true;
                        break;
                    }
                }

                // If the file was not found within the list of acceptable dates, then delete it.
                if (!found) {
                    String fileName = file.getAbsolutePath();
                    i(functionName, String.format("The file %s is about to be deleted.", fileName));
                    file.delete();
                    i(functionName, String.format("The file %s was successfully deleted.", fileName));
                }
            }
        } catch (Exception e) {
            e(functionName, "The function has ended in a failure.", e);
        }
    }

    /**
     * Writes a log message out to the log file.
     * @param severity The severity of the log message.  For example, whether or not it is an informational or warning message.
     * @param category The category that the message should be associated with.
     * @param message The actual message to be logged.
     */
    private void write(String severity, String category, String message) {
        try {
            // Get the current date and time that should be logged.
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int second = c.get(Calendar.SECOND);

            String dateString = StringProcessor.formatCalendarInstanceShortDate(c);

            // Open the log file to write to.
            File logFile = new File(_logFileDirectory, String.format("%s%s.log", _applicationName, dateString));
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            FileWriter writer = new FileWriter(logFile, true);

            try {
                // If this is the first write to the file, then do not write out a new line be record
                // that the first line has been written out.
                if (_firstWrite) {
                    _firstWrite = false;
                } else {
                    writer.append("\r\n");
                }

                // Write out the actual log message.
                writer.append(String.format("%d-%02d-%02d %02d:%02d:%02d %s%s%s", year, month, day, hour, minute, second, StringProcessor.rightPad(severity, 10), StringProcessor.rightPad(category, 50), message));
                writer.flush();
            } finally {
                writer.close();
            }

            // If that last clean up was done more than 30 minutes ago, then do another cleanup.
            if (_lastCleanupCheckTime + (1000 * 60 * 30) <= SystemClock.currentThreadTimeMillis() || _lastCleanupCheckTime == 0) {
                _lastCleanupCheckTime = SystemClock.currentThreadTimeMillis();
                cleanUpLogs();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
