package test.com.testmanager;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends ActionBarActivity {

    /**
     * Called when the application is created.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateUIFromSavedSettings();
    }

    /**
     * Called when the start button is clicked.
     * @param v The view that the start button is on.
     */
    public void start(View v) {
        Button btn = (Button)findViewById(R.id._buttonStart);
        btn.setEnabled(false);

        saveSettings();

        try {
            Runtime rt = Runtime.getRuntime();
            Process runnerProcess = rt.exec("su");
            DataOutputStream os = new DataOutputStream(runnerProcess.getOutputStream());

            CheckBox checkBoxRateVideos = (CheckBox)findViewById(R.id._checkBoxRateVideos);
            EditText editTextPerkEmailAddress = (EditText)findViewById(R.id._editTextPerkEmail);
            EditText editTextPerkPassword = (EditText)findViewById(R.id._editTextPerkPassword);
            EditText editTextStartingHour = (EditText)findViewById(R.id._editTextStartingHour);
            EditText editTextEndingHour = (EditText)findViewById(R.id._editTextEndingHour);

            os.writeBytes(String.format("setprop pUser %s\n", editTextPerkEmailAddress.getText()));
            os.writeBytes(String.format("setprop pPass %s\n", editTextPerkPassword.getText()));
            os.writeBytes(String.format("setprop pRateVideos %b\n", checkBoxRateVideos.isChecked()));
            os.writeBytes(String.format("setprop pStartingHour %s\n", editTextStartingHour.getText()));
            os.writeBytes(String.format("setprop pEndingHour %s\n", editTextEndingHour.getText()));

            os.writeBytes("uiautomator runtest testautomator.jar -c com.test.runner.test.TestAutomator\n");
            os.close();
        } catch (Exception e) {
            Log.e("Start Error", e.getMessage());
            e.printStackTrace();
            btn.setEnabled(true);
        }
    }

    /**
     * Saves the settings within a local file.
     */
    private void saveSettings() {
        String functionName = "MainActivity_updateSettings()";
        Log.i(functionName, "The function has started.");

        try {
            String fileName = getFilesDir() + "/PManagerSettings.txt";
            Log.i(functionName, String.format("The file name for saved settings is '%s'.", fileName));
            File file = new File(fileName);

            boolean alreadyExists = file.exists();
            if (alreadyExists){
                Log.i(functionName, "The file for saved settings already exists.");
            } else {
                Log.i(functionName, "The file for saved settings does not exist yet.");
            }

            FileWriter writer = new FileWriter(file, false);

            try {
                CheckBox checkBoxRateVideos = (CheckBox)findViewById(R.id._checkBoxRateVideos);
                EditText editTextPerkEmailAddress = (EditText)findViewById(R.id._editTextPerkEmail);
                EditText editTextPerkPassword = (EditText)findViewById(R.id._editTextPerkPassword);
                EditText editTextStartingHour = (EditText)findViewById(R.id._editTextStartingHour);
                EditText editTextEndingHour = (EditText)findViewById(R.id._editTextEndingHour);

                writer.append(String.format("%b,", checkBoxRateVideos.isChecked()));
                writer.append(String.format("%s,", editTextPerkEmailAddress.getText()));
                writer.append(String.format("%s,", editTextPerkPassword.getText()));
                writer.append(String.format("%s,", editTextStartingHour.getText()));
                writer.append(String.format("%s", editTextEndingHour.getText()));

                // Write out the actual log message.
                writer.flush();
            } finally {
                writer.close();
            }

            Log.i(functionName, "The file was successfully updated.");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }
    }

    /**
     * Updates the user interface with the settings that were previously saved.
     */
    private void updateUIFromSavedSettings() {
        String functionName = "MainActivity_updateUIFromSavedSettings()";
        Log.i(functionName, "The function has started.");

        try {
            String fileName = getFilesDir() + "/PManagerSettings.txt";
            Log.i(functionName, String.format("The file name for saved settings is '%s'.", fileName));
            File file = new File(fileName);

            boolean alreadyExists = file.exists();
            if (alreadyExists){
                Log.i(functionName, "The file for saved settings already exists.");
            } else {
                Log.i(functionName, "The file for saved settings does not exist yet.");
                return;
            }

            CheckBox checkBoxRateVideos = (CheckBox)findViewById(R.id._checkBoxRateVideos);
            EditText editTextPerkEmailAddress = (EditText)findViewById(R.id._editTextPerkEmail);
            EditText editTextPerkPassword = (EditText)findViewById(R.id._editTextPerkPassword);
            EditText editTextStartingHour = (EditText)findViewById(R.id._editTextStartingHour);
            EditText editTextEndingHour = (EditText)findViewById(R.id._editTextEndingHour);

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

            if (parts.length > 0){
                checkBoxRateVideos.setChecked(Boolean.parseBoolean(parts[0]));
            }
            if (parts.length > 1){
                editTextPerkEmailAddress.setText(parts[1]);
            }
            if (parts.length > 2){
                editTextPerkPassword.setText(parts[2]);
            }
            if (parts.length > 3){
                editTextStartingHour.setText(parts[3]);
            }
            if (parts.length > 4){
                editTextEndingHour.setText(parts[4]);
            }

            Log.i(functionName, "The file was successfully updated.");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }
    }
}