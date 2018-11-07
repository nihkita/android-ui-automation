package at.runner;

import java.io.File;

/**
 * A common class that will contain common functions that are used throughout the application.
 */
public class Common {

    /**
     * Returns the directory that all files should be stored within.
     * @return The directory that all files should be stored within are returned.
     */
    public static String GetStorageDirectory() {
        File directory = new File("/sdcard/ATRunner");
        directory.mkdirs();
        return directory.getAbsolutePath();
    }
}
