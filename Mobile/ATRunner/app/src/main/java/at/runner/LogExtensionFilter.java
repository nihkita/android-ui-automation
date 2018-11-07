package at.runner;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A class that contains a filter for log files.
 */
public class LogExtensionFilter implements FilenameFilter
{
    /**
     * A method that will determine whether or not the file is accepted or not through the filter.
     * @param dir The directory in which the file is located.
     * @param name The name of the file.
     * @return true is returned if the file was accepted; otherwise, false is returned.
     */
    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".log");
    }
}