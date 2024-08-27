package com.example.payhelperapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // Declare the AIDL interface
    private IMyAidlInterface payHelper;
    private EditText transAmountEditText;
    private EditText appIdEditText;
    private TextView resultTextView;
    private Button transactionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        transAmountEditText = findViewById(R.id.transAmountEditText);
        appIdEditText = findViewById(R.id.appIdEditText);
        resultTextView = findViewById(R.id.resultTextView);
        transactionButton = findViewById(R.id.transactionButton);

        // Bind to the PayHelper service
        Intent intent = new Intent();
        intent.setAction("com.telpo.pay.SERVICE");
        bindService(intent, conn, BIND_AUTO_CREATE);

        // Handle button click for transaction
        transactionButton.setOnClickListener(v -> {
            String transAmount = transAmountEditText.getText().toString();
            String appId = appIdEditText.getText().toString();

            if (!transAmount.isEmpty() && !appId.isEmpty()) {
                performTransaction(transAmount, appId);
            } else {
                Toast.makeText(MainActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Service connection to manage binding to the service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Connect to the AIDL interface
            payHelper = IMyAidlInterface.Stub.asInterface(service);
            Toast.makeText(MainActivity.this, "Service connected successfully.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Handle service disconnection
            payHelper = null;
            Toast.makeText(MainActivity.this, "Service disconnected.", Toast.LENGTH_SHORT).show();
        }
    };

    // Perform transaction using the AIDL service
    private void performTransaction(String transAmount, String appId) {
        try {
            // Construct the JSON request data
            JSONObject json = new JSONObject();
            json.put("Function ID", "01");
            json.put("Amount", transAmount);
            json.put("APP ID", appId);

            // Check if the service is connected (payHelper is not null)
            if (payHelper != null) {
                // Call the AIDL method
                String result = payHelper.doTrans(json.toString());
                // Update UI with the result
                resultTextView.setText("Transaction Result: " + result);
            } else {
                // If the service is not connected
                Toast.makeText(MainActivity.this, "Service not connected", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException | RemoteException e) {
            // Handle any errors
            e.printStackTrace();
            resultTextView.setText("Error: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind the service when the activity is destroyed
        unbindService(conn);
    }
}
