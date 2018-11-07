package test.com.testmanager;

import java.io.File;

/**
 * A process that will contain common functionality.
 */
public class CommonProcessor {

    /**
     * Returns the name of the current application.
     * @return The name of the current application will be returned.
     */
    public static String GetApplicationName() {
        return "TestManager";
    }

    /**
     * Returns the directory that all files should be stored within.
     * @return The directory that all files should be stored within are returned.
     */
    public static String GetStorageDirectory() {
        File directory = new File("/sdcard/" + GetApplicationName());
        directory.mkdirs();
        return directory.getAbsolutePath();
    }
}
