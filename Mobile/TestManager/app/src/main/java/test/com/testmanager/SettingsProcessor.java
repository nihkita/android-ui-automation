package test.com.testmanager;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A process that will handle the setting and getting of different settings.
 */
public class SettingsProcessor {

    /**
     * Contains the directory in which the settings will be stored within.
     */
    private String _filesDirectory;

    /**
     * Constructs a settings processor which will handle the storage and retrieval of settings.
     * @param filesDirectory The directory in which the settings will be stored within.
     */
    public SettingsProcessor(String filesDirectory) {
        _filesDirectory = filesDirectory;
    }

    /**
     * Gets the setting based on the key.
     * @param key The key to get the setting for.
     * @return The actual setting will be returned if it is found; otherwise, null will be returned.
     */
    public Setting get(String key) {
        String functionName = "SettingsProcessor_get()";
        Log.i(functionName, "The function has started.");

        if (key == null) {
            Log.e(functionName, "The setting key cannot be null.");
            return null;
        }

        try {
            List<Setting> settings = getAll();

            for (int i = 0; i < settings.size(); i++) {
                Setting s = settings.get(i);
                if (s.key.equals(key)) {
                    Log.i(functionName, "The setting was found and is being returned.");
                    return s;
                }
            }

            Log.i(functionName, "The setting was not found so null is returned.");
            return null;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all the settings that were previously set.
     * @return A collection of all the settings that were previously set will be returned.
     */
    public List<Setting> getAll() {
        String functionName = "SettingsProcessor_getAll()";
        Log.i(functionName, "The function has started.");
        List<Setting> settings = new ArrayList<>();

        try {
            String fileName = _filesDirectory + "/settings.txt";
            Log.i(functionName, String.format("The file name this applications settings is '%s'.", fileName));
            File file = new File(fileName);

            boolean alreadyExists = file.exists();
            if (alreadyExists){
                Log.i(functionName, "The file for this applications settings already exists.");
            } else {
                Log.i(functionName, "The file for this applications settings does not exist yet so no settings were returned.");
                return new ArrayList<>();
            }

            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));

            //noinspection TryFinallyCanBeTryWithResources
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
            String[] settingsInFile = completeString.split("\\|\\|\\|\\|");

            for (String settingInFile : settingsInFile) {
                int commaIndex = settingInFile.indexOf(",");
                if (commaIndex < 0) {
                    Log.e(functionName, "The settings file is not properly formed because there was a setting without a comma.");
                    return new ArrayList<>();
                }

                String firstPart = settingInFile.substring(0, commaIndex);
                String secondPart = settingInFile.substring(commaIndex + 1, settingInFile.length());
                Log.e(functionName, String.format("The first part of the setting was found to be '%s' and the second part of the setting was found to be '%s'.", firstPart, secondPart));

                Setting setting = new Setting();
                setting.key = firstPart;
                setting.value = secondPart;

                // Add the setting to the list of settings.
                settings.add(setting);
            }

            Log.i(functionName, String.format("The function was successfully processed and found '%d' settings.", settings.size()));
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
            return new ArrayList<>();
        }

        return settings;
    }

    /**
     * Gets the value of the setting for the specified key.
     * @param key The key of the setting to get.
     * @return Null will be returned if the key was not found; otherwise, the value of the key will be returned.
     */
    public String getValue(String key) {
        String functionName = "SettingsProcessor_getValue()";
        Log.i(functionName, "The function has started.");

        if (key == null) {
            Log.e(functionName, "The setting key cannot be null.");
            return null;
        }

        try {
            Setting s = get(key);

            if (s == null){
                Log.i(functionName, "The setting was not found so null is returned.");
                return null;
            }

            Log.i(functionName, String.format("The setting with the key '%s' was found to be '%s'.", key, s.value));
            return s.value;
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sets and saves the setting for future use.
     * @param setting The setting that is to be saved.
     * @return A value will be returned indicating whether or not the function successfully processed the set.
     */
    public boolean set(Setting setting) {
        String functionName = "SettingsProcessor_set()";
        Log.i(functionName, "The function has started.");

        if (setting == null) {
            Log.e(functionName, "The setting cannot be saved because it is null.");
            return false;
        }

        if (setting.key == null) {
            Log.e(functionName, "The setting cannot be saved because the key is null.");
            return false;
        }

        if (setting.key.isEmpty()) {
            Log.e(functionName, "The setting cannot be saved because the key is empty.");
            return false;
        }

        if (setting.key.contains(",")) {
            Log.e(functionName, "The settings key cannot contain a ',' character.");
            return false;
        }

        try {
            String fileName = _filesDirectory + "/settings.txt";
            Log.i(functionName, String.format("The file name this applications settings is '%s'.", fileName));
            File file = new File(fileName);

            boolean alreadyExists = file.exists();
            if (alreadyExists){
                Log.i(functionName, "The file for this applications settings already exists.");
            } else {
                Log.i(functionName, "The file for this applications settings does not exist yet.");
            }

            // Get all the settings.
            List<Setting> settings = getAll();

            // Add/update the setting within the collection of existing settings.
            Setting existingSetting = null;
            for (int i = 0; i < settings.size(); i++) {
                Setting s = settings.get(i);
                if (s.key.equals(setting.key)) {
                    existingSetting = s;
                    break;
                }
            }

            if (existingSetting != null) {
                if (setting.value != null) {
                    // Update the existing setting which should be reflected within the settings
                    // collection to be saved later on in this function.
                    existingSetting.value = setting.value;
                    Log.i(functionName, String.format("The existing setting for the key '%s' has been updated to have a value of '%s'.", setting.key, setting.value));
                } else {
                    // Since teh value that is being saved is null, delete the existing setting from
                    // the settings collection so the key will be missing.  Then when the "get"
                    // function is used, it will not be found and null will be returned.
                    for (int i = 0; i < settings.size(); i++) {
                        Setting s = settings.get(i);
                        if (s.key.equals(setting.key)) {
                            settings.remove(i);
                            Log.i(functionName, String.format("The existing setting for the key '%s' has been removed since the value is null.", setting.key));
                            break;
                        }
                    }
                }
            } else {
                if (setting.value != null) {
                    // Only add the setting if the value is not null.
                    settings.add(setting);
                    Log.i(functionName, String.format("The new setting for the key '%s' and value of '%s' has been added.", setting.key, setting.value));
                } else {
                    // At this point the setting doesn't already exist and the settings collection
                    // has not been adjusted so simply return out of this function since there is
                    // nothing further that needs to happen.
                    Log.i(functionName, "The function was successfully processed.");
                    return true;
                }
            }

            // Create a brand new file to be saved.
            FileWriter writer = new FileWriter(file, false);

            try {
                // Add all the settings to the file.
                for (int i = 0; i < settings.size(); i++) {
                    Setting s = settings.get(i);
                    writer.append(String.format("%s,%s", s.key, s.value));
                    if (i+1 < settings.size()) {
                        writer.append("||||");
                    }
                }

                // Write out the actual log message.
                writer.flush();
            } finally {
                writer.close();
            }

            Log.i(functionName, "The function was successfully processed.");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
