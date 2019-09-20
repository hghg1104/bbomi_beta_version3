package andbook.example.bbomi_beta_version3;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class networkSetting extends AppCompatActivity {

    TextView gotoBTSetting;

    BluetoothSocket mmSocket;

    Spinner devicesSpinner;
    Spinner wifiSpinner;
    Button refreshDevicesButton;
    Button refreshWifiButton;
    TextView pskTextView;
    Button startButton;
    TextView messageTextView;

    WifiManager wifi;
    WifiScanReceiver wifiReciever;

    public class WifiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();

            List<String> list = new ArrayList<String>();
            for(int i = 0; i < wifiScanList.size(); i++){
                list.add(((wifiScanList.get(i)).SSID));
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(networkSetting.this,   android.R.layout.simple_spinner_item, list);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            Toast.makeText(networkSetting.this,"Scanning completed.",Toast.LENGTH_SHORT).show();
            wifiSpinner.setAdapter(spinnerArrayAdapter);
        }
    }

    private DeviceAdapter adapter_devices;

    final UUID uuid = UUID.fromString("815425a5-bfac-47bf-9321-c5ff980b5e11");
    final byte delimiter = 33;
    int readBufferPosition = 0;
    final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 12345;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network);

        gotoBTSetting = (TextView)findViewById(R.id.gotobtsetting);
        gotoBTSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });

        pskTextView = (TextView) findViewById(R.id.psk_text);

        devicesSpinner = (Spinner) findViewById(R.id.devices_spinner);
        wifiSpinner = (Spinner)findViewById(R.id.wifi_spinner);

        refreshDevicesButton = (Button) findViewById(R.id.refresh_devices_button);
        startButton = (Button) findViewById(R.id.start_button);

        refreshDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDevices();
            }
        });

        refreshWifiButton = (Button)findViewById(R.id.refreshWifi_button);
        refreshWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWifi();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = (String)wifiSpinner.getSelectedItem();
                String psk = pskTextView.getText().toString();

                BluetoothDevice device = (BluetoothDevice) devicesSpinner.getSelectedItem();
                (new Thread(new workerThread(ssid, psk, device))).start();
            }
        });


        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        refreshWifi();
        refreshDevices();

    }
    protected void refreshWifi()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }else{
            wifiScan();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            wifiScan();
        }
    }
    public void wifiScan()
    {
        Toast.makeText(networkSetting.this,"Scanning for wifi networks...",Toast.LENGTH_SHORT).show();
        wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifi.startScan();
    }


    private void refreshDevices() {
        Log.d("in","refreshDevices");
        adapter_devices = new DeviceAdapter(this, R.layout.spinner_devices, new ArrayList<BluetoothDevice>());
        devicesSpinner.setAdapter(adapter_devices);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter_devices.add(device);
            }
        }
    }

    final class workerThread implements Runnable {
        private String ssid;
        private String psk;
        private BluetoothDevice device;

        public workerThread(String ssid, String psk, BluetoothDevice device) {
            this.ssid = ssid;
            this.psk = psk;
            this.device = device;
        }

        public void run() {
            clearOutput();
            Toast.makeText(networkSetting.this,"Starting config update.",Toast.LENGTH_SHORT).show();
            Toast.makeText(networkSetting.this,"Network: " + ssid,Toast.LENGTH_SHORT).show();
            Toast.makeText(networkSetting.this,"Device: " + device.getName() + " - " + device.getAddress(),Toast.LENGTH_SHORT).show();

            try {
                mmSocket = device.createRfcommSocketToServiceRecord(uuid);
                if (!mmSocket.isConnected()) {
                    mmSocket.connect();
                    Thread.sleep(1000);
                }

                Toast.makeText(networkSetting.this,"Connected.",Toast.LENGTH_SHORT).show();


                OutputStream mmOutputStream = mmSocket.getOutputStream();
                final InputStream mmInputStream = mmSocket.getInputStream();

                waitForResponse(mmInputStream, -1);

                Toast.makeText(networkSetting.this,"Sending SSID.",Toast.LENGTH_SHORT).show();


                mmOutputStream.write(ssid.getBytes());
                mmOutputStream.flush();
                waitForResponse(mmInputStream, -1);

                Toast.makeText(networkSetting.this,"Sending PSK.",Toast.LENGTH_SHORT).show();


                mmOutputStream.write(psk.getBytes());
                mmOutputStream.flush();
                waitForResponse(mmInputStream, -1);

                mmSocket.close();

                Toast.makeText(networkSetting.this,"Success.",Toast.LENGTH_SHORT).show();


            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                Toast.makeText(networkSetting.this,"Failed.",Toast.LENGTH_SHORT).show();

            }

            Toast.makeText(networkSetting.this,"Done.",Toast.LENGTH_SHORT).show();

        }
    }

    private void clearOutput() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTextView.setText("");
            }
        });
    }

    /*
     * TODO actually use the timeout
     */
    private void waitForResponse(InputStream mmInputStream, long timeout) throws IOException {
        int bytesAvailable;

        while (true) {
            bytesAvailable = mmInputStream.available();
            if (bytesAvailable > 0) {
                byte[] packetBytes = new byte[bytesAvailable];
                byte[] readBuffer = new byte[1024];
                mmInputStream.read(packetBytes);

                for (int i = 0; i < bytesAvailable; i++) {
                    byte b = packetBytes[i];

                    if (b == delimiter) {
                        byte[] encodedBytes = new byte[readBufferPosition];
                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                        final String data = new String(encodedBytes, "US-ASCII");

                        Toast.makeText(networkSetting.this,"Received:" + data,Toast.LENGTH_SHORT).show();


                        return;
                    } else {
                        readBuffer[readBufferPosition++] = b;
                    }
                }
            }
        }
    }
}