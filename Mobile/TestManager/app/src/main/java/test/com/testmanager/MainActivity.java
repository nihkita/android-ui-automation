package test.com.testmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    /**
     * Contains a reference to the log file that should be used for logging information.
     */
    private LogProcessor Log = new LogProcessor(CommonProcessor.GetStorageDirectory(), CommonProcessor.GetApplicationName().toLowerCase());

    /**
     * Called when the application is created.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String functionName = "MainActivity_onCreate()";
        Log.i(functionName, "The function has started.");
        try {
            // Gets all the environment properties that are used within this function.
            EnvironmentProcessor environmentProcessor = new EnvironmentProcessor();
            String perkBotPid = environmentProcessor.getValue("perkBotPid");
            Log.i(functionName, String.format("The perk bot identification was found to be '%s'.", perkBotPid));

            // Get all the saved settings that are used within this function.
            SettingsProcessor settingsProcessor = new SettingsProcessor(getFilesDir().getAbsolutePath());
            String perkEmailAddress = settingsProcessor.getValue("PerkEmailAddress");
            Log.i(functionName, String.format("The perk email address was found to be '%s'.", perkEmailAddress));
            String perkPassword = settingsProcessor.getValue("PerkPassword");
            Log.i(functionName, String.format("The perk password was found to be '%s'.", perkPassword));
            String startingHour = settingsProcessor.getValue("StartingHour");
            Log.i(functionName, String.format("The starting hour was found to be '%s'.", startingHour));
            String endingHour = settingsProcessor.getValue("EndingHour");
            Log.i(functionName, String.format("The ending hour was found to be '%s'.", endingHour));
            String pointsLimit = settingsProcessor.getValue("PointsLimit");
            Log.i(functionName, String.format("The points limit was found to be '%s'.", pointsLimit));

            // Get all the UI elements that are used within this function.
            Button buttonStart = (Button)findViewById(R.id._buttonStart);
            Button buttonStop = (Button)findViewById(R.id._buttonStop);
            EditText editTextPerkEmailAddress = (EditText)findViewById(R.id._editTextPerkEmailAddress);
            EditText editTextPerkPassword = (EditText)findViewById(R.id._editTextPerkPassword);
            EditText editTextStartingHour = (EditText)findViewById(R.id._editTextStartingHour);
            EditText editTextEndingHour = (EditText)findViewById(R.id._editTextEndingHour);
            EditText editTextPointsLimit = (EditText)findViewById(R.id._editTextPointsLimit);
            Log.i(functionName, "All the fields have been successfully found.");

            // Update all the buttons.
            if (perkBotPid != null && !perkBotPid.isEmpty()) {
                Log.i(functionName, "The perk bot identification was not empty so the start button is disabled and the stop button has been enabled.");
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
            }

            // Update the perk email address field on the UI.
            editTextPerkEmailAddress.setText(perkEmailAddress);
            if (perkEmailAddress == null || perkEmailAddress.isEmpty()) {
                Log.i(functionName, "Since the email address was found to be empty, the first account that is associated with the phone will attempt to be used.");
                AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
                Account[] list = manager.getAccounts();
                if (list.length > 1) {
                    editTextPerkEmailAddress.setText(list[1].name);
                    Log.i(functionName, String.format("There were a total of '%d' accounts found on the phone.  The name of the second account is '%s' and will be placed on the perk email address field.", list.length, list[1].name));
                } else {
                    Log.i(functionName, "There were no accounts found that are associated on the phone, so the perk email address is going to show up empty.");
                }
            }

            editTextPerkPassword.setText(perkPassword);
            editTextStartingHour.setText(startingHour);
            editTextEndingHour.setText(endingHour);
            editTextPointsLimit.setText(pointsLimit);

            Log.i(functionName, "The fields on the form have been updated successfully.");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
        }
    }

    /**
     * Called when the start button is clicked.
     * @param v The view that the start button is on.
     */
    public void start(View v) {
        String functionName = "MainActivity_start()";
        Log.i(functionName, "The function has started.");

        try {
            // Get all the UI elements that are used within this function.
            Button buttonStart = (Button) findViewById(R.id._buttonStart);
            Button buttonStop = (Button) findViewById(R.id._buttonStop);
            EditText editTextPerkEmailAddress = (EditText) findViewById(R.id._editTextPerkEmailAddress);
            EditText editTextPerkPassword = (EditText) findViewById(R.id._editTextPerkPassword);
            EditText editTextStartingHour = (EditText) findViewById(R.id._editTextStartingHour);
            EditText editTextEndingHour = (EditText) findViewById(R.id._editTextEndingHour);
            EditText editTextPointsLimit = (EditText) findViewById(R.id._editTextPointsLimit);
            Log.i(functionName, "All the fields have been successfully found.");

            // Get all the values from the UI.
            String perkEmailAddress = editTextPerkEmailAddress.getText().toString();
            Log.i(functionName, String.format("The perk email address was found to be '%s'.", perkEmailAddress));
            String perkPassword = editTextPerkPassword.getText().toString();
            Log.i(functionName, String.format("The perk password was found to be '%s'.", perkPassword));
            String startingHour = editTextStartingHour.getText().toString();
            Log.i(functionName, String.format("The starting hour was found to be '%s'.", startingHour));
            String endingHour = editTextEndingHour.getText().toString();
            Log.i(functionName, String.format("The ending hour was found to be '%s'.", endingHour));
            String pointsLimit = editTextPointsLimit.getText().toString();
            Log.i(functionName, String.format("The points limit was found to be '%s'.", pointsLimit));

            if (perkEmailAddress.isEmpty()) {
                Log.i(functionName, "The perk email address was found to be empty and therefore the UI Automation could not be started.");
                Toast toast = Toast.makeText(getApplicationContext(), "The perk email address cannot be empty.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (perkPassword.isEmpty()) {
                Log.i(functionName, "The perk password was found to be empty and therefore the UI Automation could not be started.");
                Toast toast = Toast.makeText(getApplicationContext(), "The perk password cannot be empty.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (startingHour.isEmpty()) {
                Log.i(functionName, "The starting hour was found to be empty and therefore the UI Automation could not be started.");
                Toast toast = Toast.makeText(getApplicationContext(), "The starting hour cannot be empty.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            int integerStartingHour = Integer.parseInt(startingHour);
            if (integerStartingHour < 0) {
                Log.i(functionName, String.format("The starting hour of '%d' is less than 0 and therefore the UI Automation could not be started.", integerStartingHour));
                Toast toast = Toast.makeText(getApplicationContext(), "The starting hour cannot be less than zero.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            } else if (integerStartingHour > 24) {
                Log.i(functionName, String.format("The starting hour of '%d' is greater than 24 and therefore the UI Automation could not be started.", integerStartingHour));
                Toast toast = Toast.makeText(getApplicationContext(), "The starting hour cannot be greater than 24.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (endingHour.isEmpty()) {
                Log.i(functionName, "The ending hour was found to be empty and therefore the UI Automation could not be started.");
                Toast toast = Toast.makeText(getApplicationContext(), "The ending hour cannot be empty.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            int integerEndingHour = Integer.parseInt(endingHour);
            if (integerEndingHour < 0) {
                Log.i(functionName, String.format("The ending hour of '%d' is less than 0 and therefore the UI Automation could not be started.", integerEndingHour));
                Toast toast = Toast.makeText(getApplicationContext(), "The ending hour cannot be less than zero.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            } else if (integerEndingHour > 24) {
                Log.i(functionName, String.format("The ending hour of '%d' is greater than 24 and therefore the UI Automation could not be started.", integerEndingHour));
                Toast toast = Toast.makeText(getApplicationContext(), "The ending hour cannot be greater than 24.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (pointsLimit.isEmpty()) {
                Log.i(functionName, "The points limit was found to be empty and therefore the UI Automation could not be started.");
                Toast toast = Toast.makeText(getApplicationContext(), "The points limit cannot be empty.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            int integerPointsLimit = Integer.parseInt(pointsLimit);
            if (integerPointsLimit < 0) {
                Log.i(functionName, String.format("The points limit of '%d' is less than 0 and therefore the UI Automation could not be started.", integerPointsLimit));
                Toast toast = Toast.makeText(getApplicationContext(), "The points limit cannot be less than zero.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            // Create a processor for environment functionality.
            EnvironmentProcessor environmentProcessor = new EnvironmentProcessor();
            EnvironmentProperty ep = new EnvironmentProperty();

            // Create a processor for settings functionality.
            SettingsProcessor settingsProcessor = new SettingsProcessor(getFilesDir().getAbsolutePath());
            Setting s = new Setting();

            s.key = "PerkEmailAddress";
            s.value = perkEmailAddress;
            if (!settingsProcessor.set(s)) {
                Log.e(functionName, "The saving of the perk email address failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The saving of the perk email address failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            s.key = "PerkPassword";
            s.value = perkPassword;
            if (!settingsProcessor.set(s)) {
                Log.e(functionName, "The saving of the perk password failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The saving of the perk password failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            s.key = "StartingHour";
            s.value = startingHour;
            if (!settingsProcessor.set(s)) {
                Log.e(functionName, "The saving of the starting hour failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The saving of the starting hour failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            s.key = "EndingHour";
            s.value = endingHour;
            if (!settingsProcessor.set(s)) {
                Log.e(functionName, "The saving of the ending hour failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The saving of the ending hour failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            s.key = "PointsLimit";
            s.value = pointsLimit;
            if (!settingsProcessor.set(s)) {
                Log.e(functionName, "The saving of the points limit failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The saving of the points limit failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            ep.key = "perkEmail";
            ep.value = perkEmailAddress;
            if (!environmentProcessor.set(ep)) {
                Log.e(functionName, "The setting of the perk email address failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The setting of the perk email address failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            ep.key = "perkPass";
            ep.value = perkPassword;
            if (!environmentProcessor.set(ep)) {
                Log.e(functionName, "The setting of the perk password failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The setting of the perk password failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            ep.key = "perkStartingHour";
            ep.value = startingHour;
            if (!environmentProcessor.set(ep)) {
                Log.e(functionName, "The setting of the starting hour failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The setting of the starting hour failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            ep.key = "perkEndingHour";
            ep.value = endingHour;
            if (!environmentProcessor.set(ep)) {
                Log.e(functionName, "The setting of the ending hour failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The setting of the ending hour failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            ep.key = "perkPointsLimit";
            ep.value = pointsLimit;
            if (!environmentProcessor.set(ep)) {
                Log.e(functionName, "The setting of the points limit failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The setting of the points limit failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (!environmentProcessor.executeCommand("uiautomator runtest testautomator.jar -c com.test.runner.test.TestAutomator")) {
                Log.e(functionName, "The UI automation command failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The UI automation command failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            // Update all the buttons.
            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);

            Toast toast = Toast.makeText(getApplicationContext(), "The UI Automation process has successfully started.", Toast.LENGTH_SHORT);
            toast.show();

            Log.i(functionName, "The UI automation process has successfully started.");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
        }
    }

    /**
     * Called when the stop button is clicked.
     * @param v The view that the stop button is on.
     */
    public void stop(View v) {
        String functionName = "MainActivity_start()";
        Log.i(functionName, "The function has started.");

        try {
            // Get all the UI elements that are used within this function.
            Button buttonStart = (Button)findViewById(R.id._buttonStart);
            Button buttonStop = (Button)findViewById(R.id._buttonStop);
            Log.i(functionName, "All the fields have been successfully found.");

            // Gets all the environment properties that are used within this function.
            EnvironmentProcessor environmentProcessor = new EnvironmentProcessor();
            EnvironmentProperty ep = new EnvironmentProperty();
            String perkBotPid = environmentProcessor.getValue("perkBotPid");
            Log.i(functionName, String.format("The perk bot identification was found to be '%s'.", perkBotPid));

            // If the process is currently not running for some reason, the allow the user to start the processing
            // again.
            if (perkBotPid == null || perkBotPid.isEmpty()) {
                Log.i(functionName, "The perk bot identification was null or empty so the UI Automation process was not stopped because its not running.");
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                Toast toast = Toast.makeText(getApplicationContext(), "The UI Automation process was not stopped because it was not running.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (!environmentProcessor.executeCommand(String.format("kill -9 %s", perkBotPid))) {
                Log.e(functionName, "The perk bot failed to stop.");
                Toast toast = Toast.makeText(getApplicationContext(), "The UI Automation process was not stopped.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            ep.key = "perkBotPid";
            ep.value = "";
            if (!environmentProcessor.set(ep)) {
                Log.e(functionName, "The setting of the perk bot identification to be empty failed.");
                Toast toast = Toast.makeText(getApplicationContext(), "The setting of the process identification failed.  Please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            // Update all the buttons.
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);

            Toast toast = Toast.makeText(getApplicationContext(), "The UI Automation process was successfully stopped.", Toast.LENGTH_SHORT);
            toast.show();

            Log.i(functionName, "The UI automation process has successfully stopped.");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
            e.printStackTrace();
        }
    }
}