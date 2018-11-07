package test.com.ATManager;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
        saveSettings();
        Button btn = (Button)findViewById(R.id._buttonStart);
        btn.setEnabled(false);
        try {
            Runtime rt = Runtime.getRuntime();
            Process runnerProcess = rt.exec("su");
            DataOutputStream os = new DataOutputStream(runnerProcess.getOutputStream());

            EditText editTextEndingHour = (EditText)findViewById(R.id._editTextEndingHour);
            EditText editTextFacebookEmailAddress = (EditText)findViewById(R.id._editTextFacebookEmail);
            EditText editTextFacebookPassword = (EditText)findViewById(R.id._editTextFacebookPassword);
            EditText editTextMinimumPoints = (EditText)findViewById(R.id._editTextMinimumPoints);
            EditText editTextMaximumPoints = (EditText)findViewById(R.id._editTextMaximumPoints);
            EditText editTextStartingHour = (EditText)findViewById(R.id._editTextStartingHour);

            os.writeBytes(String.format("setprop pATEndingHour %s\n", editTextEndingHour.getText()));
            os.writeBytes(String.format("setprop pATFaceBookUsername %s\n", editTextFacebookEmailAddress.getText()));
            os.writeBytes(String.format("setprop pATFaceBookPassword %s\n", editTextFacebookPassword.getText()));
            os.writeBytes(String.format("setprop pATMinimumPoints %s\n", editTextMinimumPoints.getText()));
            os.writeBytes(String.format("setprop pATMaximumPoints %s\n", editTextMaximumPoints.getText()));
            os.writeBytes(String.format("setprop pATStartingHour %s\n", editTextStartingHour.getText()));

            os.writeBytes("uiautomator runtest testautomator.jar -c at.runner.TestAutomator\n");
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
            String fileName = getFilesDir() + "/ATManagerSettings.txt";
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
                EditText editTextEndingHour = (EditText)findViewById(R.id._editTextEndingHour);
                EditText editTextFacebookEmailAddress = (EditText)findViewById(R.id._editTextFacebookEmail);
                EditText editTextFacebookPassword = (EditText)findViewById(R.id._editTextFacebookPassword);
                EditText editTextMinimumPoints = (EditText)findViewById(R.id._editTextMinimumPoints);
                EditText editTextMaximumPoints = (EditText)findViewById(R.id._editTextMaximumPoints);
                EditText editTextStartingHour = (EditText)findViewById(R.id._editTextStartingHour);

                writer.append(String.format("%s,", editTextEndingHour.getText()));
                writer.append(String.format("%s,", editTextFacebookEmailAddress.getText()));
                writer.append(String.format("%s,", editTextFacebookPassword.getText()));
                writer.append(String.format("%s,", editTextMinimumPoints.getText()));
                writer.append(String.format("%s,", editTextMaximumPoints.getText()));
                writer.append(String.format("%s", editTextStartingHour.getText()));

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
            String fileName = getFilesDir() + "/ATManagerSettings.txt";
            Log.i(functionName, String.format("The file name for saved settings is '%s'.", fileName));
            File file = new File(fileName);

            boolean alreadyExists = file.exists();
            if (alreadyExists){
                Log.i(functionName, "The file for saved settings already exists.");
            } else {
                Log.i(functionName, "The file for saved settings does not exist yet.");
                return;
            }

            EditText editTextEndingHour = (EditText)findViewById(R.id._editTextEndingHour);
            EditText editTextFacebookEmailAddress = (EditText)findViewById(R.id._editTextFacebookEmail);
            EditText editTextFacebookPassword = (EditText)findViewById(R.id._editTextFacebookPassword);
            EditText editTextMinimumPoints = (EditText)findViewById(R.id._editTextMinimumPoints);
            EditText editTextMaximumPoints = (EditText)findViewById(R.id._editTextMaximumPoints);
            EditText editTextStartingHour = (EditText)findViewById(R.id._editTextStartingHour);

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

            editTextEndingHour.setText(parts[0]);
            editTextFacebookEmailAddress.setText(parts[1]);
            editTextFacebookPassword.setText(parts[2]);
            editTextMinimumPoints.setText(parts[3]);
            editTextMaximumPoints.setText(parts[4]);
            editTextStartingHour.setText(parts[5]);

            Log.i(functionName, "The file was successfully updated.");
        } catch (Exception e) {
            Log.e(functionName, "The function has ended in a failure.", e);
        }
    }
}