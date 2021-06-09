package android.example.bleperipheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

import io.realm.Realm;

public class peripheralActivity extends AppCompatActivity implements View.OnClickListener {
    BluetoothGattServer gattServer;
    BluetoothDevice mDevice;
    BluetoothGattCharacteristic mCharacteristic;
    BluetoothGattCharacteristic readCharacteristic;
    BluetoothManager manager;
    BluetoothAdapter adapter;
    BluetoothLeAdvertiser advertiser;
    BluetoothGattService service;

    private String TAG = "info";
    private boolean Connect = false;
    private Button add;
    private Button subtract;
    private Button reset;
    private TextView read;
    private TextView write;
    public int value1 = 0;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripheral);
        add = (Button)findViewById(R.id.add);
        subtract = (Button)findViewById(R.id.sub);
        reset = (Button)findViewById(R.id.reset);
        read = (TextView)findViewById(R.id.read);
        write = (TextView)findViewById(R.id.write);
        add.setOnClickListener(this);
        subtract.setOnClickListener(this);
        reset.setOnClickListener(this);
        read.setText(String.valueOf(value1));

        realm = Realm.getDefaultInstance();

        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Log.e(TAG, "adapter is null");
            return;
        }

        setGattServer();
    }

    public void setGattServer() {
        advertiser = adapter.getBluetoothLeAdvertiser();
        if (advertiser == null) {
            Log.e(TAG, "cant use peripheral");
            return;
        }
        gattServer = manager.openGattServer(this, gattServerCallback);

        service = new BluetoothGattService(UUID.fromString("e625601e-9e55-4597-a598-76018a0d293d"), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("26e2b12b-85f0-4f3f-9fdd-91d114270e6e"),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        service.addCharacteristic(mCharacteristic);

        readCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("52dc2801-7e98-4fc2-908a-66161b5959b0"),
                BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(readCharacteristic);
        gattServer.addService(service);

        try {
            Thread.sleep(200);
        } catch (Exception ex) {
        }

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addServiceUuid(new ParcelUuid(UUID.fromString("e625601e-9e55-4597-a598-76018a0d293d")));
        settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        settingBuilder.setTimeout(0);
        settingBuilder.setConnectable(true);

        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeDeviceName(true);

        advertiser.startAdvertising(settingBuilder.build(), dataBuilder.build(), builder.build(), new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.i(TAG, "start advertise");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.i(TAG, "cannot start advertise");
            }
        });
    }

    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mDevice = device;
                Connect = true;
                Log.d(TAG, device.toString());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Connect = false;
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                                int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            if (characteristic.getUuid().equals(UUID.fromString("26e2b12b-85f0-4f3f-9fdd-91d114270e6e"))) {
                Log.i(TAG, "first value read");
                value1 = increment(value1);
                read.setText(String.valueOf(value1));
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, String.valueOf(value1).getBytes());
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value){
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            characteristic.setValue(value);
            final String value2 = characteristic.getStringValue(offset);
            Log.i("info", value2);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    realm.beginTransaction();
                    dataModel model = realm.createObject(dataModel.class);
                    model.setValue(value2);
                    realm.commitTransaction();
                    write.setText(value2);
                }
            });
        }

    };

    private int increment(int i){
        return i+=1;
    }

    private int decrement(int i){
        return i-=1;
    }

    private int reset(int i){
        return 0;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == add.getId()){
            value1 = increment(value1);
            read.setText(String.valueOf(value1));
        }
        if (v.getId() == subtract.getId()){
            value1 = decrement(value1);
            read.setText(String.valueOf(value1));
        }
        if(v.getId() == reset.getId()){
            value1 = reset(value1);
            read.setText(String.valueOf(value1));
        }
    }
}
