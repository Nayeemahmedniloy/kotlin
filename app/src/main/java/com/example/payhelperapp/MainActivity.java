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

import com.example.payhelperapp.PayHelper;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private PayHelper payHelper;
    private EditText transAmountEditText;
    private EditText appIdEditText;
    private TextView resultTextView;
    private Button transactionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transAmountEditText = findViewById(R.id.transAmountEditText);
        appIdEditText = findViewById(R.id.appIdEditText);
        resultTextView = findViewById(R.id.resultTextView);
        transactionButton = findViewById(R.id.transactionButton);

        // Bind to the PayHelper service
        Intent intent = new Intent();
        intent.setAction("com.telpo.pay.SERVICE");
        bindService(intent, conn, BIND_AUTO_CREATE);

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

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            payHelper = PayHelper.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            payHelper = null;
        }
    };

    private void performTransaction(String transAmount, String appId) {
        JSONObject json = new JSONObject();
        try {
            json.put("Function ID", "01");
            json.put("Amount", transAmount);
            json.put("APP ID", appId);

            if (payHelper != null) {
                String result = payHelper.doTrans(json.toString());
                resultTextView.setText("Transaction Result: " + result);
            } else {
                Toast.makeText(MainActivity.this, "Service not connected", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException | RemoteException e) {
            e.printStackTrace();
            resultTextView.setText("Error: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
