package android.example.blecentral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class showInfo extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "Gatt Info";
    private String name;
    private String address = "";
    private String value;
    String url;
    private UUID uuids[][];
    private int key;
    private int mNum = 0;
    long start;
    long end;

    private Button connectButton;
    private Button disconnectButton;
    private Button readCumulativeButton;
    private Button connectPHP;
    private Button toURL;

    private TextView textUuids;

    private MyAsyncTask task;
    private showInfo activity;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showinfo);

        Intent intent = getIntent();
        name = intent.getStringExtra("NAME");
        address = intent.getStringExtra("ADDRESS");
        uuids = new UUID[3][3];
        activity = this;

        TextView textName = (TextView) findViewById(R.id.devicename);
        TextView textAddress = (TextView) findViewById(R.id.deviceaddress);
        textUuids = (TextView) findViewById(R.id.deviceuuids);
        textName.setText(name);
        textAddress.setText(address);

        connectButton = (Button) findViewById(R.id.connectbutton);
        disconnectButton = (Button) findViewById(R.id.disconnectbutton);
        readCumulativeButton = (Button) findViewById(R.id.readCumulativeButton);
        connectPHP = (Button)findViewById(R.id.connectPHP);
        toURL = (Button)findViewById(R.id.gotoURL);
        connectButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);
        readCumulativeButton.setOnClickListener(this);
        connectPHP.setOnClickListener(this);
        toURL.setOnClickListener(this);
        connectButton.setEnabled(false);

        uuids[0][0] = UUID.fromString("ab394469-185b-5f57-4a70-dd53dbdea64b");
        uuids[0][1] = UUID.fromString("3cb08b55-71d2-33e9-8d9d-ad3fecb06bd6");
        uuids[0][2] = UUID.fromString("3f68a506-a151-3d29-2046-fff9185ca1c8");
        uuids[1][0] = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
        uuids[1][1] = UUID.fromString("0000180e-0000-1000-8000-00805f9b34fb");
        uuids[1][2] = UUID.fromString("0000180e-0000-1000-8000-00805f9b34fb");
        uuids[2][0] = UUID.fromString("e625601e-9e55-4597-a598-76018a0d293d");
        uuids[2][1] = UUID.fromString("26e2b12b-85f0-4f3f-9fdd-91d114270e6e");
        uuids[2][2] = UUID.fromString("52dc2801-7e98-4fc2-908a-66161b5959b0");

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        connect();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == connectButton.getId()) {
            Toast.makeText(this, "start connect", Toast.LENGTH_SHORT).show();
            connect();
            return;
        }
        if (v.getId() == disconnectButton.getId()) {
            Toast.makeText(this, "stop connect", Toast.LENGTH_SHORT).show();
            disconnect();
            return;
        }
        if (v.getId() == readCumulativeButton.getId()) {
            readCharacteristic(uuids[key][0], uuids[key][1]);
            start = System.currentTimeMillis();
        }
        if(v.getId() == connectPHP.getId()){
            task = new MyAsyncTask(this , uuids[key][0], mNum);
            task.execute();
            Log.i(TAG, "start connect php");
        }
        if (v.getId() == toURL.getId()){
            connectWeb();
        }
    }

    private void connect() {
        if (address.equals("")) {
            Toast.makeText(this, "address is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothGatt != null) {
            Log.i(TAG, "device is null");
            return;
        }
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = bluetoothDevice.connectGatt(this, false, gattCallBack);
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(true);
    }

    private void disconnect() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        readCumulativeButton.setEnabled(false);
    }

    private void readCharacteristic(UUID uuid1, UUID uuid2) {
        if (bluetoothGatt == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(uuid1).getCharacteristic(uuid2);
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void returnNum(String str){
        char c = '"';
        if(str.charAt(0) == c && str.charAt(str.length()-1) == c) {
            this.url = str.substring(1, str.length()-1);
        }
        connectWeb();
    }

    private void connectWeb(){
        Uri uri = Uri.parse(url);
        Intent mIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(mIntent);
    }

    private final BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (BluetoothGatt.GATT_SUCCESS != status) {
                return;
            }
            if (BluetoothProfile.STATE_CONNECTED == newState) {
                bluetoothGatt.discoverServices();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectButton.setEnabled(true);
                    }
                });
                return;
            } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {
                bluetoothGatt.connect();
                runOnUiThread(new Runnable() {
                    public void run() {
                        readCumulativeButton.setEnabled(false);
                    }
                });
                return;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            for (BluetoothGattService service : gatt.getServices()) {
                if (service == null) {
                    Log.i(TAG, "service is null");
                    continue;
                }
                if (service.getUuid() == null) {
                    Log.i(TAG, "service uuid is null");
                    continue;
                }
                for (int i = 0; i < uuids.length; i++) {
                    if (uuids[i][0].equals(service.getUuid())) {
                        key = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textUuids.setText(uuids[key][0].toString());
                                readCumulativeButton.setEnabled(true);
                            }
                        });
                        continue;
                    }
                }
                continue;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (BluetoothGatt.GATT_SUCCESS != status) {
                return;
            }
            if (uuids[key][1].equals(characteristic.getUuid())) {
                final byte[] bytes = characteristic.getValue();
                final String trueStr = new String(bytes);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.chara1)).setText(trueStr);
                        end = System.currentTimeMillis();
                        long time = end - start;
                        Log.i("used time", "start:" +String.valueOf(start)+", end:"+String.valueOf(end)+", result"+String.valueOf(time));
                        mNum = Integer.parseInt(trueStr);
                        task = new MyAsyncTask(activity, uuids[key][0], mNum);
                        task.execute();
                        Log.i(TAG, "start connect php");
                    }
                });
                return;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(false);
        readCumulativeButton.setEnabled(false);
        if (!address.equals("")) {
            connectButton.setEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}
