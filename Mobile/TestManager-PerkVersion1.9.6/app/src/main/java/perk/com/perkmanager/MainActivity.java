package perk.com.perkmanager;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File startFile = new File(Environment.getExternalStorageDirectory() + File.separator + "perkStarted.txt");
        if (startFile.exists()) {
            Button btn = (Button)findViewById(R.id.startButton);
            btn.setEnabled(false);
            btn = (Button)findViewById(R.id.stopButton);
            btn.setEnabled(true);
        }
    }

    public void start(View v) {
        Button btn = (Button)findViewById(R.id.startButton);
        btn.setEnabled(false);
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("uiautomator runtest perkautomator.jar -c com.test.runner.perkbot.PerkAutomator\n");
            os.close();
            btn = (Button)findViewById(R.id.stopButton);
            btn.setEnabled(true);
        } catch (Exception e) {
            Log.e("Start Error", e.getMessage());
            e.printStackTrace();
            btn.setEnabled(true);
        }
    }

    public void stop(View v) {
        try {
            File stopFile = new File(Environment.getExternalStorageDirectory() + File.separator + "stopPerk.txt");
            if (stopFile.createNewFile()) {
                Button btn = (Button) findViewById(R.id.stopButton);
                btn.setEnabled(false);
                btn = (Button) findViewById(R.id.startButton); // TODO: Set to true when perkStarted.txt is deleted.
                btn.setEnabled(true);
            }
        } catch (Exception e) {
            Log.e("Stop Error", e.getMessage());
            e.printStackTrace();
        }
    }

    public void apply(View v) {
        EditText emailField = (EditText)findViewById(R.id.emailField);
        EditText passwordField = (EditText)findViewById(R.id.passwordField);
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            //os.writeBytes("setprop perkUser "+emailField.getText()+"\n"); // Not used.
            os.writeBytes("setprop perkPass " + passwordField.getText() + "\n");
            os.close();
            Toast toast = Toast.makeText(getApplicationContext(), "Settings saved", Toast.LENGTH_SHORT);
            toast.show();
        } catch (Exception e) {}
    }
}