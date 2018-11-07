package test.com.testmanager;

import android.os.SystemClock;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The process that will provide the ability to get and set environment properties.
 */
public class EnvironmentProcessor {

    /**
     * Executes a command to the environment.
     * @param command The command that should be executed.
     * @return true is returned if the execution was successful; otherwise, false is returned.
     */
    public boolean executeCommand(String command) {
        String functionName = "EnvironmentProcessor_executeCommand()";
        Log.i(functionName, "The function has started.");

        if (command == null || command.isEmpty()) {
            Log.e(functionName, "The command cannot be executed to the environment because it is null.");
            return false;
        }

        Runtime rt = Runtime.getRuntime();
        Process process = null;
        DataOutputStream os = null;
        try {
            process = rt.exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(String.format("%s\n", command));
            os.flush();
            Log.i(functionName, String.format("The command '%s' was  was successfully executed.", command));
        } catch (IOException e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(functionName, "The DataOutputStream failed when attempting to be closed.", e);
                    e.printStackTrace();
                }
            }
            SystemClock.sleep(50);
            if (process != null) {
                process.destroy();
            }
        }

        return true;
    }

    /**
     * Gets the environment property based on the key.
     * @param key The key to get the environment property for.
     * @return The actual environment property will be returned if it is found; otherwise, null will be returned.
     */
    public EnvironmentProperty get(String key) {
        String functionName = "EnvironmentProcessor_get()";
        Log.i(functionName, "The function has started.");

        if (key == null) {
            Log.e(functionName, "The environment property key cannot be null.");
            return null;
        }

        Process process = null;
        BufferedReader bis = null;
        try {
            process = Runtime.getRuntime().exec(String.format("getprop %s", key));
            bis = new BufferedReader(new InputStreamReader(process.getInputStream()));

            EnvironmentProperty ep = new EnvironmentProperty();
            ep.key = key;

            Log.i(functionName, String.format("Attempting to read the environment property with the key of '%s'.", ep.key));
            ep.value = bis.readLine();

            // Switch out the keys to what they really are.
            if (ep.value.equals("[NULL]")) {
                ep.value = null;
            } else if (ep.value.equals("[EMPTY]")) {
                ep.value = "";
            }

            Log.i(functionName, String.format("The environment property with the key of '%s' was read and found to be '%s'.", ep.key, ep.value));
            return ep;
        } catch (IOException e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
            return null;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    Log.e(functionName, "The BufferedReader failed when attempting to be closed.", e);
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Gets the value of the environment property for the specified key.
     * @param key The key of the environment property to get.
     * @return Null will be returned if the key was not found; otherwise, the value of the key will be returned.
     */
    public String getValue(String key) {
        String functionName = "EnvironmentProcessor_getValue()";
        Log.i(functionName, "The function has started.");

        if (key == null) {
            Log.e(functionName, "The environment property key cannot be null.");
            return null;
        }

        try {
            EnvironmentProperty s = get(key);

            if (s == null){
                Log.i(functionName, "The environment property was not found so null is returned.");
                return null;
            }

            Log.i(functionName, String.format("The environment property with the key '%s' was found to be '%s'.", key, s.value));
            return s.value;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sets and saves the environment property for future use.
     * @param ep The environment property that is to be saved.
     * @return A value will be returned indicating whether or not the function successfully processed the set.
     */
    public boolean set(EnvironmentProperty ep) {
        String functionName = "EnvironmentProcessor_set()";
        Log.i(functionName, "The function has started.");

        if (ep == null) {
            Log.e(functionName, "The environment property cannot be set because it is null.");
            return false;
        }

        if (ep.key == null) {
            Log.e(functionName, "The environment property cannot be set because the key is null.");
            return false;
        }

        if (ep.key.isEmpty()) {
            Log.e(functionName, "The environment property cannot be set because the key is empty.");
            return false;
        }

        // Setting the property to be null or empty just simply doesn't work.  Therefore, set the value
        // of the property to be "[NULL]" or "[EMPTY]".
        String epValue = ep.value;
        if (epValue == null) {
            epValue = "[NULL]";
        } else if (epValue.isEmpty()) {
            epValue = "[EMPTY]";
        }

        Runtime rt = Runtime.getRuntime();
        Process process = null;
        DataOutputStream os = null;
        try {
            process = rt.exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(String.format("setprop %s %s\n", ep.key, epValue));
            os.flush();
            Log.i(functionName, String.format("The environment property with the key '%s' and value '%s' was successfully set.", ep.key, epValue));
        } catch (IOException e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(functionName, "The DataOutputStream failed when attempting to be closed.", e);
                    e.printStackTrace();
                }
            }
            SystemClock.sleep(50);
            if (process != null) {
                process.destroy();
            }
        }

        if (!getValue(ep.key).equals(ep.value)) {
            Log.e(functionName, String.format("The environment property with the key '%s' and value '%s' was not successfully retrieved after setting.", ep.key, ep.value));
            return false;
        }

        return true;
    }
}
